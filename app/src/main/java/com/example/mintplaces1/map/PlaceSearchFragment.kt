package com.example.mintplaces1.map

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.example.mintplaces1.R
import com.google.android.gms.common.api.Status
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener

/*
* 장소를 검색할 수 있는 검색창 AutocompleteSupportFragment를 관리.
* 장소를 검색하고 장소 정보를 받아옴.
* */
class PlaceSearchFragment : Fragment() {
    private val mapViewModel by lazy { ViewModelProvider(requireActivity()).get(MapViewModel::class.java) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_place_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 장소 검색창 세팅
        // https://developers.google.com/places/android-sdk/autocomplete
        // childFragmentManager는 onViewCreated에서: https://stackoverflow.com/questions/18935505/where-to-call-getchildfragmentmanager
        val autocompleteSupportFragment = childFragmentManager.findFragmentById(R.id.frg_auto_complete_place_search) as AutocompleteSupportFragment?
            ?: AutocompleteSupportFragment.newInstance().also {
                childFragmentManager.beginTransaction().replace(R.id.frg_auto_complete_place_search, it).commit()
            }
        // place 초기화. 안 하니까 오류생김.
        if (!Places.isInitialized())
            Places.initialize(requireContext(), getString(R.string.google_map_api_key))
        // 장소 이름, 위도경도, 주소 정보를 사용할 예정임.
        autocompleteSupportFragment.setPlaceFields(listOf(Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS))
        autocompleteSupportFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                Log.i(TAG, "Place: ${place.name}, ${place.latLng}, ${place.address}")
                // 검색한 장소를 마커에 표시
                mapViewModel.setMarker(place.name!!, place.latLng!!, place.address!!)
            }
            override fun onError(status: Status) {
                Log.i(TAG, "An error occurred: $status")
            }
        })
    }

    companion object {
        @JvmStatic fun newInstance() = PlaceSearchFragment()

        private const val TAG = "MyLogPlaceSearchFrag"
    }
}