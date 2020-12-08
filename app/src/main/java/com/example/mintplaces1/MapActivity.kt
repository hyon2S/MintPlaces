package com.example.mintplaces1

import android.annotation.SuppressLint
import android.content.Intent
import android.content.IntentSender
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.activity.result.registerForActivityResult
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.Task

class MapActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var map: GoogleMap
    // 현재 위치 추적
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private var currentLocation: Location? = null // 위치 추적 처음 시작할때는 null, 그 외에는 계속 새로 얻은 위치로 업데이트 시켜줌.
    private val locationRequest = LocationRequest.create().apply {
        interval = 5000
        fastestInterval = 5000
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

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
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

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

        // 지도 본체 세팅
        val mapFragment = supportFragmentManager.findFragmentById(R.id.frg_map) as SupportMapFragment?
                ?: SupportMapFragment.newInstance().also {
                    supportFragmentManager.beginTransaction().replace(R.id.frg_map, it).commit()
                }
        mapFragment.getMapAsync(this)
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
    // 현재 사용자의 위치 정보를 받아오는 세팅
    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        val builder = LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)
        val client: SettingsClient = LocationServices.getSettingsClient(this)
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())
        task.addOnSuccessListener {
            Log.d(TAG, "위치 설정 이상 없음")
            // 인터넷 연결 확인 필요
            // 위치 정보 받아오기 시작
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
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

    @SuppressLint("MissingPermission")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.d(TAG, "onActivityResult()")
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            GPS_REQ_CODE -> {
                Log.d(TAG, "resultCode: ${resultCode}")
                if (resultCode == RESULT_OK) {
                    // 인터넷 연결 확인 필요
                    // 위치 정보 받아오기 한 번 더 도전..
                    fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
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
        }
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
    }

}