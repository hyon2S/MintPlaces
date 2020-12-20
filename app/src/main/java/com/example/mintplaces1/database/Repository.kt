package com.example.mintplaces1.database

import com.example.mintplaces1.dto.EditInfo
import com.example.mintplaces1.dto.LocationInfo
import com.example.mintplaces1.dto.PlaceInfoClient
import com.example.mintplaces1.dto.PlaceInfoServer
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.GeoPoint

class Repository {
    private val storeDao = StoreDao()

    // db에 새 매장 등록
    // 성공, 실패 여부를 반환해야되지 않을까..?
    fun addStore(placeInfoClient: PlaceInfoClient, user: FirebaseUser) {
        // PlaceInfoClient -> PlaceInfoServer로 변환
        val geoPoint = GeoPoint(placeInfoClient.latLng.latitude, placeInfoClient.latLng.longitude)

        // 일단 이미 같은 위치의 store가 있는 것은 아닌지 확인하고,

        val editInfo = EditInfo(user.uid, Timestamp.now())

        val placeInfoServer = PlaceInfoServer(
                name = placeInfoClient.name,
                geoPoint =  geoPoint,
                address = placeInfoClient.address,
                editInfo = editInfo
        )

        // 매장을 db에 추가하고, 새 매장 정보가 저장되어있는 DocumentReference를 반환
        val storeDocument = storeDao.addPlaceInfo(placeInfoServer)

        val locationInfo = LocationInfo(geoPoint, storeDocument)

        // 위도, 경도 별로 정리된 문서에 새 매장 문서를 추가
        storeDao.addStoreToList(locationInfo)
    }
}