package com.example.cafemusicchange.api

import com.google.gson.annotations.SerializedName

data class JamendoTrackResponse(
    @SerializedName("results") val tracks: List<JamendoTrack>
)

data class JamendoTrack(
    @SerializedName("id") val id: Long,
    @SerializedName("name") val title: String,
    @SerializedName("artist_name") val artist: String,
    @SerializedName("audio") val audioUrl: String, // Link phát nhạc
    @SerializedName("album_image") val albumImage: String // Ảnh album
)

