package com.example.mintplaces1.database

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.example.mintplaces1.R
import com.example.mintplaces1.exception.NullUserException
import com.example.mintplaces1.exception.StoreInfoNotExistException
import com.example.mintplaces1.map.MapViewModel
import com.example.mintplaces1.map.MapViewModelFactory
import com.example.mintplaces1.user.FirebaseUtil
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.fragment_map_buttons.*

/*
* 지도 하단에 있는 버튼을 관리.
* */
class MapButtonsFragment : Fragment() {
    private val databaseViewModel by lazy { ViewModelProvider(requireActivity()).get(DatabaseViewModel::class.java) }
    private val mapViewModel by lazy { ViewModelProvider(requireActivity(), MapViewModelFactory(databaseViewModel)).get(MapViewModel::class.java) }

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

        // 카메라가 다른 곳에 가있어도 추가할 매장 위치로 카메라 이동시킴

        // 진짜 매장 등록 할 건지 물어보기. yes면 계속 진행.
        val builder = AlertDialog.Builder(context)
        builder.apply {
            setMessage(getString(R.string.confirm_message_add_store))
            setPositiveButton(getString(R.string.yes)) { _, _ ->
                Log.d(TAG, "매장을 등록합니다.")
                try {
                    val user: FirebaseUser = FirebaseUtil.getUser() ?: throw NullUserException()

                    databaseViewModel.addStore(user) // throws StoreInfoNotExistException
                    Log.d(TAG, "매장 등록 성공")
                    Snackbar.make(requireActivity().findViewById(android.R.id.content), getString(R.string.success_message_add_store), Snackbar.LENGTH_SHORT).show()
                    // 등록한 매장을 다시 등록할 일은 없으니 편의상 저장한 place 정보를 삭제하기
                    databaseViewModel.deletePlaceInfo()
                    mapViewModel.hidePlaceSearchMarker()
                    // 매장 db에서 다시 받아오는 작업 필요?
                } catch (e: Exception) {
                    if (e is NullUserException || e is StoreInfoNotExistException) {
                        Snackbar.make(requireActivity().findViewById(android.R.id.content), e.message!!, Snackbar.LENGTH_SHORT).show()
                    }
                    Log.e(TAG, "매장 추가 실패", e)
                }
            }
            setNegativeButton(getString(R.string.no)) { _, _ ->
                Log.d(TAG, "매장 등록 취소")
                // 아무것도 안 함
            }
        }

        builder.create().show()
    }

    companion object {
        private const val TAG = "MyLogMapBtnFrag"

        @JvmStatic fun newInstance() = MapButtonsFragment()
    }
}