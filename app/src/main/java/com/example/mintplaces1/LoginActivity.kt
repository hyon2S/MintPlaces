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
        const val TAG = "MyLoginLog"
    }
}