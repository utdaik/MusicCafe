package com.example.cafemusicchange.reponsitory

import com.example.cafemusicchange.api.JamendoTrack
import com.example.cafemusicchange.local.*
import com.example.cafemusicchange.repository.JamendoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.withContext


import javax.inject.Inject

class MusicRepository @Inject constructor(
    private val appDao: AppDao,
    private val jamendoApiService: JamendoRepository
) {

    // ======================== USER ========================

    /** Thêm user mới */
    suspend fun insertUser(user: User) = appDao.insertUser(user)

    /** Lấy thông tin user theo ID */
    suspend fun getUserById(userId: Long) = appDao.getUserById(userId)

    suspend fun getUserId(email: String) = appDao.getUserId(email)

    /** Kiểm tra đăng nhập */
    suspend fun login(email: String, password: String) = appDao.login(email, password)

    /** Kiểm tra user có tồn tại không */
    suspend fun isUserExists(email: String) = appDao.isUserExists(email)

    // ======================== PLAYLIST ========================

    /** Thêm playlist mới */
    //suspend fun insertPlaylist(playlist: Playlist) = appDao.insertPlaylist(playlist)

    suspend fun addPlaylist(name: String, userId: Long, imagePath: String?) {
        val playlist = Playlist(name = name, userOwnerId = userId, albumImage = imagePath)
        appDao.insertPlaylist(playlist)
    }

//    suspend fun updatePlaylistImage(playlistId: Long, imagePath: String) {
//        appDao.updatePlaylistImage(playlistId, imagePath)
//    }




    /** Lấy danh sách playlist của user */
    suspend fun getUserPlaylists(userId: Long): Flow<List<Playlist>> = flow {
        emit(appDao.getUserPlaylists(userId))
    }


    /** Xóa playlist */
    suspend fun deletePlaylist(playlistId: Long, userId: Long) {
        appDao.clearSongsInPlaylist(playlistId, userId) // Xóa toàn bộ bài hát trong playlist trước
        appDao.deletePlaylist(playlistId, userId) // Sau đó xóa playlist
    }

    /** Kiểm tra playlist có tồn tại không */
    suspend fun isPlaylistExists(playlistId: Long, userId: Long) = appDao.isPlaylistExists(playlistId, userId)

    // ======================== PLAYLIST SONG ========================

    /** Thêm bài hát vào playlist nếu chưa có */
    suspend fun addSongToPlaylist(playlistId: Long, songId: Long, userId: Long) {
        val exists = appDao.isSongInPlaylist(playlistId, songId, userId)
        if (!exists) {
            appDao.addSongToPlaylist(PlaylistSong(playlistId, songId, userId))
        }
    }

    /** Lấy danh sách bài hát trong playlist */
    suspend fun getPlaylistSongs(playlistId: Long, userId: Long): List<JamendoTrack> {
        val songIds = appDao.getSongIdsInPlaylist(playlistId, userId) // Lấy danh sách songId
        return songIds.mapNotNull { songId ->
            val response = jamendoApiService.getTrackById(songId.toString())
            if (response.isSuccessful) {
                response.body()?.tracks?.firstOrNull()
            } else {
                null
            }
        }
    }

    /** Xóa bài hát khỏi playlist */
    suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long, userId: Long) =
        appDao.removeSongFromPlaylist(playlistId, songId, userId)


    // ======================== FAVORITE ========================
    fun isSongFavoriteFlow(songId: Long, userId: Long): Flow<Boolean> =
        appDao.isSongFavoriteFlow(songId, userId)

    suspend fun toggleFavoriteSong(songId: Long, userId: Long) = withContext(Dispatchers.IO) {
        val currentlyFav = appDao.isSongFavoriteFlow(songId, userId).first()
        if (currentlyFav) appDao.removeFavorite(songId, userId)
        else              appDao.addFavorite(FavoriteSong(songId, userId))
    }

    /** Lấy danh sách JamendoTrack đã favorite */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun getFavoriteTracks(userId: Long): Flow<List<JamendoTrack>> =
        appDao.getFavoriteIdsFlow(userId)      // Flow<List<Long>>
            .flatMapLatest { ids ->
                flow {
                    // nếu không có id thì trả về list rỗng ngay
                    if (ids.isEmpty()) {
                        emit(emptyList())
                        return@flow
                    }

                    // khởi tạo một coroutineScope để chạy song song
                    val tracks = coroutineScope {
                        ids.map { id ->
                            async(Dispatchers.IO) {
                                jamendoApiService
                                    .getTrackById(id.toString())
                                    .takeIf { it.isSuccessful }
                                    ?.body()
                                    ?.tracks
                                    ?.firstOrNull()
                            }
                        }.awaitAll()
                    }
                    // lọc null và emit
                    emit(tracks.filterNotNull())
                }
            }

    // ======================== DOWNLOADED ========================

    // Flow báo trạng thái đã tải hay chưa
    fun isSongDownloadedFlow(songId: Long): Flow<Boolean> =
        appDao.isSongDownloadedFlow(songId)

    // Toggle download & lưu metadata
    suspend fun toggleDownloaded(track: JamendoTrack, filePath: String) = withContext(Dispatchers.IO) {
        val exists = appDao.isSongDownloadedFlow(track.id).first()
        if (exists) {
            appDao.removeDownloaded(track.id)
        } else {
            appDao.addDownloaded(DownloadedSong(
                songId     = track.id,
                title      = track.title,
                artist     = track.artist,
                albumImage = track.albumImage,
                filePath   = filePath
            ))
        }
    }

    // Lấy list offline dưới dạng JamendoTrack
    fun getDownloadedSongs(): Flow<List<DownloadedSong>> =
        appDao.getAllDownloaded()


    // ======================== HISTORY ========================

    /** Ghi nhận bài vừa phát */
    suspend fun markPlayed(songId: Long, userId: Long) = withContext(Dispatchers.IO) {
        appDao.deleteHistory(songId, userId)
        appDao.insertHistory(HistorySong(songId, userId))
    }

    /** Lấy danh sách 10 bài nghe gần đây nhất → JamendoTrack */
    fun getRecentHistoryTracks(userId: Long, limit: Int = 10): Flow<List<JamendoTrack>> = flow {
        val ids = appDao.getRecentHistoryIds(userId, limit)
        emit(ids)
    }.mapLatest { ids ->
        if (ids.isEmpty()) emptyList()
        else {
            val csv = ids.joinToString(",")
            jamendoApiService.getTrackById(csv).body()?.tracks ?: emptyList()
        }
    }

}

