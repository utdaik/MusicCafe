package com.example.cafemusicchange.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cafemusicchange.api.JamendoApiService
import com.example.cafemusicchange.api.JamendoTrack
import com.example.cafemusicchange.reponsitory.MusicRepository
import com.example.cafemusicchange.repository.JamendoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: JamendoRepository,
    private val db : MusicRepository
) : ViewModel() {

    private val _morning = MutableStateFlow<List<JamendoTrack>>(emptyList())
    val moring = _morning.asStateFlow()

    private val _indie = MutableStateFlow<List<JamendoTrack>>(emptyList())
    val indie = _indie.asStateFlow()

    private val _vintage = MutableStateFlow<List<JamendoTrack>>(emptyList())
    val vintage= _vintage.asStateFlow()

    private val _jazz = MutableStateFlow<List<JamendoTrack>>(emptyList())
    val jazz = _jazz.asStateFlow()

    private val _classic = MutableStateFlow<List<JamendoTrack>>(emptyList())
    val classic = _classic.asStateFlow()

    private val _lofi = MutableStateFlow<List<JamendoTrack>>(emptyList())
    val lofi = _lofi.asStateFlow()

    init {
        fetchTracks("Morning lofi", _morning, "Popular Tracks")
        fetchTracks("Chill House lofi", _indie, "New Releases")
        fetchTracks("Vintage music", _vintage, "Recommended Tracks")
        fetchTracks("jazz lofi", _jazz, "Focus Music")
        fetchTracks("Classical music", _classic, "Relax Music")
        fetchTracks("lofi", _lofi, "Relax Music")
    }
//    private fun getUserId(){
//        viewModelScope.launch {
//            val userId = db.getUserId("james.k.polk@examplepetstore.com")
//        }
//    }

    private fun fetchTracks(query: String, stateFlow: MutableStateFlow<List<JamendoTrack>>, logTag: String) {
        viewModelScope.launch {
            try {
                val response = repository.searchTracks(query)
                if (response.isSuccessful) {
                    stateFlow.value = response.body()?.tracks ?: emptyList()
                    Log.d("HomeViewModel", "$logTag: Tải thành công ${stateFlow.value.size} bài hát")
                } else {
                    Log.e("HomeViewModel", "$logTag: Lỗi API - ${response.code()} - ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "$logTag: Lỗi khi tải bài hát", e)
            }
        }
    }
}
