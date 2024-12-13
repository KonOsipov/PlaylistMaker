package com.example.playlistmaker.ui.player

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.models.Track
import java.text.SimpleDateFormat
import java.util.Locale



class PlayerActivity : AppCompatActivity() {

    companion object {
        private const val STATE_DEFAULT = 0
        private const val STATE_PREPARED = 1
        private const val STATE_PLAYING = 2
        private const val STATE_PAUSED = 3
        private const val DELAY = 500L
    }

    private var mediaPlayer = MediaPlayer()
    private lateinit var playButton: ImageView
    private var mainThreadHandler: Handler? = null
    private var actualTime: TextView? = null
    private var playerState = STATE_DEFAULT

    private lateinit var track: Track

    private val dateFormat by lazy { SimpleDateFormat("mm:ss", Locale.getDefault()) }


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

        mainThreadHandler = Handler(Looper.getMainLooper())

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

        if (url != null) {
            preparePlayer(url)
        }

        playButton.setOnClickListener {
            playbackControl()
        }
    }
    override fun onPause() {
        super.onPause()
        val secondPlayerStats = playerState
        if (playerState == STATE_PAUSED) pauseTimer()
        pausePlayer()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
        mainThreadHandler?.removeCallbacksAndMessages(null)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun startPlayer() {
        mediaPlayer.start()
        playButton.setImageResource(R.drawable.pause)
        playerState = STATE_PLAYING
    }

    private fun pausePlayer() {
        mediaPlayer.pause()
        playButton.setImageResource(R.drawable.play_button)
        playerState = STATE_PAUSED

    }

    private fun playbackControl() {
        when (playerState) {
            STATE_PLAYING -> {
                pausePlayer()
                pauseTimer()
            }

            STATE_PREPARED, STATE_PAUSED -> {
                startPlayer()
                startTimer()
            }
        }
    }

    private fun preparePlayer(url: String) {
        mediaPlayer.setDataSource(url)
        mediaPlayer.prepareAsync()
        mediaPlayer.setOnPreparedListener {

            playerState = STATE_PREPARED
        }
        mediaPlayer.setOnCompletionListener {
            playerState = STATE_PREPARED
            playButton.setImageResource(R.drawable.play_button)
            mainThreadHandler?.removeCallbacksAndMessages(null)
            actualTime?.text = getString(R.string._00_00)
        }
    }

    private fun startTimer() {
        mainThreadHandler?.post(
            createUpdateTimerTask()
        )
    }

    private fun pauseTimer() {
        actualTime?.text = dateFormat.format(mediaPlayer.currentPosition)
        mainThreadHandler?.removeCallbacksAndMessages(null)
    }

    private fun createUpdateTimerTask(): Runnable {
        return object : Runnable {
            override fun run() {
                actualTime?.text = dateFormat.format(mediaPlayer.currentPosition)
                mainThreadHandler?.postDelayed(this, DELAY)
            }
        }

    }
}