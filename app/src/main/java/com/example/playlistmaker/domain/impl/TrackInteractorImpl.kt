package com.example.playlistmaker.domain.impl

import com.example.playlistmaker.domain.api.TrackRepository
import com.example.playlistmaker.domain.api.TrackSharedPref
import com.example.playlistmaker.domain.api.TracksInteractor
import com.example.playlistmaker.domain.models.Track
import java.util.concurrent.Executors

class TrackInteractorImpl (private val repository: TrackRepository, private val sharedPrefs: TrackSharedPref) :
    TracksInteractor {

    private val executor = Executors.newCachedThreadPool()

    override fun searchTracks(expression: String, consumer: TracksInteractor.TracksConsumer) {
        executor.execute {
            try {
                consumer.consume(repository.searchTracks(expression), repository.getResultCode())
            } catch (t: Throwable) {
                consumer.onFailure(t)
            }

        }
    }

    override fun getSavedTracks(): List<Track> {
        return sharedPrefs.getSavedTracks()
    }

    override fun saveTrack(track: Track) {
        sharedPrefs.saveTrack(track)
    }

    override fun cleanHistory() {
        sharedPrefs.cleanHistory()
    }

}