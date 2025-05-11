package com.example.cafemusicchange.ui.playlist

import androidx.annotation.OptIn
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import com.example.cafemusicchange.api.JamendoTrack
import com.example.cafemusicchange.local.Playlist
import com.example.cafemusicchange.local.PlaylistSong
import com.example.cafemusicchange.reponsitory.MusicRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlaylistViewModel @Inject constructor(private val repository: MusicRepository) : ViewModel() {

    private val _userPlaylists = MutableStateFlow<List<Playlist>>(emptyList())
    val userPlaylists: StateFlow<List<Playlist>> = _userPlaylists.asStateFlow()

    private val _playlistSongs = MutableStateFlow<List<JamendoTrack>>(emptyList())
    val playlistSongs: StateFlow<List<JamendoTrack>> = _playlistSongs.asStateFlow()

    fun loadUserPlaylists(userId: Long) {
        viewModelScope.launch {
            repository.getUserPlaylists(userId).collect { playlists ->
                _userPlaylists.value = playlists
            }
        }
    }

    fun addPlaylist(name: String, userId: Long, imagePath: String?) {
        viewModelScope.launch {
            repository.addPlaylist(name, userId, imagePath) //  Chỉ nhận imagePath
            loadUserPlaylists(userId) //  Load lại danh sách
        }
    }

    fun deletePlaylist(playlistId: Long, userId: Long) {
        viewModelScope.launch {
            repository.deletePlaylist(playlistId,userId)
            loadUserPlaylists(userId) // Load lại danh sách sau khi xóa
        }
    }


    @OptIn(UnstableApi::class)
    fun loadPlaylistSongs(playlistId: Long, userId: Long) {
        viewModelScope.launch {
            try {
                val songs = repository.getPlaylistSongs(playlistId, userId)
                _playlistSongs.value = songs
            } catch (e: Exception) {
                Log.e("PlaylistViewModel", "Lỗi khi lấy danh sách bài hát: ${e.message}")
            }
        }
    }


    fun removeSongFromPlaylist(playlistId: Long, songId: Long, userId: Long) {
        viewModelScope.launch {
            repository.removeSongFromPlaylist(playlistId, songId, userId)
            loadPlaylistSongs(playlistId, userId)  // Load lại danh sách bài hát sau khi xóa
        }
    }

}

