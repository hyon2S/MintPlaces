package com.example.mintplaces1.map

import android.annotation.SuppressLint
import android.location.Location
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mintplaces1.database.DatabaseViewModel
import com.example.mintplaces1.dto.MarkerInfoClient
import com.example.mintplaces1.exception.LatLngBoundException
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/*
* 지도, 마커, 사용자의 현재위치 등... 지도와 지도의 구성요소들을 관리.
* MapFragment와 PlaceSearchFragment에서 MapViewModel을 사용함.
* 마커 추가, 카메라 이동 등의 기능이 모두 여기서 이루어짐.
* */
class MapViewModel(private val databaseViewModel: DatabaseViewModel): ViewModel() {
    private lateinit var map: GoogleMap
    // 선택한 장소를 표시 할 마커. 장소 선택은 한 번에 한 군데밖에 안 되므로 마커 하나를 끝까지 사용.
    var placeSearchMarker: Marker? = null
    // 사용자의 현재 위치를 (얻어올 수 있으면) 얻어와서 저장함.
    // 위치 추적 처음 시작할때는 null, 그 외에는 계속 새로 얻은 위치로 업데이트 시켜줌.
    var currentLocation: Location? = null
    // 우리 나라 범위. 지도가 표시할 수 있는 범위를 우리나라로 제한하기 위해 사용.
    private val latLngBounds = LatLngBounds(SOUTH_KOREA_SOUTH_WEST, SOUTH_KOREA_NORTH_EAST)

    // db에서 매장 위치를 받아오는 작업. 중간에 다른 일이 생기면 취소할 수 있게 따로 보관해 둠.
    private var getStoreJob: Job? = null
    // db에서 받아온 매장 위치를 표시하는 마커를 보관. 화면을 갱신해서 마커를 새로 받아올 때는 marker.remove()를 하고 list를 비운 뒤 다시 채움.
    private val storeMarkerList = mutableListOf<Marker>()

    fun setMap(map: GoogleMap) {
        this.map = map
    }

    // 지도 초기 세팅
    fun initMap() {
        map.apply {
            mapType = GoogleMap.MAP_TYPE_NORMAL
            // isMyLocationEnabled 는 권한 확인 후 세팅 예정

            uiSettings.apply {
                isCompassEnabled = true
                isZoomControlsEnabled = true
                isMyLocationButtonEnabled = true
            }
            // 처음 시작할 때 visible을 false로 세팅한 마커를 일단 지도에 추가해놓고,
            placeSearchMarker = addMarker(
                    MarkerOptions()
                            .position(SEOUL_CITY_HALL_LATLNG) // 마커 위치는, 일단 걍 아무 위치나 있어야되니까 넣은 것으로 별 의미는 없음
                            .visible(false) // 처음 시작할때는 안 보이게 함.
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
            )

            setOnMarkerClickListener {
                // 장소 검색 마커는 제목이 없으니까 db에서 받아온 매장 이름을 보여주는 용도로만 사용.
                it.showInfoWindow()
                true
                // 마커 중심으로 카메라가 이동 못 하게 누르는 동작을 소모함.
                // 마커 누를때마다 카메라가 이동하면 db에서 매장 로딩을 다시 해와야되니까..
            }

            // 지도에서 우리나라만 볼 수 있게 제한
            setLatLngBoundsForCameraTarget(latLngBounds)

            setOnCameraIdleListener {
                // 카메라 이동이 멈추면 카메라 범위 내에 있는 매장 정보를 얻어오기
                val farRightLatLng = map.projection.visibleRegion.farRight
                val nearLeftLatLng = map.projection.visibleRegion.nearLeft
                Log.d(TAG, "카메라 범위: ${farRightLatLng}, ${nearLeftLatLng}")

                // 현재 카메라가 보여주는 범위 안에 있는 매장 정보 얻어오기
                showStores(farRightLatLng, nearLeftLatLng)
            }
        }
        // 시작할 때 카메라 설정
        setDefaultCameraLocation()
    }

