package com.example.mintplaces1.database

import android.util.Log
import com.example.mintplaces1.dto.*
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ktx.toObject

class Repository {
    private val storeDao = StoreDao()

    // db에 새 매장 등록
    // 성공, 실패 여부를 반환해야되지 않을까..?
    suspend fun addStore(storeInfo: StoreInfo, user: FirebaseUser) {
        // StoreInfo -> PlaceInfo로 변환
        val lat = storeInfo.latLng.latitude
        val lng = storeInfo.latLng.longitude

        val geoPoint = GeoPoint(lat, lng)

        // 일단 이미 같은 위치의 store가 있는 것은 아닌지 확인하고,

        val editInfo = EditInfo(user.uid, Timestamp.now())

        val placeInfo = PlaceInfo(
                name = storeInfo.name,
                address = storeInfo.address,
                editInfo = editInfo
        )

        // 매장을 db에 추가하고, 새 매장 정보가 저장되어있는 DocumentReference를 반환
        val storeDocument = storeDao.addPlaceInfo(placeInfo)

        val markerInfoServer = MarkerInfoServer(geoPoint, storeInfo.name, storeDocument)

        // 위도, 경도 별로 정리된 문서에 새 매장 문서를 추가
        storeDao.addStoreToList(markerInfoServer, (lat * LAT_LNG_INDEX_DIGIT).toInt(), (lng * LAT_LNG_INDEX_DIGIT).toInt())
    }

    // 지정된 위도, 경도 범위 안에서 등록된 매장들을 db에서 가져옴
    // Double을 대소비교하는 것을 피하기 위해 Int화 해서 작업
    suspend fun getStoresList(farRightLatLng: LatLng, nearLeftLatLng: LatLng): List<MarkerInfoClient> {
        Log.d(TAG, "getStoresList()")

        val fromLat: Int = (nearLeftLatLng.latitude * LAT_LNG_INDEX_DIGIT).toInt()
        val toLat: Int = (farRightLatLng.latitude * LAT_LNG_INDEX_DIGIT).toInt()
        val fromLng: Int = (nearLeftLatLng.longitude * LAT_LNG_INDEX_DIGIT).toInt()
        val toLng: Int = (farRightLatLng.longitude * LAT_LNG_INDEX_DIGIT).toInt()

        val documentSnapshotList: List<DocumentSnapshot> = storeDao.getStores(fromLat, toLat, fromLng, toLng)
        Log.d(TAG, "${documentSnapshotList.size}")

        // documentSnapshot에서 필요한 정보를 빼내서 storesMarkerInfoList에 담음
        val markerInfoClientList = mutableListOf<MarkerInfoClient>()
        for (documentSnapshot in documentSnapshotList) {
            Log.d(TAG, "$nearLeftLatLng ~ $farRightLatLng: $documentSnapshot.toString()")
            val markerInfoServer: MarkerInfoServer = documentSnapshot.toObject<MarkerInfoServer>() ?: break
            Log.d(TAG, "markerInfo 얻음")

            // MapUtil에 geoPoint <-> LatLng 하면 좋을듯
            val latLng = LatLng(markerInfoServer.geoPoint!!.latitude, markerInfoServer.geoPoint!!.longitude)
            val storesMarkerInfo = MarkerInfoClient(latLng, markerInfoServer.name!!, markerInfoServer.storeDocument!!)
            markerInfoClientList.add(storesMarkerInfo)
        }
        return markerInfoClientList
    }

    companion object {
        private const val TAG = "MyLogRep"

        /*
        * 쿼리를 이용해서 현재 화면에 보이는 위도, 경도 영역의 매장 자료만을 가져오는데,
        * 구글맵에서 위도 경도를 가져올 때 소수점 14자리까지 받아옴.
        *
        * 그렇다고 해서 소수점 14자리나 되는 위도 경도들의 자료를 각각의 문서를 만들어서 보관하면 문서가 너무 많아져서
        * 쿼리를 이용해서 문서들을 받아온 다음에 합치는 작업을 할 때 시간이 너무 오래 걸림.
        * 그러므로 소수점 몇 째 자리까지만 일치하면 좌표들을 묶어서 쿼리를 하기로 함.
        * (예를 들어 위도 33.100000... ~ 33.2000000 까지를 동일한 문서에 보관한다든지...)
        *
        * 그렇게 묶어서 가져오면 화면 범위를 넘어서는 곳에서도 데이터를 일부 받아오게 됨.
        * 휴대폰 정도의 크기 되는 기기에서 매장을 찾으려고 화면을 확대했을 때 일반적으로 많이 확대해봤자 줌레벨 17.0 정도가 일반적이라고 가정.
        * 화면 바깥에 잘려서 안 보이는 곳의 자료를 좀 받아오더라도 그렇게까지 손해는 아닌 것 같은 단위가 소수점 둘째짜리라고 판단함.
        *
        * 대소비교를 할 때 오차를 줄이기 위해 Double을 Int로 바꿀 때 자릿수를 세는 용도로 사용.
        * */
        private const val LAT_LNG_INDEX_DIGIT = 100
    }
}