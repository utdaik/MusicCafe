package com.example.cafemusicchange.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {

    // ======================== USER DAO ========================

    /** Thêm user mới, nếu trùng ID thì cập nhật */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    /** Lấy thông tin user theo ID */
    @Query("SELECT * FROM user WHERE userId = :userId")
    suspend fun getUserById(userId: Long): User?

    /** Kiểm tra đăng nhập bằng email và password */
    @Query("SELECT * FROM user WHERE email = :email AND password = :password")
    suspend fun login(email: String, password: String): User?

    /** Kiểm tra user có tồn tại không */
    @Query("SELECT EXISTS (SELECT 1 FROM user WHERE email = :email)")
    suspend fun isUserExists(email: String): Boolean

    @Query("SELECT userId FROM user WHERE email = :email")
    suspend fun getUserId(email: String): Long?


    // ======================== PLAYLIST DAO ========================

    /** Thêm hoặc cập nhật playlist */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: Playlist)


    /** Lấy danh sách playlist theo user */
    @Query("SELECT * FROM playlist WHERE userOwnerId = :userId ORDER BY createdAt DESC")
    suspend fun getUserPlaylists(userId: Long): List<Playlist>

    /** Xóa playlist theo ID */
    @Query("DELETE FROM playlist WHERE playlistId = :playlistId AND userOwnerId = :userId")
    suspend fun deletePlaylist(playlistId: Long, userId: Long)

    @Query("UPDATE playlist SET albumImage = :imagePath WHERE playlistId = :playlistId")
    suspend fun updatePlaylistImage(playlistId: Long, imagePath: String)

    /** Kiểm tra xem playlist có tồn tại không */
    @Query("SELECT COUNT(*) > 0 FROM playlist WHERE playlistId = :playlistId AND userOwnerId = :userId")
    suspend fun isPlaylistExists(playlistId: Long, userId: Long): Boolean

    // ======================== PLAYLIST SONG DAO ========================

    /** Thêm bài hát vào playlist, nếu bài hát đã tồn tại thì bỏ qua */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addSongToPlaylist(playlistSong: PlaylistSong)

    /** Lấy danh sách bài hát trong một playlist cùng thông tin chi tiết */
    @Query("SELECT songId FROM playlist_song WHERE playlistId = :playlistId AND userId = :userId")
    suspend fun getSongIdsInPlaylist(playlistId: Long, userId: Long): List<Long>


    /** Kiểm tra xem một bài hát đã tồn tại trong playlist chưa */
    @Query("""
        SELECT COUNT(*) > 0 
        FROM playlist_song 
        WHERE playlistId = :playlistId AND songId = :songId AND userId = :userId
    """)
    suspend fun isSongInPlaylist(playlistId: Long, songId: Long, userId: Long): Boolean

    /** Xóa bài hát khỏi playlist */
    @Query("DELETE FROM playlist_song WHERE playlistId = :playlistId AND songId = :songId AND userId = :userId")
    suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long, userId: Long)

    /** Xóa toàn bộ bài hát khỏi playlist khi playlist bị xóa */
    @Query("DELETE FROM playlist_song WHERE playlistId = :playlistId AND userId = :userId")
    suspend fun clearSongsInPlaylist(playlistId: Long, userId: Long)

    // ======================== FAVORITE ========================

    /** Flow báo trạng thái đã favorite hay chưa */
    @Query("SELECT EXISTS(SELECT 1 FROM favorite_song WHERE songId = :songId AND userId = :userId)")
    fun isSongFavoriteFlow(songId: Long, userId: Long): Flow<Boolean>

    /** Thêm 1 bài vào bảng favorite */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addFavorite(favorite: FavoriteSong)

    /** Xóa 1 bài khỏi bảng favorite */
    @Query("DELETE FROM favorite_song WHERE songId = :songId AND userId = :userId")
    suspend fun removeFavorite(songId: Long, userId: Long)

    /** Lấy list songId đã favorite để chuyển thành JamendoTrack */
    @Query("SELECT songId FROM favorite_song WHERE userId = :userId")
    fun getFavoriteIdsFlow(userId: Long): Flow<List<Long>>


    // ======================== DOWNLOADED ========================

    // Lấy toàn bộ danh sách đã download
    @Query("SELECT * FROM downloaded_song")
    fun getAllDownloaded(): Flow<List<DownloadedSong>>

    // Kiểm tra xem đã download chưa
    @Query("SELECT EXISTS(SELECT 1 FROM downloaded_song WHERE songId = :songId)")
    fun isSongDownloadedFlow(songId: Long): Flow<Boolean>

    // Thêm / Xóa bản ghi download
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addDownloaded(downloaded: DownloadedSong)

    @Query("DELETE FROM downloaded_song WHERE songId = :songId")
    suspend fun removeDownloaded(songId: Long)

    // ======================== HISTORY ========================

    /** Xóa bỏ record cũ trước khi insert vào để RowId tăng → lấy mới nhất lên đầu */
    @Query("DELETE FROM history_song WHERE songId = :songId AND userId = :userId")
    suspend fun deleteHistory(songId: Long, userId: Long)

    /** Thêm record mới (với REPLACE sẽ ghi đè nếu có) */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: HistorySong)

    /** Lấy list songId nghe gần đây, RowId DESC → mới nhất lên đầu, giới hạn limit */
    @Query("""
    SELECT songId 
    FROM history_song 
    WHERE userId = :userId 
    ORDER BY rowid DESC 
    LIMIT :limit
  """)
    suspend fun getRecentHistoryIds(userId: Long, limit: Int): List<Long>

}
