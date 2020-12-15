package com.example.mintplaces1.database

import androidx.lifecycle.ViewModel
import com.example.mintplaces1.dto.PlaceInfo
import com.google.android.gms.maps.model.LatLng

// 민트 음식을 파는 매장 정보 등을 Firebase에 저장하는 기능
class DatabaseViewModel: ViewModel() {
    // 등록할 매장 정보를 보관함.
    var markerPlaceInfo: PlaceInfo? = null

    fun setPlace(name: String, latLng: LatLng, address: String) {
        markerPlaceInfo = PlaceInfo(name, latLng, address)
    }
}