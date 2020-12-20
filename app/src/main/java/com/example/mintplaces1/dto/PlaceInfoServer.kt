package com.example.mintplaces1.dto

import com.google.firebase.firestore.GeoPoint

// 매장 건물의 장소 정보와, 작성자, 작성시간 정보를 갖고있음
class PlaceInfoServer(val name: String, val geoPoint: GeoPoint, val address: String, val editInfo: EditInfo)