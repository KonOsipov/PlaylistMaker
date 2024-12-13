package com.example.playlistmaker.domain.api

interface SettingsInteractor {
    fun getSaved(): Boolean
    fun change()
}