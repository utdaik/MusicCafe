package com.example.cafemusicchange.repository

import com.example.cafemusicchange.api.JamendoApiService
import com.example.cafemusicchange.api.JamendoTrackResponse
import retrofit2.Response
import javax.inject.Inject

open class JamendoRepository @Inject constructor(
    private val apiService: JamendoApiService
) {
    suspend fun searchTracks(query: String): Response<JamendoTrackResponse> {
        return apiService.searchTracks(query = query)
    }

    suspend fun getTrackById(trackId: String): Response<JamendoTrackResponse> {
        return apiService.getTrackDetails(trackId = trackId)
    }
}
