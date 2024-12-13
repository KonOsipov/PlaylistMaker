package com.example.playlistmaker.domain.impl

import com.example.playlistmaker.domain.api.SettingsInteractor
import com.example.playlistmaker.domain.api.SettingsSharedPref

class SettingsInteractorImpl(private val sharedPrefs: SettingsSharedPref) : SettingsInteractor {
    override fun getSaved(): Boolean {
        return sharedPrefs.getSaved()
    }

    override fun change() {
        sharedPrefs.change()
    }

}