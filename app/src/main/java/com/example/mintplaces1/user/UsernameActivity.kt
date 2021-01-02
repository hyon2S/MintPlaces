package com.example.mintplaces1.user

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.mintplaces1.R
import com.example.mintplaces1.network.NetworkConnectionCheckAdapter
import com.example.mintplaces1.network.NetworkConnectionChecker
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.android.synthetic.main.activity_username.*

// 사용자의 닉네임(displayName)을 변경하는 액티비티
class UsernameActivity : AppCompatActivity(), NetworkConnectionCheckAdapter {
    // 네트워크 연결 상태 확인
    override lateinit var networkConnectionChecker: NetworkConnectionChecker
    // 호출했을 시점에 네트워크 연결이 끊겨있으면 메시지 띄움.
    override fun notifyNetworkConnection(): Boolean {
        val isNetworkConnected = networkConnectionChecker.isNetworkConnected()
        if (!isNetworkConnected)
            Toast.makeText(this, getString(R.string.request_network_connection), Toast.LENGTH_SHORT).show()
        return isNetworkConnected
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_username)

        networkConnectionChecker = NetworkConnectionChecker(baseContext)

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
            Toast.makeText(this, getString(R.string.require_username_message), Toast.LENGTH_SHORT).show()
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
                    notifyNetworkConnection()
                    Toast.makeText(this, getString(R.string.fail_message_change_username), Toast.LENGTH_SHORT).show()
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