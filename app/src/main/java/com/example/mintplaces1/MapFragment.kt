package com.example.mintplaces1

import android.annotation.SuppressLint
import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.activity.result.registerForActivityResult
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.*

// 더 이상 Map이 보이지 않으면 위치 업데이트 받는 것을 중단하는 코드도 추가하기
/*
* 구글맵 본체인 SupportMapFragment를 관리.
* 사용자의 요청에 따라 현재 위치를 gps에서 받아오는 기능을 설정.
* */
class MapFragment : Fragment(), OnMapReadyCallback {
    private val mapViewModel by lazy { ViewModelProvider(requireActivity()).get(MapViewModel::class.java) }

    // 사용자의 실시간 위치를 지도에 업데이트
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private val locationRequest = LocationRequest.create().apply {
        interval = 5000
        fastestInterval = 3000
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    // 위치 접근 권한
    // https://developer.android.com/training/permissions/requesting
    // https://pluu.github.io/blog/android/2020/05/01/migation-activity-result/
    private val locationAccessPermissionLauncher =
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

                // 네트워크 연결 확인
                (activity as NetworkConnectionCheckAdapter).notifyNetworkConnection()

                when (isGranted) {
                    true -> {
                        Log.d(TAG, "권한 허용")

                        // 내위치버튼사용 & 내위치버튼누르면 실행할 내용 세팅
                        mapViewModel.enableMyLocation {
                            // 내위치버튼 누르면 사용자 위치 얻어오기
                            startLocationUpdates()
                            true
                            // If the listener returns true, the event is consumed and the default behavior (i.e. The camera moves such that it is centered on the user's location) will not occur.
                            // https://developers.google.com/android/reference/com/google/android/gms/maps/GoogleMap#setOnMyLocationButtonClickListener(com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener)
                        }

                        // 사용자 위치 얻어오기 시작
                        startLocationUpdates()
                    }
                    false -> {
                        Log.d(TAG, "권한 미허용")
                        // 위치 얻어올 수 없다고 메시지 띄우기
                    }
                }
            }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        // https://developer.android.com/training/location/request-updates#callback
        // 주기적으로 현재 위치를 gps에서 받아옴
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                Log.d(TAG, "onLocationResult()")
                locationResult ?: return
                for (location in locationResult.locations){
                    // 그나저나 location이 null일수도있나?
                    mapViewModel.updateCurrentLocation(location)
                }
            }
        }

        // 지도 본체 세팅
        // childFragmentManager는 onViewCreated에서: https://stackoverflow.com/questions/18935505/where-to-call-getchildfragmentmanager
        val mapFragment = childFragmentManager.findFragmentById(R.id.frg_map) as SupportMapFragment?
            ?: SupportMapFragment.newInstance().also {
                childFragmentManager.beginTransaction().replace(R.id.frg_map, it).commit()
            }
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        // 맵 초기화
        mapViewModel.setMap(googleMap)
        mapViewModel.initMap()

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
        val client: SettingsClient = LocationServices.getSettingsClient(requireActivity())
        client.checkLocationSettings(builder.build())
                .addOnSuccessListener {
                    Log.d(TAG, "위치 설정 이상 없음")
                    removeAndRequestLocationUpdates()
                }
                .addOnFailureListener {
                    Log.d(TAG, "위치 설정 실패: ${it.message}")
                    if (it is ResolvableApiException){
                        try {
                            Log.d(TAG, "startIntentSenderForResult()")
                            // https://stackoverflow.com/questions/40110823/start-resolution-for-result-in-a-fragment
                            startIntentSenderForResult(it.resolution.intentSender, GPS_REQ_CODE, null, 0, 0, 0,null)
                            // ActivityResultLauncher를 사용하는 방법도 있나..?
                            // 결과 처리는 onActivityResult()에서...
                        } catch (sendEx: IntentSender.SendIntentException) {
                            Log.d(TAG, "startIntentSenderForResult 예외 catch")
                        }
                    }
                }
    }

    // 기존 locationRequest가 있으면 제거하고 다시 처음부터 위치 업데이트 시작.
    @SuppressLint("MissingPermission")
    private fun removeAndRequestLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        mapViewModel.currentLocation = null
        (activity as NetworkConnectionCheckAdapter).notifyNetworkConnection()
        // 위치 정보 받아오기 시작
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }

    @SuppressLint("MissingPermission")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.d(TAG, "onActivityResult()")
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            GPS_REQ_CODE -> {
                if (resultCode == AppCompatActivity.RESULT_OK) {
                    // GPS 설정도 잘 되었으니 위치 업데이트 다시 시도
                    removeAndRequestLocationUpdates()
                } else {
                    // 위치 정보에 접근할 수 없으니 걍 마지막 접속 위치로 카메라 이동
                }
            }
        }
    }

    companion object {
        @JvmStatic fun newInstance() = MapFragment()

        private const val TAG = "MyLogMapFrag"

        // 위치권한
        private val GPS_REQ_CODE: Int = 100

        val LOCATION_ACCESS_PERMISSION = arrayOf(
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION
        )
    }
}