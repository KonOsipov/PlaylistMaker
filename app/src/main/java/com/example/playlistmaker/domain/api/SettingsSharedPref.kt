package com.example.playlistmaker.domain.api

interface SettingsSharedPref {
    fun getSaved(): Boolean
    fun change()
}