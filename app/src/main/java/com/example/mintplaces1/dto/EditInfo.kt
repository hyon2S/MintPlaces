package com.example.mintplaces1.dto

import com.google.firebase.Timestamp

// Firestore에 새 정보를 추가할 때, 정보 제공자 uid와 작성 시간을 표시할 때 사용.
class EditInfo(val uid: String, val timestamp: Timestamp)