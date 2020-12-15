package com.example.mintplaces1.dto

import com.google.android.gms.maps.model.LatLng

// 장소 검색으로 얻어온 장소의 정보 저장
// 장소 정보는 구글맵에서 얻어온 정보를 그대로 사용하고, 변경하지 않음.
class PlaceInfo(val name: String, val latLng: LatLng, val address: String)