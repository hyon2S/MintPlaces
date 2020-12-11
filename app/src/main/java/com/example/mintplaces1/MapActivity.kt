package com.example.mintplaces1

import android.annotation.SuppressLint
import android.content.Intent
import android.content.IntentSender
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.activity.result.registerForActivityResult
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.Task
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import kotlinx.android.synthetic.main.activity_map.*

class MapActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var map: GoogleMap
    // 현재 위치 추적
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private var currentLocation: Location? = null // 위치 추적 처음 시작할때는 null, 그 외에는 계속 새로 얻은 위치로 업데이트 시켜줌.
    private val locationRequest = LocationRequest.create().apply {
        interval = 5000
        fastestInterval = 3000
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }
    // 선택한 장소를 표시 할 마커. 장소 선택은 한 번에 한 군데밖에 안 되므로 마커 하나를 끝까지 사용.
    private var marker: Marker? = null
    // 네트워크 연결 상태 확인
    private lateinit var networkConnectionChecker: NetworkConnectionChecker

    // 위치 접근 권한
    // https://developer.android.com/training/permissions/requesting
    // https://pluu.github.io/blog/android/2020/05/01/migation-activity-result/
    val locationAccessPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions(), LOCATION_ACCESS_PERMISSION) {
                Log.d(TAG, "registerForActivityResult")
                // 권한 모두 허용이면 true, 하나라도 미허용이면 false
                var isGranted: Boolean = true
                for (permission in it) {
                    if (permission.value == false) {
                        isGranted = false
                        break
                    }
                }

                networkConnectionChecker.notifyNetworkConnection()

                @SuppressLint("MissingPermission")
                when (isGranted) {
                    true -> {
                        Log.d(TAG, "권한 허용")
                        map.isMyLocationEnabled = true
                        // 현재 위치 얻어오기
                        startLocationUpdates()
                    }
                    false -> {
                        Log.d(TAG, "권한 미허용")
                        // 위치 얻어올 수 없다고 메시지 띄우기
                    }
                }
            }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate()")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        // 툴바 설정
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayShowTitleEnabled(false)
        // 사이드바 설정
        drawable_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        // https://developer.android.com/training/location/request-updates#callback
        // 주기적으로 현재 위치를 gps에서 받아와서 전역변수 currentLocation에 업데이트하게 함.
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                Log.d(TAG, "onLocationResult()")
                locationResult ?: return
                for (location in locationResult.locations){
                    // 그나저나 location이 null일수도있나?
                    if (currentLocation == null) {
                        // 맨 처음에 기본 위치에서 시작할 때는 현재 위치로 카메라 이동하고 시작함.
                        val currentLatLng = LatLng(location.latitude, location.longitude)
                        val cameraUpdate: CameraUpdate = CameraUpdateFactory.newLatLng(currentLatLng)
                        map.moveCamera(cameraUpdate)
                    }
                    // 위치가 null이 아닌 경우는 위치 업데이트만 하고 굳이 그 위치로 이동은 안 함.
                    // 주기적으로 강제로 그 위치로 이동시키면 다른데 보다가 움직여서 화날수도 있음...
                    currentLocation = location
                }
            }
        }
        networkConnectionChecker = NetworkConnectionChecker(baseContext)

        // 지도 본체 세팅
        val mapFragment = supportFragmentManager.findFragmentById(R.id.frg_map) as SupportMapFragment?
                ?: SupportMapFragment.newInstance().also {
                    supportFragmentManager.beginTransaction().replace(R.id.frg_map, it).commit()
                }
        mapFragment.getMapAsync(this)

        // 장소 검색창 세팅
        // https://developers.google.com/places/android-sdk/autocomplete
        val placeSearchFragment = supportFragmentManager.findFragmentById(R.id.frg_place_search) as AutocompleteSupportFragment?
                ?: AutocompleteSupportFragment.newInstance().also {
                    supportFragmentManager.beginTransaction().replace(R.id.frg_place_search, it).commit()
                }
        // place 초기화. 안 하니까 오류생김.
        if (!Places.isInitialized())
            Places.initialize(baseContext, getString(R.string.google_map_api_key))
        // 장소 이름과 위도경도 정보를 사용할 예정임.
        placeSearchFragment.setPlaceFields(listOf(Place.Field.NAME, Place.Field.LAT_LNG))
        placeSearchFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                Log.i(TAG, "Place: ${place.name}, ${place.latLng}")
                // 마커 위치를 검색한 장소로 옮김
                val latLng: LatLng = place.latLng!!
                marker?.apply {
                    position = latLng
                    title = "${place.name}"
                    isVisible = true
                    showInfoWindow()
                }
                // 그 장소로 카메라 이동
                val cameraUpdate: CameraUpdate = CameraUpdateFactory.newLatLng(latLng)
                map.animateCamera(cameraUpdate) // 순간이동 말고 스르륵 이동
            }
            override fun onError(status: Status) {
                Log.i(TAG, "An error occurred: $status")
            }
        })
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        setMap()

        // 현재 위치로 이동하기 전, 지도를 서울시청에서 시작
        val cameraUpdate: CameraUpdate = CameraUpdateFactory.newLatLngZoom(SEOUL_CITY_HALL_LATLNG, DEFAULT_CAMERA_ZOOM)
        map.moveCamera(cameraUpdate)

        // 현재 위치 받아올 수 있으면 받아와서 카메라 현재 위치로 이동시킴.
        Log.d(TAG, "권한요청")
        locationAccessPermissionLauncher.launch()
    }

    // https://developer.android.com/training/location/change-location-settings#get-settings
    // https://developer.android.com/training/location/change-location-settings#prompt
    // 현재 사용자의 위치 정보를 받아옴
    private fun startLocationUpdates() {
        val builder = LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)
        val client: SettingsClient = LocationServices.getSettingsClient(this)
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())
        task.addOnSuccessListener {
            Log.d(TAG, "위치 설정 이상 없음")
            removeAndRequestLocationUpdates()
        }
        task.addOnFailureListener {
            Log.d(TAG, "위치 설정 실패: ${it.message}")
            if (it is ResolvableApiException){
                try {
                    it.startResolutionForResult(this, GPS_REQ_CODE)
                    // ActivityResultLauncher를 사용하는 방법도 있나..?
                    // 결과 처리는 onActivityResult()에서...
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }
    }

    // 기존 locationRequest가 있으면 제거하고 다시 처음부터 위치 업데이트 시작.
    @SuppressLint("MissingPermission")
    private fun removeAndRequestLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        currentLocation = null
        networkConnectionChecker.notifyNetworkConnection()
        // 위치 정보 받아오기 시작
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }

    @SuppressLint("MissingPermission")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.d(TAG, "onActivityResult()")
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            GPS_REQ_CODE -> {
                if (resultCode == RESULT_OK) {
                    // GPS 설정도 잘 되었으니 위치 업데이트 다시 시도
                    removeAndRequestLocationUpdates()
                } else {
                    // 위치 정보에 접근할 수 없으니 걍 마지막 접속 위치로 카메라 이동
                }
            }
        }
    }

    // 지도 초기 세팅
    private fun setMap() {
        map.apply {
            mapType = GoogleMap.MAP_TYPE_NORMAL
            // isMyLocationEnabled 는 권한 확인 후 세팅

            uiSettings.apply {
                isCompassEnabled = true
                isZoomControlsEnabled = true
                isMyLocationButtonEnabled = true
            }
            setOnMyLocationButtonClickListener {
                // 사용자 위치 얻어오기 시작
                startLocationUpdates()
                true
                // If the listener returns true, the event is consumed and the default behavior (i.e. The camera moves such that it is centered on the user's location) will not occur.
                // https://developers.google.com/android/reference/com/google/android/gms/maps/GoogleMap#setOnMyLocationButtonClickListener(com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener)
            }
            // 처음 시작할 때 visible을 false로 세팅한 마커를 일단 지도에 추가해놓고,
            marker = addMarker(MarkerOptions()
                    .position(SEOUL_CITY_HALL_LATLNG) // 마커 위치는, 일단 걍 아무 위치나 있어야되니까 넣은 것으로 별 의미는 없음
                    .visible(false) // 처음 시작할때는 안 보이게 함.
                    .title("")
            )
            // 지도를 클릭하면 클릭한 위치로 마커를 옮김.
            setOnMapClickListener {
                Log.d(TAG, "맵 클릭")
                val latLng = LatLng(it.latitude, it.longitude)
                marker?.apply {
                    position = latLng
                    title = ""
                    isVisible = true
                }
            }
        }
    }

    // 툴바 메뉴 세팅
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_my -> {
                drawable_layout.openDrawer(navigation_view)
                setNavigationMode(NavigationMode.MY)
                return true
            }
            R.id.action_search -> {
                drawable_layout.openDrawer(navigation_view)
                setNavigationMode(NavigationMode.SEARCH)
                return true
            }
            else ->
                return super.onOptionsItemSelected(item)
        }
    }

    private val myNavigationFragment: MyNavigationFragment by lazy { supportFragmentManager.findFragmentByTag(MY_FRAGMENT_TAG) as MyNavigationFragment?
        ?: MyNavigationFragment.newInstance() }
    private val searchNavigationFragment: SearchNavigationFragment by lazy { supportFragmentManager.findFragmentByTag(SEARCH_FRAGMENT_TAG) as SearchNavigationFragment?
        ?: SearchNavigationFragment.newInstance() }

    // 네비게이션 드로어 안에 보여지는 내용물 설정
    private fun setNavigationMode(navigationMode: NavigationMode) {
        val fragment =
            when (navigationMode) {
                NavigationMode.MY -> {
                    myNavigationFragment
                }
                NavigationMode.SEARCH -> {
                    searchNavigationFragment
                }
            }
        supportFragmentManager.beginTransaction()
            .replace(R.id.navigation_content, fragment)
            .commit()
    }

    companion object {
        const val TAG = "MyMapLog"
        // 지도 처음 시작 위치를 서울시청으로 설정.
        val SEOUL_CITY_HALL_LATLNG: LatLng = LatLng(37.566669, 126.978406)
        val DEFAULT_CAMERA_ZOOM: Float = 16.0f

        private val GPS_REQ_CODE: Int = 100

        val LOCATION_ACCESS_PERMISSION = arrayOf(
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION
        )

        // 네비게이션 드로어 내용물의 모드
        enum class NavigationMode {
            MY, SEARCH
        }

        const val MY_FRAGMENT_TAG = "myFragmentTag"
        const val SEARCH_FRAGMENT_TAG = "searchFragmentTag"
    }

}