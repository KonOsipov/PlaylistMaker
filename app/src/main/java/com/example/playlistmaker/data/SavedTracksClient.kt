package com.example.playlistmaker.data

import com.example.playlistmaker.data.dto.SharedPrefsHistory

interface SavedTracksClient {
    fun getSaved(): SharedPrefsHistory
    fun save(dto: Any)
    fun clean()
}