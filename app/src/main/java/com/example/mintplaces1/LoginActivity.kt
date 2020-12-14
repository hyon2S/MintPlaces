package com.example.mintplaces1

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {
    // 로그인 옵션. 추후 추가 가능.
    val providers = arrayListOf(
        AuthUI.IdpConfig.GoogleBuilder().build()
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // 인터넷 연결 안 되어있으면 인터넷 연결 안 돼있다고 하고 걍 종료!

        val user = Firebase.auth.currentUser
        if (user != null) {
            Log.d(TAG, "이미 로그인 되어있음.")
        } else {
            // Choose authentication providers
            startActivity(
                AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setIsSmartLockEnabled(false)
                .setAvailableProviders(providers)
                .build())
        }
        finish()
    }

    companion object {
        private const val TAG = "MyLogLoginAct"
    }
}