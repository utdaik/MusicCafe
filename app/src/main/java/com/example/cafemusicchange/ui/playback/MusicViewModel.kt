package com.example.cafemusicchange.ui.playback

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.example.cafemusicchange.MainViewModel
import com.example.cafemusicchange.api.JamendoTrack
import com.example.cafemusicchange.reponsitory.MusicRepository
import com.example.cafemusicchange.repository.JamendoRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File

// Định nghĩa trạng thái trình phát nhạc
data class PlaybackState(
    val isPlaying: Boolean = false,
    val currentPosition: Long = 0L,
    val duration: Long = 0L,
    val isShuffle: Boolean = false,
    val isRepeat: Boolean = false
)

@HiltViewModel
class MusicPlayer @Inject constructor(
    private val exoPlayer: ExoPlayer,
    private val db : MusicRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlaybackState())
    val uiState: StateFlow<PlaybackState> = _uiState.asStateFlow()


    private val _currentQueue =
        MutableStateFlow<List<JamendoTrack>>(emptyList()) // 🔥 Danh sách nhạc hiện tại
//    val currentQueue: StateFlow<List<JamendoTrack>> = _currentQueue.asStateFlow()

    private val _currentTrackIndex =
        MutableStateFlow(0) // 🔥 Vị trí bài hát hiện tại trong danh sách
//    val currentTrackIndex: StateFlow<Int> = _currentTrackIndex.asStateFlow()

    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite.asStateFlow()

    private val _currentTrack = MutableStateFlow<JamendoTrack?>(null) // 🔥 Bài hát hiện tại
    val tracks = _currentTrack.asStateFlow()

    private val _isDownloaded = MutableStateFlow(false)
    val isDownloaded: StateFlow<Boolean> = _isDownloaded.asStateFlow()


    init {
        observePlayer()
    }

    /**
     * Lấy thông tin bài hát từ Deezer API, tránh gọi lại API khi đã có dữ liệu.
     */
//    private var currentTrackId: String? = null

