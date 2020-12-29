package com.example.mintplaces1.dto

import com.google.android.gms.maps.model.LatLng

/*
* AutoComplete 장소 검색창에서 장소를 검색해서 server쪽에 전달할 자료들을 담고있는 클래스.
* Repository에서 StoreInfo의 정보가 일부는 PlaceInfo로, 일부는 MarkerInfoServer로 각각 분리되어 사용됨.
* */
class StoreInfo(val name: String, val latLng: LatLng, val address: String)