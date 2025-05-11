package com.example.cafemusicchange.api

import com.example.cafemusicchange.BuildConfig
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface JamendoApiService {

    // Tìm kiếm bài hát theo tên
    @GET("tracks/")
    suspend fun searchTracks(
        @Query("client_id") apiKey: String = BuildConfig.JAMENDO_API_KEY,
        @Query("format") format: String = "json",
        @Query("search") query: String,
        @Query("limit") limit: Int = 20 // Giới hạn số bài hát trả về
    ): Response<JamendoTrackResponse>

    // Lấy thông tin chi tiết bài hát dựa vào ID
    @GET("tracks/")
    suspend fun getTrackDetails(
        @Query("client_id") apiKey: String = BuildConfig.JAMENDO_API_KEY,
        @Query("format") format: String = "json",
        @Query("id") trackId: String
    ): Response<JamendoTrackResponse>

}