    // 지정된 위도, 경도 범위 안에서 등록된 매장들을 db에서 가져옴
    private fun showStores(farRightLatLng: LatLng, nearLeftLatLng: LatLng) {
        Log.d(TAG, "showStores()")

        // 기존에 하고 있던 동작이 있으면 취소시킴
        cancelStoresList()

        // 중간에 다른 변수가 생기면 scope 자체를 취소할 수 있게 job으로 만듦
        getStoreJob = viewModelScope.launch {
            val markerInfoClientList: List<MarkerInfoClient> = databaseViewModel.getStoresList(farRightLatLng, nearLeftLatLng)
            for (marker in markerInfoClientList) {
                showStoreMarker(marker)
            }
        }
        Log.d(TAG, "showStores 종료")
    }

    private fun showStoreMarker(markerInfoClient: MarkerInfoClient) {
        Log.d(TAG, "showStoreMarker()")
        val markerOptions = MarkerOptions()
                .position(markerInfoClient.latLng)
                .title(markerInfoClient.name)
                .visible(true)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
        // MarkerInfoClient의 DocumentReference는 추후 사용 방법 생각해보기

        // 마커 추후 삭제하려면 전역변수 리스트에 저장해두기. Marker.remove()
        val marker = map.addMarker(markerOptions)
        storeMarkerList.add(marker)
    }

    private fun cancelStoresList() {
        getStoreJob?.cancel() // db에서 받아오던 것이 있으면 취소하고

        // 있던 마커는 모두 제거
        for (marker in storeMarkerList)
            marker.remove()
        storeMarkerList.clear()
    }

    // 지도의 기본 카메라 시작 위치를 서울시청으로 설정
    fun setDefaultCameraLocation() {
        val cameraUpdate: CameraUpdate = CameraUpdateFactory.newLatLngZoom(
            SEOUL_CITY_HALL_LATLNG,
            DEFAULT_CAMERA_ZOOM
        )
        map.moveCamera(cameraUpdate)
    }

    // 위치 권한이 허용되면 호출되게 함.
    // 내위치버튼 설정
    fun enableMyLocation(listener: GoogleMap.OnMyLocationButtonClickListener) {
        @SuppressLint("MissingPermission")
        map.isMyLocationEnabled = true
        map.setOnMyLocationButtonClickListener { listener.onMyLocationButtonClick() }
    }

    // 사용자의 현재 위치(var currentLocation)를 업데이트함
    // 내위치버튼을 누르면 MapFragment에 의해 주기적으로 호출되도록 함
    fun updateCurrentLocation(location: Location) {
        if (currentLocation == null) {
            // 맨 처음에 위치를 받아올 때는 현재 위치로 카메라 이동하고 시작함.
            val currentLatLng = LatLng(location.latitude, location.longitude)
            // 우리나라 범위 밖이면 예외를 발생시켜서 중단시킴.
            if (!latLngBounds.contains(currentLatLng)) throw LatLngBoundException()
            val cameraUpdate: CameraUpdate = CameraUpdateFactory.newLatLng(currentLatLng)
            map.moveCamera(cameraUpdate)
        }
        // 위치가 null이 아닌 경우는 위치 업데이트만 하고 굳이 카메라 이동은 안 함.
        // 주기적으로 강제로 카메라 위치를 이동시키면 다른데 보다가 카메라가 움직여서 화날수도 있음...
        currentLocation = location
    }

    // 지정한 장소를 마커에 표시
    fun setMarker(latLng: LatLng) {
        // 우리나라 범위 밖이면 예외를 발생시켜서 중단시킴.
        if (!latLngBounds.contains(latLng)) throw LatLngBoundException()

        // 마커 세팅
        placeSearchMarker?.apply {
            position = latLng
            isVisible = true
        }
        // 카메라 이동
        val cameraUpdate: CameraUpdate = CameraUpdateFactory.newLatLng(latLng)
        map.animateCamera(cameraUpdate) // 순간이동 말고 스르륵 이동
    }

    companion object {
        private const val TAG = "MyLogMapVM"

        // 지도 처음 시작 위치를 서울시청으로 설정.
        val SEOUL_CITY_HALL_LATLNG: LatLng = LatLng(37.566669, 126.978406)
        const val DEFAULT_CAMERA_ZOOM: Float = 16.0f

        // 우리나라 동북, 서남 좌표
        val SOUTH_KOREA_NORTH_EAST = LatLng(38.5, 131.9)
        val SOUTH_KOREA_SOUTH_WEST = LatLng(33.0, 125.0)
    }
}