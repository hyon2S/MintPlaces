package com.example.mintplaces1.map

import android.annotation.SuppressLint
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.activity.result.registerForActivityResult
import androidx.lifecycle.ViewModelProvider
import com.example.mintplaces1.R
import com.example.mintplaces1.database.DatabaseViewModel
import com.example.mintplaces1.exception.LatLngBoundException
import com.example.mintplaces1.network.NetworkConnectionCheckAdapter
import com.google.android.gms.location.*
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.material.snackbar.Snackbar

/*
* 구글맵 본체인 SupportMapFragment를 관리.
* 사용자의 요청에 따라 현재 위치를 gps에서 받아오는 기능을 설정.
* */
class MapFragment : Fragment(), OnMapReadyCallback {
    private val databaseViewModel by lazy { ViewModelProvider(requireActivity()).get(
        DatabaseViewModel::class.java) }
    private val mapViewModel by lazy { ViewModelProvider(requireActivity(), MapViewModelFactory(databaseViewModel)).get(MapViewModel::class.java) }

    // 사용자의 실시간 위치를 지도에 업데이트
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

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
                            getLastKnownLocation()
                            true
                            // If the listener returns true, the event is consumed and the default behavior (i.e. The camera moves such that it is centered on the user's location) will not occur.
                            // https://developers.google.com/android/reference/com/google/android/gms/maps/GoogleMap#setOnMyLocationButtonClickListener(com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener)
                        }

                        // 사용자 현재 위치 (또는 가장 마지막에 알려진 위치)를 얻어옴
                        getLastKnownLocation()
                    }
                    false -> {
                        Log.d(TAG, "권한 미허용")
                        // 권한이 없어서 위치 얻어올 수 없다고 메시지 띄우기
                    }
                }
            }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())

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

    @SuppressLint("MissingPermission")
    fun getLastKnownLocation() {
        fusedLocationProviderClient.lastLocation.addOnSuccessListener { location : Location? ->
            // 네트워크 연결 안 돼있으면 확인하라고 하고 종료.
            // 어차피 네트워크 연결 안 돼있으면 위치 못 받아옴.
            if (!(activity as NetworkConnectionCheckAdapter).notifyNetworkConnection())
                return@addOnSuccessListener

            // 위치가 null이면 gps 확인하라고 하기.
            if (location == null)
                Snackbar.make(requireActivity().findViewById(android.R.id.content), getString(R.string.require_gps), Snackbar.LENGTH_SHORT).show()
            else {
                try {
                    mapViewModel.moveCameraToCurrentLocation(location)
                } catch (e: LatLngBoundException) {
                    Snackbar.make(requireActivity().findViewById(android.R.id.content), e.message!!, Snackbar.LENGTH_SHORT).show()
                }
            }
        }
    }

    companion object {
        @JvmStatic fun newInstance() = MapFragment()

        private const val TAG = "MyLogMapFrag"

        val LOCATION_ACCESS_PERMISSION = arrayOf(
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION
        )
    }
}