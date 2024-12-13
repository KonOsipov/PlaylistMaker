package com.example.playlistmaker.creator

import android.content.SharedPreferences
import com.example.playlistmaker.data.network.RetrofitNetworkClient
import com.example.playlistmaker.data.network.TrackRepositoryImpl
import com.example.playlistmaker.data.sharedPref.SettingsManager
import com.example.playlistmaker.data.sharedPref.SettingsSharedPrefsImpl
import com.example.playlistmaker.data.sharedPref.SharedPrefsManager
import com.example.playlistmaker.data.sharedPref.TrackSharedPrefsImpl
import com.example.playlistmaker.domain.api.SettingsInteractor
import com.example.playlistmaker.domain.api.SettingsSharedPref
import com.example.playlistmaker.domain.api.TrackRepository
import com.example.playlistmaker.domain.api.TrackSharedPref
import com.example.playlistmaker.domain.api.TracksInteractor
import com.example.playlistmaker.domain.impl.SettingsInteractorImpl
import com.example.playlistmaker.domain.impl.TrackInteractorImpl

object Creator {
    private fun getTracksRepository(): TrackRepository {
        return TrackRepositoryImpl(RetrofitNetworkClient())
    }

    private fun getTrackSharedPrefs(sharedPreferences: SharedPreferences): TrackSharedPref {
        return TrackSharedPrefsImpl(SharedPrefsManager(sharedPreferences))
    }

    private fun getSettingsSharedPrefs(sharedPreferences: SharedPreferences): SettingsSharedPref {
        return SettingsSharedPrefsImpl(SettingsManager(sharedPreferences))
    }

    fun provideTracksInteractor(sharedPreferences: SharedPreferences): TracksInteractor {
        return TrackInteractorImpl(getTracksRepository(), getTrackSharedPrefs(sharedPreferences))
    }

    fun provideSettingsInteractor(sharedPreferences: SharedPreferences): SettingsInteractor {
        return SettingsInteractorImpl(getSettingsSharedPrefs(sharedPreferences))
    }

}