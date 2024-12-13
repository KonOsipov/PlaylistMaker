package com.example.playlistmaker.data.sharedPref

import android.content.SharedPreferences
import androidx.core.content.edit
import com.example.playlistmaker.data.SavedTracksClient
import com.example.playlistmaker.data.dto.SharedPrefsHistory
import com.example.playlistmaker.data.dto.TrackDto
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SharedPrefsManager(private val sharedPreferences: SharedPreferences) : SavedTracksClient {

    private var savedTracks = mutableListOf<TrackDto>()


    override fun getSaved(): SharedPrefsHistory {
        val json =
            sharedPreferences.getString(HISTORY_KEY, null) ?: return SharedPrefsHistory(ArrayList())
        savedTracks = if (json != "") {
            Gson().fromJson<ArrayList<TrackDto>>(
                json,
                object : TypeToken<ArrayList<TrackDto>>() {}.type
            )
        } else {
            ArrayList<TrackDto>()
        }
        return SharedPrefsHistory(savedTracks)
    }

    override fun save(dto: Any) {
        if (dto is TrackDto) {
            savedTracks = getSaved().savedTracks.toMutableList()

            val index: Int = savedTracks.indexOf(dto)

            if (index >= 0) savedTracks.removeAt(index)

            savedTracks.add(0, dto)

            if (savedTracks.size > 10) savedTracks.removeAt(10)

            val json = Gson().toJson(savedTracks)
            sharedPreferences.edit {
                putString(HISTORY_KEY, json)
            }

        }
    }

    override fun clean() {
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