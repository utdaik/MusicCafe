package com.example.cafemusicchange.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [User::class, Playlist::class, PlaylistSong::class, HistorySong::class, FavoriteSong::class, DownloadedSong::class],
    version = 7,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun appDao(): AppDao

}