//    fun playTrack(trackId: String) {
//        currentTrackId = trackId
//        viewModelScope.launch {
//            val track = _currentQueue.value.find { it.id.toString() == trackId }
//            if (track != null) {
//                _currentTrack.value = track
//                setMediaSource(track.audioUrl ?: return@launch)
//            } else {
//                // Nếu không có trong danh sách, gọi API như trước
//                val response = repository.getTrackById(trackId)
//                if (response.isSuccessful) {
//                    response.body()?.tracks?.firstOrNull()?.let {
//                        _currentTrack.value = it
//                        setMediaSource(it.audioUrl ?: return@launch)
//                    }
//                }
//            }
//        }
//    }

    // khi chuyển track (vd trong setQueue), gọi:
    private fun refreshDownloaded(trackId: Long) {
        viewModelScope.launch {
            db.isSongDownloadedFlow(trackId)
                .collect { _isDownloaded.value = it }
        }
    }

    fun setQueue(tracksJson: String, startIndex: Int, userId: Long) {
        viewModelScope.launch {
            try {
                val tracks: List<JamendoTrack> =
                    Gson().fromJson(tracksJson, object : TypeToken<List<JamendoTrack>>() {}.type)
                _currentQueue.value = tracks
                _currentTrackIndex.value = startIndex
                _currentTrack.value = tracks[startIndex] // Cập nhật bài hát hiện tại
                refreshFavorite(tracks[startIndex].id, userId)
                refreshDownloaded(tracks[startIndex].id)
                setMediaSource(tracks[startIndex].audioUrl ?: "") // Phát bài hát đầu tiên
            } catch (e: Exception) {
                Log.e("MusicPlayer", "Lỗi khi thiết lập danh sách phát", e)
            }
        }
    }

    // user click download
    fun onDownloadClicked(track: JamendoTrack, context: Context) {
        viewModelScope.launch {
            // 1) download file
            val path = downloadToLocalFile(context, track.audioUrl!!, track.id)
            // 2) ghi metadata
            db.toggleDownloaded(track, path)
            // 3) refresh state ngay
            _isDownloaded.value = true
        }
    }


    fun addSongToPlaylist(playlistId: Long, trackId: Long, userId: Long) {
        viewModelScope.launch {
            db.addSongToPlaylist(playlistId = playlistId, songId = trackId, userId = userId)
        }
    }


    /**
     * Theo dõi trạng thái của ExoPlayer và cập nhật vào StateFlow.
     * Giảm tần suất cập nhật để tránh recompose liên tục.
     */
    private fun observePlayer() {
        viewModelScope.launch {
            flow {
                while (true) {
                    val newState = PlaybackState(
                        isPlaying = exoPlayer.isPlaying,
                        currentPosition = exoPlayer.currentPosition,
                        duration = exoPlayer.duration.takeIf { it > 0 } ?: _uiState.value.duration,
                        isShuffle = exoPlayer.shuffleModeEnabled,
                        isRepeat = exoPlayer.repeatMode == ExoPlayer.REPEAT_MODE_ONE
                    )
                    emit(newState)
                    delay(1000)
                }
            }
                .distinctUntilChanged()
                .collect { newState ->
                    _uiState.value = newState
                }
        }
    }


    /**
     * Cập nhật nguồn nhạc cho ExoPlayer và phát nhạc ngay lập tức.
     */
    private fun setMediaSource(url: String) {
        val mediaItem = MediaItem.fromUri(Uri.parse(url))
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true  // Đảm bảo phát bài hát ngay lập tức
    }

    /**
     * Bật/tắt phát nhạc. Nếu bài hát chưa được tải, tự động tải và phát.
     */
    fun playPause() {
        if (_currentQueue.value.isEmpty()) {
            Log.e("MusicPlayer", "Danh sách phát trống!")
            return
        }

        val currentTrack = _currentQueue.value[_currentTrackIndex.value]
        val trackUrl = currentTrack.audioUrl ?: ""

        // Nếu chưa có bài hát nào được tải, thiết lập nguồn phát
        if (exoPlayer.currentMediaItem == null) {
            setMediaSource(trackUrl)
        } else {
            // Nếu ExoPlayer đang phát, thì tạm dừng, ngược lại phát tiếp
            if (exoPlayer.isPlaying) {
                exoPlayer.pause()
            } else {
                exoPlayer.playWhenReady = true // Đảm bảo phát lại khi sẵn sàng
            }
        }

        _uiState.update { it.copy(isPlaying = exoPlayer.isPlaying) }
    }


    fun playNext() {
        viewModelScope.launch {
            val nextIndex = (_currentTrackIndex.value + 1) % _currentQueue.value.size
            _currentTrackIndex.value = nextIndex
            _currentTrack.value = _currentQueue.value[nextIndex]
            setMediaSource(_currentQueue.value[nextIndex].audioUrl ?: "")
        }
    }

    fun playPrevious() {
        viewModelScope.launch {
            val prevIndex =
                if (_currentTrackIndex.value - 1 < 0) _currentQueue.value.size - 1 else _currentTrackIndex.value - 1
            _currentTrackIndex.value = prevIndex
            _currentTrack.value = _currentQueue.value[prevIndex]
            setMediaSource(_currentQueue.value[prevIndex].audioUrl ?: "")
        }
    }


    fun toggleShuffle() {
        exoPlayer.shuffleModeEnabled = !exoPlayer.shuffleModeEnabled
        _uiState.update { it.copy(isShuffle = exoPlayer.shuffleModeEnabled) }
    }

    fun toggleRepeat() {
        val newRepeatMode = if (exoPlayer.repeatMode == ExoPlayer.REPEAT_MODE_ONE) {
            ExoPlayer.REPEAT_MODE_OFF
        } else {
            ExoPlayer.REPEAT_MODE_ONE
        }
        exoPlayer.repeatMode = newRepeatMode
        _uiState.update { it.copy(isRepeat = newRepeatMode == ExoPlayer.REPEAT_MODE_ONE) }
    }


    fun seekTo(position: Long) {
        exoPlayer.seekTo(position)
    }


    private fun refreshFavorite(songId: Long, userId: Long) {
        viewModelScope.launch {
            db.isSongFavoriteFlow(songId, userId)
                .collect { fav -> _isFavorite.value = fav }
        }
    }

    /**
     * Toggle favorite: nếu đã có thì xóa, chưa có thì thêm
     */
    fun toggleFavorite(songId: Long, userId: Long) {
        viewModelScope.launch {
            db.toggleFavoriteSong(songId, userId)
            refreshFavorite(songId, userId)
        }
    }

    private suspend fun downloadToLocalFile(
        context: Context,
        url: String,
        trackId: Long
    ): String = withContext(Dispatchers.IO) {
        val downloadsDir = File(context.filesDir, "downloads").apply { mkdirs() }
        val outFile = File(downloadsDir, "track_$trackId.mp3")
        val client = OkHttpClient()
        val req = Request.Builder().url(url).build()
        client.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) throw RuntimeException("Download failed: ${resp.code}")
            outFile.outputStream().use { fos ->
                resp.body!!.byteStream().copyTo(fos)
            }
        }
        outFile.absolutePath
    }
}

