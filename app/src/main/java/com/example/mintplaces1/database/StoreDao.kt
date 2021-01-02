package com.example.mintplaces1.database

import com.example.mintplaces1.dto.MarkerInfoServer
import com.example.mintplaces1.dto.PlaceInfo
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.util.*

/*
C: collection, D: document, F: Field

(위도 경도별로 매장의 마커 정보를 보관)
(복잡하게 위도 컬렉션, 경도 컬렉션을 만드는 대신, 하나의 컬렉션 밑에 모든 MarkerInfo의 문서들을 모아놓고 geoPoint로 쿼리를 하는 방법도 시도해봤으나,
GeoPoint가 소수점 이하는 제대로 대소비교를 하지 못해 사용하지 못함.)
C: byLat
    D: lat1(원래 Double인 위도를 소수점 둘째짜리까지 정수화 시킴. 예: 33.123455... -> 3312)
        F: 내용은 중요하지않지만 문서 자체가 존재하기 하기 위해 들어가는 형식적인 필드
        C: byLng
            D: lng1(원래 Double인 경도를 소수점 둘째짜리까지 정수화 시킴)
                F: 내용은 중요하지않지만 문서 자체가 존재하기 하기 위해 들어가는 형식적인 필드
                C: markerInfo
                    D: markerInfo1(autoID)
                        F: name String (이름 뺄까..?)
                        F: geoPoint GeoPoint
                        F: docRef DocumentReference (컬렉션 store 하위에 있는 각각의 store들의 DocumentReference)
                    D: markerInfo2(autoID)
                        F: name String
                        F: geoPoint GeoPoint
                        F: docRef DocumentReference (컬렉션 store 하위에 있는 각각의 store들의 DocumentReference)
            D: lng2
                (이하생략)
    D: lat2
        (이하생략)

C: store
    D: store1(autoID)
        C: placeInfo
            D: placeInfoList
                F: list Array<Reference> (placeInfo가 하나씩 추가될때마다 array에 순서대로 하나씩 넣어줌. 가장 index가 큰 것이 가장 최근에 생성된 문서.)
            D: placeInfo1(autoID)
                F: name String
                F: address String
                F: editInfo Map
                    editor String (uid)
                    timestamp TimeStamp
            D: placeInfo2(autoID)
                F: name String
                F: address String
                F: editInfo Map
                    editor String (uid)
                    timestamp TimeStamp
        C: storeType
            D: storeTypeList
                F: list Array<Reference>
            D: storeType1(autoID)
                F: storeType Map
                    디저트 Boolean
                    뷔페 Boolean
                    일반음식점 Boolean
                    마트 Boolean
                F: editInfo Map
                    editor String (uid)
                    timestamp TimeStamp
            D: storeType2(autoID)
                (이하생략)
            (이하생략)
        C: takeoutOrInstore
            D: takeoutOrInstoreList
                F: list Array<Reference>
            D: takeoutOrInstore1(autoID)
                F: takeoutOrInStore Map
                    매장취식가능 Boolean
                    포장가능 Boolean
                F: editInfo Map
                    editor String (uid)
                    timestamp TimeStamp
            D: takeoutOrInstore2(autoID)
                (이하생략)
            (이하생략)
        C: Foods
            D: Food1(autoID)
            D: Food2(autoID)
            (이하생략)
    D: store2(autoID)
        (이하생략)
* */
// Firestore에서 매장 관련 내용을 편집하는 기능을 수행
class StoreDao {
    private val db = Firebase.firestore

    // 매장을 db에 추가하고, 새 매장 정보가 저장되어있는 DocumentReference를 반환
    suspend fun addPlaceInfo(PlaceInfo: PlaceInfo): DocumentReference {
        val newStoreDocument: DocumentReference = db.collection(STORE).document()
        val placeInfoCollection: CollectionReference = newStoreDocument.collection(PLACE_INFO)
        val placeInfoListDocument: DocumentReference = placeInfoCollection.document(PLACE_INFO_LIST)
        val newPlaceInfoDocument: DocumentReference = placeInfoCollection.document()

        db.runTransaction {
            it.apply {
                set(newPlaceInfoDocument, PlaceInfo)
                set(placeInfoListDocument, hashMapOf(LIST to listOf(newPlaceInfoDocument)))
            }
        }.await()
        return newStoreDocument
    }

