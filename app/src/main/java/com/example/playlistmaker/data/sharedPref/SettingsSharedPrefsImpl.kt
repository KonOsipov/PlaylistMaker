package com.example.playlistmaker.data.sharedPref

import com.example.playlistmaker.domain.api.SettingsSharedPref

class SettingsSharedPrefsImpl(private val settingsManager: SettingsManager) : SettingsSharedPref {
    override fun getSaved(): Boolean {
        return settingsManager.getSaved().savedTheme
    }

    override fun change() {
        settingsManager.change()
    }
}