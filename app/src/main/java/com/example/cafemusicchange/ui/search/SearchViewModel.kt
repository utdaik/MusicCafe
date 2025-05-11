package com.example.cafemusicchange.ui.search

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cafemusicchange.api.JamendoTrack
import com.example.cafemusicchange.repository.JamendoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(private val repository: JamendoRepository) : ViewModel() {

    private val _tracks = MutableStateFlow<List<JamendoTrack>>(emptyList())
    val track = _tracks.asStateFlow()

    private var _searchQuery = MutableStateFlow("")
    var searchQuery = _searchQuery.asStateFlow()

    fun updateSearchQuery(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun searchTracks(query: String) {
        if (query.isBlank()) return // Không tìm nếu query rỗng

        viewModelScope.launch {
            try {
                val response = repository.searchTracks(query)
                if (response.isSuccessful) {
                    _tracks.value = response.body()?.tracks ?: emptyList()
                    Log.d("SearchViewModel", "Tìm thấy ${_tracks.value.size} bài hát cho từ khóa '$query'")
                } else {
                    Log.e("SearchViewModel", "Lỗi API: ${response.code()} - ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("SearchViewModel", "Lỗi khi tìm kiếm", e)
            }
        }
    }
}
