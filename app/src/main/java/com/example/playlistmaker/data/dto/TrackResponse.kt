package com.example.playlistmaker.data.dto
class TrackResponse(
    val results: List<TrackDto>, resultCode: Int
) : Response(resultCode)