package com.example.mintplaces1.database

import com.example.mintplaces1.dto.LocationInfo
import com.example.mintplaces1.dto.PlaceInfoServer
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

/*
C: collection, D: document, F: Field

C: store
    D: storeByLatitude
    (지도에 매장을 마커로 표시할 때 빠르게 가져올 수 있도록 위치 정보와 reference만으로 이루어진 정보를 보관)
        C: 37.5 (37.5 <= 위도 < 37.6 인 매장들)
            D: storeByLongitude
                C: 126.9 (126.9 <= 경도 < 127.0 인 매장들)
                    D: store1(autoID)
                        F: geoPoint GeoPoint
                        F: document Reference
                    D: store2(autoID)
                        F: geoPoint GeoPoint
                        F: document Reference
                C: 127.0
                    D: store1(autoID)
                        F: geoPoint GeoPoint
                        F: document Reference
        C: 35.2
            D: storeByLongitude
                C: 128.9
                    D: store1(autoID)
                        F: geoPoint GeoPoint
                        F: document Reference
                    D: store2(autoID)
                        F: geoPoint GeoPoint
                        F: document Reference
                C: 126.8
                    D: store1(autoID)
                        F: geoPoint GeoPoint
                        F: document Reference
    D: store1(autoID)
        C: placeInfo
            D: placeInfoList(autoID)
                F: list Array<Reference>
                (placeInfo를 수정할 때 마다 기존 문서를 변경하는 것이 아닌, 새 문서가 하나씩 추가됨.
                가장 index가 큰 것이 가장 최근에 생성된 문서.)
            D: placeInfo1(autoID)
                F: name String
                F: address String
                F: geoPoint GeoPoint
                F: editInfo Map
                    editor String (uid)
                    timestamp TimeStamp
            D: placeInfo2(autoID)
                F: name String
                F: address String
                F: geoPoint GeoPoint
                F: editInfo Map
                    editor String (uid)
                    timestamp TimeStamp
        C: storeType
            D: storeTypeList(autoID)
                F: list Array<Reference>
                (storeType을 수정할 때 마다 기존 문서를 변경하는 것이 아닌, 새 문서가 하나씩 추가됨.
                가장 index가 큰 것이 가장 최근에 생성된 문서.)
            D: storeType1(autoID)
                (이하생략)
            D: storeType2(autoID)
                (이하생략)
    D: store2(autoID)
        (이하생략)
* */
// Firestore에서 매장 관련 내용을 편집하는 기능을 수행
class StoreDao {
    private val db = Firebase.firestore

    // 매장을 db에 추가하고, 새 매장 정보가 저장되어있는 DocumentReference를 반환
    fun addPlaceInfo(placeInfoServer: PlaceInfoServer): DocumentReference {
        val newStoreDocument: DocumentReference = db.collection("store").document()

        val placeInfoCollection: CollectionReference = newStoreDocument.collection("placeInfo")

        val placeInfoListDocument: DocumentReference = placeInfoCollection.document("placeInfoList")

        val newPlaceInfoDocument: DocumentReference = placeInfoCollection.document()

        db.runTransaction {
            it.apply {
                set(newPlaceInfoDocument, placeInfoServer)
                set(placeInfoListDocument, hashMapOf("list" to listOf(newPlaceInfoDocument)))
            }
        } // addOnSuccess, addOnFailure 해야되나..?
        return newStoreDocument
    }

    // 위도, 경도 별로 정리된 문서에 새 매장 문서를 추가
    fun addStoreToList(locationInfo: LocationInfo) {
        val byLat: String = to1Decimal(locationInfo.geoPoint.latitude)
        val byLng: String = to1Decimal(locationInfo.geoPoint.longitude)

        val documentRef = db.collection("store").document("storeByLatitude").collection(byLat)
                .document("storeByLongitude").collection(byLng)
                .document()
        db.runTransaction {
            it.set(documentRef, locationInfo)
        }
    }

    // double을 소수점 첫째자리까지만 잘라서 (반올림 아님) String 형태로 반환
    private fun to1Decimal(double: Double): String {
        val string = double.toString()
        val index = string.indexOf('.')
        return string.substring(0, index + 2)
    }

    companion object {
        private const val TAG = "MyLogStoreDao"
    }
}