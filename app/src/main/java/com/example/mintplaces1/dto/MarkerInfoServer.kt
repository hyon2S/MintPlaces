package com.example.mintplaces1.dto

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.GeoPoint

// 매장의 좌표와, 매장 정보가 저장되어있는 DocumentReference로 이루어짐
// db에서 정보를 빠르게 가져와 지도에 위치만 표시할 수 있게 하는 용도.
// firstore에서 toObject로 데이터를 받아오려면 매개변수가 없는 기본생성자가 있어야됨.
class MarkerInfoServer(var geoPoint: GeoPoint? = null, var name: String? = null, var storeDocument: DocumentReference? = null)