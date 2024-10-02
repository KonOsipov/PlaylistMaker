package com.example.playlistmaker

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import java.text.SimpleDateFormat
import java.util.Locale



class PlayerActivity : AppCompatActivity() {

    private lateinit var track: Track
    private val dateFormat by lazy { SimpleDateFormat("mm:ss", Locale.getDefault()) }
    private lateinit var playButton: ImageView
    private var actualTime: TextView? = null

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

        playButton = findViewById(R.id.play_button)
        actualTime = findViewById(R.id.actual_time)
        actualTime?.text = "00:30"

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

    }
}