package com.example.mintplaces1.user

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.mintplaces1.R
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.android.synthetic.main.activity_username.*

// 사용자의 닉네임(displayName)을 변경하는 액티비티
class UsernameActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_username)

        // 사용자가 null인지 확인하고 null이면 종료해야되는거 아님?

        setEditTextUsername()
        btn_change_username.setOnClickListener { changeUsername() }
    }

    private fun setEditTextUsername() {
        Log.d(TAG, "setEditTextUsername()")
        val user = FirebaseUtil.getUser()
        if (user != null)
            et_username.setText(user.displayName)
    }

    private fun changeUsername() {
        Log.d(TAG, "changeUsername()")
        val username: String = et_username.text.toString().trim()
        if (username.isEmpty()) {
            // 이름 입력해달라고 메시지 띄움
            return
        }
        val user = FirebaseUtil.getUser()
        if (user != null) {
            user.updateProfile(
                    UserProfileChangeRequest.Builder()
                            .setDisplayName(username)
                            .build()
            ).addOnCompleteListener {
                if (it.isSuccessful) {
                    Log.d(TAG, "이름 변경 완료!")
                    Toast.makeText(this, getString(R.string.success_message_change_username), Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Log.d(TAG, "이름 변경 실패: ${it.exception}")
                }
            }
        } else {
            Log.d(TAG, "user is null")
        }
    }

    companion object {
        private const val TAG = "MyLogUsernameAct"
    }
}