package com.example.playlistmaker.data

import com.example.playlistmaker.data.dto.SharedPrefsSettings

interface SavedSettingsClient {
    fun getSaved(): SharedPrefsSettings
    fun change()

}