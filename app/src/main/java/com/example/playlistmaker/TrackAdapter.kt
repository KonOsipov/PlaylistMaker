package com.example.playlistmaker

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class TrackAdapter(private val track: List<Track>, private val listener: TrackHolder.Listener) : RecyclerView.Adapter<TrackHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackHolder =
        TrackHolder(parent)


    override fun onBindViewHolder(holder: TrackHolder, position: Int) {
        holder.bind(track[position],listener)
    }

    override fun getItemCount(): Int {
        return track.size
    }

}