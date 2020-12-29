package com.example.mintplaces1.dto

import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.DocumentReference

/*
* db에 저장된 매장 정보를 불러와서 마커로 띄울 때 사용.
* 사용자가 데이터베이스에 등록한 매장을 지도에 표시하는 데 필요한 정보들로 이루어짐.
* 지도상의 위치, 매장이름, 추후 마커를 눌렀을 때 매장의 다른 정보들을 불러올 수 있으므로 데이터베이스 DocumentReference가 필요.
* */
class MarkerInfoClient(val latLng: LatLng, val name: String, val documentReference: DocumentReference)