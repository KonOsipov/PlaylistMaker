package com.example.playlistmaker.ui.tracklist

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import java.util.Locale
import android.icu.text.SimpleDateFormat
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.models.Track

class TrackHolder(parent: ViewGroup) :
    RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context)
            .inflate(R.layout.track, parent, false)
    ) {

    private val trackIcon: ImageView = itemView.findViewById(R.id.track_icon)
    private var trackName: TextView = itemView.findViewById(R.id.track_name)
    private val musicianName: TextView = itemView.findViewById(R.id.musician_name)
    private val trackTiming: TextView = itemView.findViewById(R.id.track_timing)


    fun bind(model: Track, listener: Listener) {
        trackName.text = model.trackName
        musicianName.text = model.artistName
        trackTiming.text = SimpleDateFormat("mm:ss", Locale.getDefault()).format(model.trackTimeMillis?.toInt())

        val imageUrl = model.artworkUrl100

        Glide.with(itemView).load(imageUrl)
            .centerCrop()
            .transform(RoundedCorners(10))
            .placeholder(R.drawable.placeholder)
            .into(trackIcon)

        itemView.setOnClickListener {
            listener.onClickTrackHolder(model)
        }
    }
    interface Listener {
        fun onClickTrackHolder(track: Track)
    }


}