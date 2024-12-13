package com.example.playlistmaker.domain.api

import com.example.playlistmaker.domain.models.Track

interface TrackSharedPref {
    fun getSavedTracks(): List<Track>
    fun saveTrack(track: Track)
    fun cleanHistory()
}