    // 위도, 경도 별로 정리된 문서에 새 매장 문서를 추가
    suspend fun addStoreToList(MarkerInfoServer: MarkerInfoServer, latIndex: Int, lngIndex: Int) {
        val latDocument = db.collection(BY_LAT).document(latIndex.toString())
        val lngDocument = latDocument.collection(BY_LNG).document(lngIndex.toString())
        val markerInfoDocument = lngDocument.collection(MARKER_INFO).document()

        db.runTransaction {
            it.apply {
                set(latDocument, dummyData)
                set(lngDocument, dummyData)
                set(markerInfoDocument, MarkerInfoServer)
            }
        }.await()
    }

    suspend fun getStores(fromLat: Int, toLat: Int, fromLng: Int, toLng: Int): List<DocumentSnapshot> {
        val querySnapshotList = getQuerySnapshot(fromLat, toLat, fromLng, toLng)

        // 위에서 얻은 위도 fromLat ~ toLat, 경도 fromLng ~ toLng인 쿼리 스냅샷에서
        // 하위 문서들을 모두 추출해서 하나의 list에 옮겨담음.
        val documentList = mutableListOf<DocumentSnapshot>()
        for (querySnapshot in querySnapshotList) {
            documentList += querySnapshot.documents
        }
        return documentList
    }

    // 위도 fromLat ~ toLat, 경도 fromLng ~ toLng 사이의 매장 문서를 담고있는 쿼리스냅샷을 얻어옴
    private suspend fun getQuerySnapshot(fromLat: Int, toLat: Int, fromLng: Int, toLng: Int): List<QuerySnapshot> {
        // 코루틴스코프 안에서 동기화처리를 위해 synchronized 사용
        val querySnapshotList = Collections.synchronizedList(mutableListOf<QuerySnapshot>())

        // 위도 fromLat ~ toLat
        val latQuerySnapshot: QuerySnapshot = getLatQuerySnapshot(fromLat, toLat)

        // 아래의 for문에서 얻은 job을 모두 담음
        val jobList = mutableListOf<Job>()

        /*
        * 화면에 표시되는 지도 범위가 넓어져 받아와야 할 위도, 경도 문서가 많아질수록
        * for 루프를 실행하는 데 시간이 오래 걸림.
        * for루프 안에서 코루틴스코프를 실행해서
        * 데이터를 다 받아올 때 까지 기다리지 않고 다음 루프로 넘어갈 수 있게 함.
        * 모든 for루프가 끝나고나서 joinAll()로 모든 job이 끝나는 것을 기다림.
        * (기다리지 않으면 일부 쿼리스냅샷이 list에 담기지 않을 수 있음.)
        * */
        for (latDocumentSnapshot in latQuerySnapshot.documents) {
            // 경도 fromLng ~ toLng
            val job = CoroutineScope(Dispatchers.Default).launch {
                val lngQuerySnapshot: QuerySnapshot = getLngQuerySnapshot(fromLng, toLng, latDocumentSnapshot.reference)
                for (lngDocumentSnapshot in lngQuerySnapshot.documents) {
                    val markerInfoQuerySnapshot: QuerySnapshot = lngDocumentSnapshot.reference.collection(MARKER_INFO).get().await()
                    querySnapshotList.add(markerInfoQuerySnapshot)
                }
            }
            jobList.add(job)
        }
        jobList.joinAll()
        return querySnapshotList
    }

    private suspend fun getLatQuerySnapshot(fromLat: Int, toLat: Int): QuerySnapshot =
            db.collection(BY_LAT)
                    .whereGreaterThanOrEqualTo(FieldPath.documentId(), fromLat.toString())
                    .whereLessThanOrEqualTo(FieldPath.documentId(), toLat.toString())
                    .get()
                    .await()

    private suspend fun getLngQuerySnapshot(fromLng: Int, toLng: Int, latDocumentReference: DocumentReference): QuerySnapshot =
            latDocumentReference.collection(BY_LNG)
                    .whereGreaterThanOrEqualTo(FieldPath.documentId(), fromLng.toString())
                    .whereLessThanOrEqualTo(FieldPath.documentId(), toLng.toString())
                    .get()
                    .await()

    companion object {
        private const val TAG = "MyLogStoreDao"

        // firestore 컬렉션, 문서, 필드 이름들
        private const val STORE = "store"
        private const val PLACE_INFO = "placeInfo"
        private const val PLACE_INFO_LIST = "placeInfoList"
        private const val LIST = "list"
        private const val BY_LAT = "byLat"
        private const val BY_LNG = "byLng"
        private const val MARKER_INFO = "markerInfo"

        // 아무 필드 없는 문서는 존재를 안하니까 어쩔수없이넣은 의미 없는 데이터.
        private val dummyData = hashMapOf("dummy" to 1)
    }
}