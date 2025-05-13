package com.example.cafemusicchange.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user")
data class User(
    @PrimaryKey(autoGenerate = true) val userId: Long = 0,
    val username: String,
    val email: String,
    val password: String
)

@Entity(tableName = "playlist")
data class Playlist(
    @PrimaryKey(autoGenerate = true) val playlistId: Long = 0,
    val name: String,
    val userOwnerId: Long,  // Playlist thuộc về user nào
    val albumImage: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "playlist_song", primaryKeys = ["playlistId", "songId", "userId"])
data class PlaylistSong(
    val playlistId: Long,
    val songId: Long,  // Chỉ lưu songId, không lưu dữ liệu bài hát
    val userId: Long   // Bài hát này thuộc về user nào
)

@Entity(tableName = "history_song", primaryKeys = ["songId", "userId"])
data class HistorySong(
    val songId: Long,
    val userId: Long,  // Lịch sử phát nhạc của từng user riêng biệt
)

@Entity(
    tableName = "favorite_song",
    primaryKeys = ["songId", "userId"]
)
data class FavoriteSong(
    val songId: Long,
    val userId: Long
)

@Entity(tableName = "downloaded_song", primaryKeys = ["songId"])
data class DownloadedSong(
    val songId: Long,
    val title: String,
    val artist: String,
    val albumImage: String,
    val filePath: String
)


