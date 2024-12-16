package com.example.playlistmaker.domain.api

import com.example.playlistmaker.domain.models.PlayerState
import com.example.playlistmaker.domain.models.Track

interface PlayerRepository {
    fun preparePlayer(listener: PlayerInteractor.OnStateChangeListener)
    fun startPlayer()
    fun pausePlayer()
    fun releasePlayer()
    fun getPlayerCurrentTimerPosition(): String
    fun getPlayerState(): PlayerState
    fun setPlayerDataSource(track: Track)
}