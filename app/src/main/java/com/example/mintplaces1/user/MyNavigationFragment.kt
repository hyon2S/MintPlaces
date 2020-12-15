package com.example.mintplaces1.user

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.mintplaces1.R
import com.firebase.ui.auth.AuthUI
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
        btn_change_username.setOnClickListener { changeUsername() }
    }

    // 화면이 다시 보일 때마다 멤버 모드를 새로고침
    override fun onResume() {
        super.onResume()
        setMemberMode()
    }

    private fun signIn() {
        Log.d(TAG, "signIn()")
        val user = FirebaseUtil.getUser()

        // 네트워크 연결되어있는지 확인부터 하기

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
                    // 로그아웃되었다고 메시지 띄움.
                    setMemberMode()
                }
    }

    private fun changeUsername() {
        startActivity(Intent(requireActivity(), UsernameActivity::class.java))
    }

    // user가 회원인지 비회원인지에 따라 로그인/로그아웃 버튼 & 비회원/닉네임 이 보이게 함
    private fun setMemberMode() {
        Log.d(TAG, "setMemberMode()")
        val user = FirebaseUtil.getUser()

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
        private const val TAG = "MyLogMyNavFrag"
        @JvmStatic
        fun newInstance() = MyNavigationFragment()
    }
}