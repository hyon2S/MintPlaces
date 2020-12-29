package com.example.mintplaces1.database

import androidx.lifecycle.ViewModel
import com.example.mintplaces1.dto.StoreInfo
import com.example.mintplaces1.dto.MarkerInfoClient
import com.example.mintplaces1.exception.StoreInfoNotExistException
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseUser
import java.lang.NullPointerException

// 민트 음식을 파는 매장 정보 등을 Firebase에 저장하는 기능
class DatabaseViewModel: ViewModel() {
    // 등록할 매장 정보를 보관함.
    private var storeInfo: StoreInfo? = null
    private val repository = Repository()

    fun setStoreInfo(name: String, latLng: LatLng, address: String) {
        storeInfo = StoreInfo(name = name, latLng = latLng, address = address)
    }

    fun initPlaceInfo() {
        storeInfo = null
    }

    // 데이터베이스에 새 매장 정보를 추가
    // 추가가 정상적으로 완료됐는지 실패했는지 결과를 반환해야되지않을까..?
    fun addStore(user: FirebaseUser) {
        try {
            repository.addStore(storeInfo!!, user)
        } catch (e: NullPointerException) {
            throw StoreInfoNotExistException()
        }
    }

    // 지정된 위도, 경도 범위 안에서 등록된 매장들을 db에서 가져옴
    suspend fun getStoresList(farRightLatLng: LatLng, nearLeftLatLng: LatLng): List<MarkerInfoClient> =
            repository.getStoresList(farRightLatLng, nearLeftLatLng)

    companion object {
        private const val TAG = "MyLogDbVM"
    }
}