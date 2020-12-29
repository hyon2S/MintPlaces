package com.example.mintplaces1.dto

/*
* 서버 쪽에 저장될 매장 건물의 정보. 누가 언제 작성했는지 정보도 갖고있음.
* */
class PlaceInfo(val name: String, val address: String, val editInfo: EditInfo)