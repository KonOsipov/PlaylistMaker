package com.example.playlistmaker

import android.content.SharedPreferences
import android.util.Log
import com.google.gson.Gson
import android.content.Context
import androidx.core.content.edit
import com.google.gson.reflect.TypeToken

class SearchHistory(private val sharedPreferences: SharedPreferences) {

    private var savedTracks = mutableListOf<Track>()

    fun getSaved(): SharedPrefsHistory {

        val json =
            sharedPreferences.getString(HISTORY_KEY, null) ?: return SharedPrefsHistory(
                ArrayList()
            )
        savedTracks = if (json != "") {
            Gson().fromJson<ArrayList<Track>>(
                json,
                object : TypeToken<ArrayList<Track>>() {}.type
            )
        } else {
            ArrayList<Track>()
        }
        return SharedPrefsHistory(savedTracks)
    }


    fun addTrackToHistory(track: Track) //добавление треков в SP
    {
        savedTracks = getSaved().savedTracks.toMutableList()

        val index: Int = savedTracks.indexOf(track)

        if (index >= 0) savedTracks.removeAt(index)

        savedTracks.add(0, track)

        if (savedTracks.size > 10) savedTracks.removeAt(10)

        val json = Gson().toJson(savedTracks)
        sharedPreferences.edit {
            putString(HISTORY_KEY, json)
        }

    }

    fun clean() {
        savedTracks.clear()
        sharedPreferences
            .edit()
            .putString(HISTORY_KEY, "")
            .apply()
    }

    private companion object {
        const val HISTORY_KEY = "key"
    }

}