package com.example.cafemusicchange.di

import android.content.Context
import androidx.media3.exoplayer.ExoPlayer
import androidx.room.Room
import com.example.cafemusicchange.api.JamendoApiService
import com.example.cafemusicchange.local.AppDao
import com.example.cafemusicchange.local.AppDatabase
import com.example.cafemusicchange.repository.JamendoRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    /** Cung cấp instance của Room Database */
    @Provides
    @Singleton
     fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "music_app_database"
        ).fallbackToDestructiveMigration()
            .build()
    }

    /** Cung cấp instance của DAO */
    @Provides
    fun provideAppDao(database: AppDatabase): AppDao {
        return database.appDao()
    }

    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.jamendo.com/v3.0/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideJamendoApiService(retrofit: Retrofit): JamendoApiService {
        return retrofit.create(JamendoApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideJamendoRepository(apiService: JamendoApiService): JamendoRepository {
        return JamendoRepository(apiService)
    }


    @Provides
    @Singleton
    fun provideExoPlayer(@ApplicationContext context: Context): ExoPlayer {
        return ExoPlayer.Builder(context).build()
    }
}
