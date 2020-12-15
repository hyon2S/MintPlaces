package com.example.mintplaces1

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_map_buttons.*

/*
* 지도 하단에 있는 버튼을 관리.
* */
class MapButtonsFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_map_buttons, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btn_add_store.setOnClickListener {
            Log.d(TAG, "플로팅버튼 클릭")
        }
    }

    companion object {
        private const val TAG = "MyLogMapBtnFrag"

        @JvmStatic fun newInstance() = MapButtonsFragment()
    }
}