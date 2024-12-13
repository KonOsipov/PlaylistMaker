package com.example.playlistmaker.presentation

import android.app.Application
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate

const val THEME = "theme"

class App : Application() {

    private lateinit var sharedPreferences: SharedPreferences
    var darkTheme = false

    override fun onCreate() {
        super.onCreate()
        sharedPreferences = getSharedPreferences(THEME, MODE_PRIVATE)

        if (!sharedPreferences.getString(THEME, "").equals("")) {

            if (sharedPreferences.getString(THEME, "").equals("MODE_NIGHT_YES")) {
                darkTheme = true
                switchTheme(darkTheme)
            } else {
                darkTheme = false
                switchTheme(darkTheme)
            }
        }

    }

    fun switchTheme(darkThemeEnabled: Boolean) {

        darkTheme = darkThemeEnabled
        AppCompatDelegate.setDefaultNightMode(
            if (darkThemeEnabled) {
                AppCompatDelegate.MODE_NIGHT_YES
            } else {
                AppCompatDelegate.MODE_NIGHT_NO
            }
        )

        changeSharePref()
    }

    private fun changeSharePref() {

        if (darkTheme) {
            sharedPreferences.edit().putString(THEME, "MODE_NIGHT_YES").apply()
        } else {
            sharedPreferences.edit().putString(THEME, "MODE_NIGHT_NO").apply()
        }
    }

}