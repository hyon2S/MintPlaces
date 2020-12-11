package com.example.mintplaces1

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

// user가 회원인지 비회원인지에 따라 로그인/로그아웃 버튼 & 비회원/닉네임 이 보이게 할 것.
class MyNavigationFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_my_navigation, container, false)
    }

    companion object {
        @JvmStatic
        fun newInstance() = MyNavigationFragment()
    }
}