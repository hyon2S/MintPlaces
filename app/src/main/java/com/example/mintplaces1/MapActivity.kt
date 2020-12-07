package com.example.mintplaces1

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.gms.maps.*

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
    }
}