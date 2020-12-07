package com.example.mintplaces1

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng

class MapActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var map: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
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
        // 지도 처음 시작 위치를 서울시청으로 설정.
        val SEOUL_CITY_HALL_LATLNG: LatLng = LatLng(37.566669, 126.978406)
        val DEFAULT_CAMERA_ZOOM: Float = 16.0f
    }

}