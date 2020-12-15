package com.example.mintplaces1.user

import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

// 여러 액티비티에서 공통적으로 쓰일 Firebase 관련 기능들을 여기에 몰아넣을 예정
class FirebaseUtil {
    companion object {
        private val auth = Firebase.auth

        fun getUser(): FirebaseUser? = auth.currentUser
    }
}