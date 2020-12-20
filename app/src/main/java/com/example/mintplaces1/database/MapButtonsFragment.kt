package com.example.mintplaces1.database

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.example.mintplaces1.R
import com.example.mintplaces1.exception.NullUserException
import com.example.mintplaces1.exception.PlaceInfoNotExistException
import com.example.mintplaces1.user.FirebaseUtil
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.fragment_map_buttons.*
import java.lang.Exception

/*
* 지도 하단에 있는 버튼을 관리.
* */
class MapButtonsFragment : Fragment() {
    private val databaseViewModel by lazy { ViewModelProvider(requireActivity()).get(DatabaseViewModel::class.java) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_map_buttons, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btn_add_store.setOnClickListener {
            addStore()
        }
    }

    // db에 새 매장 정보를 등록
    private fun addStore() {
        Log.d(TAG, "addStore()")

        // 진짜 매장 등록 할 건지 물어보기. yes면 계속 진행.

        try {
            val user: FirebaseUser = FirebaseUtil.getUser() ?: throw NullUserException()

            databaseViewModel.addStore(user) // throws PlaceInfoNotExistException
            Log.d(TAG, "매장 등록 성공")
            // 매장 등록 됐다고 메시지 띄우기
            // 등록한 매장을 다시 등록할 일은 없으니 편의상 저장한 place 정보를 삭제하기
            databaseViewModel.initPlaceInfo()
        } catch (e: Exception) {
            if (e is NullUserException || e is PlaceInfoNotExistException) {
                // 둘 다 message로 확인창 띄우기 (확인버튼 눌러야만 없어지는 그거)
            }
            Log.e(TAG, "매장 추가 실패", e)
        }
    }

    companion object {
        private const val TAG = "MyLogMapBtnFrag"

        @JvmStatic fun newInstance() = MapButtonsFragment()
    }
}