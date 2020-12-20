package com.example.mintplaces1.database

import androidx.lifecycle.ViewModel
import com.example.mintplaces1.dto.PlaceInfoClient
import com.example.mintplaces1.exception.PlaceInfoNotExistException
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseUser
import java.lang.NullPointerException

// 민트 음식을 파는 매장 정보 등을 Firebase에 저장하는 기능
class DatabaseViewModel: ViewModel() {
    // 등록할 매장 정보를 보관함.
    private var placeInfoClient: PlaceInfoClient? = null
    private val repository = Repository()

    fun setPlace(name: String, latLng: LatLng, address: String) {
        placeInfoClient = PlaceInfoClient(name = name, latLng = latLng, address = address)
    }

    fun initPlaceInfo() {
        placeInfoClient = null
    }

    // 데이터베이스에 새 매장 정보를 추가
    // 추가가 정상적으로 완료됐는지 실패했는지 결과를 반환해야되지않을까..?
    fun addStore(user: FirebaseUser) {
        try {
            repository.addStore(placeInfoClient!!, user)
        } catch (e: NullPointerException) {
            throw PlaceInfoNotExistException()
        }
    }
}