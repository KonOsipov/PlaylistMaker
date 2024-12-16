package com.example.playlistmaker.ui.player

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.R
import com.example.playlistmaker.creator.Creator
import com.example.playlistmaker.domain.api.PlayerInteractor
import com.example.playlistmaker.domain.models.PlayerState
import com.example.playlistmaker.domain.models.Track
import java.text.SimpleDateFormat
import java.util.Locale


class PlayerActivity : AppCompatActivity() {

    companion object {
        private const val ZERO_TIMER = "00:00"
        private const val TIMER_DELAY = 400L
    }

    private lateinit var playButton: ImageView
    private var mainThreadHandler: Handler = Handler(Looper.getMainLooper())
    private var actualTime: TextView? = null

    private lateinit var track: Track

    private val dateFormat by lazy { SimpleDateFormat("mm:ss", Locale.getDefault()) }

    private val playerInteractor = Creator.provideMediaPlayaerInteractor()
    private var timerRunnable: Runnable? = null


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        setSupportActionBar(findViewById(R.id.my_toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val intent = intent
        track = intent.getParcelableExtra("track")!!

        val trackIcon = findViewById<ImageView>(R.id.track_icon)
        val trackName = findViewById<TextView>(R.id.track_name)
        val musicianName = findViewById<TextView>(R.id.musician_name)
        val imageUrl = track.getCoverArtwork()
        val trackTiming = findViewById<TextView>(R.id.timing_track)
        val trackAlbum = findViewById<TextView>(R.id.album_track)
        val trackYear = findViewById<TextView>(R.id.year_track)
        val genre = findViewById<TextView>(R.id.genre_track)
        val country = findViewById<TextView>(R.id.country_track)
        val url = track.previewUrl

        playButton = findViewById(R.id.play_button)
        actualTime = findViewById(R.id.actual_time)
        actualTime?.text = getString(R.string._00_00)


        trackName.text = track.trackName
        musicianName.text = track.artistName
        trackTiming.text = dateFormat.format(track.trackTimeMillis?.toInt())
        trackAlbum.text = track.collectionName
        trackYear.text = track.getYear()
        genre.text = track.primaryGenreName
        country.text = track.country

        Glide.with(baseContext).load(imageUrl)
            .centerCrop()
            .transform(RoundedCorners(8))
            .placeholder(R.drawable.placeholder)
            .into(trackIcon)

        preparePlayer()

        playButton.setOnClickListener {
            when (playerInteractor.getPlayerState()) {
                PlayerState.PREPARED, PlayerState.PAUSED -> playerInteractor.start()
                PlayerState.PLAYING -> playerInteractor.pause()
                PlayerState.DEFAULT -> {}
            }
        }

    }

    private fun preparePlayer() {
        playerInteractor.setPlayerDataSource(track)
        playerInteractor.prepare(
            listener = object : PlayerInteractor.OnStateChangeListener {
                override fun onChange(state: PlayerState) {
                    when (state) {
                        PlayerState.PREPARED -> {
                            playButton.isEnabled = true
                            playButton.setImageResource(R.drawable.play_button)
                            manageTimer()
                        }

                        PlayerState.PLAYING -> {
                            playButton.setImageResource(R.drawable.pause)
                            manageTimer()
                        }

                        PlayerState.PAUSED -> {
                            playButton.setImageResource(R.drawable.play_button)
                            manageTimer()
                        }

                        PlayerState.DEFAULT -> {
                            playButton.isEnabled = false
                            playButton.setImageResource(R.drawable.play_button)
                            manageTimer()
                        }
                    }
                }
            }
        )
    }

    private fun manageTimer() {
        manageTimerByPlayerState(playerInteractor.getPlayerState())
    }

    private fun manageTimerByPlayerState(state: PlayerState) {
        when (state) {
            PlayerState.DEFAULT, PlayerState.PREPARED -> {
                mainThreadHandler.removeCallbacksAndMessages(null)
                actualTime?.text = ZERO_TIMER
            }

            PlayerState.PLAYING -> {
                val runnable = object : Runnable {
                    override fun run() {
                        actualTime?.text = playerInteractor.getPlayerCurrentTimerPosition()
                        mainThreadHandler.postDelayed(this, TIMER_DELAY)
                    }
                }

                mainThreadHandler.postDelayed(runnable, TIMER_DELAY)
                timerRunnable = runnable
            }

            PlayerState.PAUSED -> timerRunnable?.let {
                mainThreadHandler.removeCallbacksAndMessages(
                    timerRunnable
                )
            }
        }
    }

    override fun onPause() {
        super.onPause()
        playerInteractor.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        playerInteractor.release()
        manageTimer()
    }

}
