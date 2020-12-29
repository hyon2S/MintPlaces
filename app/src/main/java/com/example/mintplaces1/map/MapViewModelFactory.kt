package com.example.mintplaces1.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mintplaces1.database.DatabaseViewModel

class MapViewModelFactory(private val databaseViewModel: DatabaseViewModel) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(MapViewModel::class.java)) {
            MapViewModel(databaseViewModel) as T
        } else {
            throw IllegalArgumentException()
        }
    }
}