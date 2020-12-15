package com.example.mintplaces1.map

import android.annotation.SuppressLint
import android.location.Location
import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.mintplaces1.dto.PlaceInfo
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions

/*
* 지도, 마커, 사용자의 현재위치 등... 지도와 지도의 구성요소들을 관리.
* MapFragment와 PlaceSearchFragment에서 MapViewModel을 사용함.
* 마커 추가, 카메라 이동 등의 기능이 모두 여기서 이루어짐.
* */
class MapViewModel: ViewModel() {
    private lateinit var map: GoogleMap
    // 선택한 장소를 표시 할 마커. 장소 선택은 한 번에 한 군데밖에 안 되므로 마커 하나를 끝까지 사용.
    var marker: Marker? = null
    // 마커에 찍힌 장소 정보 저장. 추후 firebase에 매장 정보를 저장할 때 사용할 예정.
    var markerPlaceInfo: PlaceInfo? = null
    // 사용자의 현재 위치를 (얻어올 수 있으면) 얻어와서 저장함.
    // 위치 추적 처음 시작할때는 null, 그 외에는 계속 새로 얻은 위치로 업데이트 시켜줌.
    var currentLocation: Location? = null

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
            marker = addMarker(
                    MarkerOptions()
                            .position(SEOUL_CITY_HALL_LATLNG) // 마커 위치는, 일단 걍 아무 위치나 있어야되니까 넣은 것으로 별 의미는 없음
                            .visible(false) // 처음 시작할때는 안 보이게 함.
            )
        }
        // 시작할 때 카메라 설정
        setDefaultCameraLocation()
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
            val cameraUpdate: CameraUpdate = CameraUpdateFactory.newLatLng(currentLatLng)
            map.moveCamera(cameraUpdate)
        }
        // 위치가 null이 아닌 경우는 위치 업데이트만 하고 굳이 카메라 이동은 안 함.
        // 주기적으로 강제로 카메라 위치를 이동시키면 다른데 보다가 카메라가 움직여서 화날수도 있음...
        currentLocation = location
    }

    // 지정한 장소를 마커에 표시
    fun setMarker(name: String, latLng: LatLng, address: String) {
        // 마커 세팅
        marker?.apply {
            position = latLng
            isVisible = true
        }
        // 장소 정보 저장
        markerPlaceInfo = PlaceInfo(name, latLng, address)
        // 카메라 이동
        val cameraUpdate: CameraUpdate = CameraUpdateFactory.newLatLng(latLng)
        map.animateCamera(cameraUpdate) // 순간이동 말고 스르륵 이동
    }

    companion object {
        private const val TAG = "MyLogMapVM"

        // 지도 처음 시작 위치를 서울시청으로 설정.
        val SEOUL_CITY_HALL_LATLNG: LatLng = LatLng(37.566669, 126.978406)
        const val DEFAULT_CAMERA_ZOOM: Float = 16.0f
    }
}