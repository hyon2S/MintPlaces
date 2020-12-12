package com.example.mintplaces1

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_my_navigation.*
import kotlinx.android.synthetic.main.navigation_my_guest.*
import kotlinx.android.synthetic.main.navigation_my_member.*

class MyNavigationFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_my_navigation, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setMemberMode()

        btn_sign_in_sign_up.setOnClickListener { signIn() }
        btn_sign_out.setOnClickListener { signOut() }
    }

    // 화면이 다시 보일 때마다 멤버 모드를 새로고침
    override fun onResume() {
        super.onResume()
        setMemberMode()
    }

    private fun signIn() {
        Log.d(TAG, "signIn()")
        val user = Firebase.auth.currentUser

        if (user != null) {
            Log.d(TAG, "이미 로그인 되어있습니다.")
        } else {
            startActivity(Intent(requireActivity(), LoginActivity::class.java))
        }
    }

    private fun signOut() {
        Log.d(TAG, "signOut()")
        AuthUI.getInstance().signOut(requireContext())
                .addOnCompleteListener {
                    Log.d(TAG, "로그아웃되었습니다.")
                    setMemberMode()
                }
    }

    // user가 회원인지 비회원인지에 따라 로그인/로그아웃 버튼 & 비회원/닉네임 이 보이게 함
    private fun setMemberMode() {
        Log.d(TAG, "setMemberMode()")
        val user = Firebase.auth.currentUser

        if (user != null) {
            guest.visibility = View.GONE
            member.visibility = View.VISIBLE
            tv_username.text = getString(R.string.username, user.displayName)
        } else {
            guest.visibility = View.VISIBLE
            member.visibility = View.GONE
        }
    }

    companion object {
        const val TAG = "MyNavLog"
        @JvmStatic
        fun newInstance() = MyNavigationFragment()
    }
}