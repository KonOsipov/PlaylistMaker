package com.example.playlistmaker.ui.tracklist

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.R
import com.example.playlistmaker.creator.Creator.provideTracksInteractor
import com.example.playlistmaker.domain.models.Track
import com.example.playlistmaker.domain.api.TracksInteractor
import com.example.playlistmaker.ui.player.PlayerActivity


class SearchActivity : AppCompatActivity(), TrackHolder.Listener {


    private var isClickAllowed = true
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var sharedPreferences: SharedPreferences


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        setSupportActionBar(findViewById(R.id.my_toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)


        val queryInput = findViewById<EditText>(R.id.search)
        val cleanButton = findViewById<ImageView>(R.id.clean_icon)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        val searchErrorImage = findViewById<ImageView>(R.id.search_image_error)
        val searchErrorText = findViewById<TextView>(R.id.search_text_error)
        val updateButton = findViewById<Button>(R.id.update_button)
        val errorLayout: LinearLayout = findViewById(R.id.error_layout)
        val hintMessage = findViewById<TextView>(R.id.youSearch)
        val cleanHistoryButton = findViewById<Button>(R.id.clean_history_button)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)

        val trackAdapter = TrackAdapter(trackList, this)
        sharedPreferences = getSharedPreferences(HISTORY_KEY, Context.MODE_PRIVATE)
        var trackInteractor = provideTracksInteractor(sharedPreferences)

        getSharedPreferences(SEARCH_HISTORY, MODE_PRIVATE)


        fun showSavedTracks() {
            trackInteractor = provideTracksInteractor(sharedPreferences)
            recyclerView.setItemViewCacheSize(trackInteractor.getSavedTracks().size)
            recyclerView.adapter = TrackAdapter(trackInteractor.getSavedTracks(), this)
            hintMessage.visibility = View.VISIBLE
            cleanHistoryButton.visibility = View.VISIBLE
            recyclerView.visibility = View.VISIBLE
        }

        fun hideSavedTracks() {
            hintMessage.visibility = View.GONE
            cleanHistoryButton.visibility = View.GONE
            recyclerView.visibility = View.GONE
        }

        fun hideAll() {
            hintMessage.visibility = View.GONE
            cleanHistoryButton.visibility = View.GONE
            recyclerView.visibility = View.GONE
            errorLayout.visibility = View.GONE
            updateButton.visibility = View.GONE
        }


        fun showConnectionError() {
            requestStatusFlag = NO_CONNECTION
            trackList.clear()
            recyclerView.adapter = trackAdapter
            errorLayout.visibility = View.VISIBLE
            searchErrorImage.setImageResource(R.drawable.no_internet_error)
            searchErrorText.setText(R.string.no_connection)
            updateButton.visibility = View.VISIBLE
        }

        fun showNoResults() {
            requestStatusFlag = NO_RESULT
            recyclerView.adapter = trackAdapter
            searchErrorImage.setImageResource(R.drawable.no_track_error)
            searchErrorText.setText(R.string.no_find_error)
            errorLayout.visibility = View.VISIBLE
            updateButton.visibility = View.GONE
        }


        fun tracksRequest() {

            if (countValue == "") {
                requestStatusFlag = NO_REQUEST
            } else {
                hideAll()
                progressBar.visibility = View.VISIBLE
                trackList.clear()
                trackInteractor = provideTracksInteractor(sharedPreferences)
                trackInteractor.searchTracks(countValue, object : TracksInteractor.TracksConsumer {

                    override fun consume(foundTracks: List<Track>, resultCode: Int) {
                        runOnUiThread {
                            progressBar.visibility = View.GONE
                            if (resultCode == 200) {
                                if (foundTracks.isNotEmpty()) {
                                    requestStatusFlag = REQUEST_DONE
                                    errorLayout.visibility = View.GONE
                                    trackList.addAll(foundTracks)
                                    recyclerView.setItemViewCacheSize(trackList.size)
                                    recyclerView.adapter = trackAdapter
                                    recyclerView.visibility = View.VISIBLE
                                }
                                if (trackList.isEmpty()) {
                                    showNoResults()
                                }
                            } else {
                                showConnectionError()
                            }
                        }

                    }

                    override fun onFailure(t: Throwable) {
                        runOnUiThread {
                            progressBar.visibility = View.GONE
                            showConnectionError()
                        }
                    }

                })


            }
        }

        val searchRunnable = Runnable { tracksRequest() }

        fun searchDebounce() {
            handler.removeCallbacks(searchRunnable)
            handler.postDelayed(searchRunnable, SEARCH_DEBOUNCE_DELAY)
        }

        val simpleTextWatcher = object : TextWatcher {


            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                searchDebounce()

                if (queryInput.hasFocus() && s?.isEmpty() == true && trackInteractor.getSavedTracks()
                        .isNotEmpty()
                ) showSavedTracks() else hideSavedTracks()

                cleanButton.visibility = cleanButtonVisibility(s)
                countValue = s.toString()
            }

            override fun afterTextChanged(s: Editable?) {
            }
        }




        queryInput.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus && queryInput.text.isEmpty() && trackInteractor.getSavedTracks()
                    .isNotEmpty()
            ) showSavedTracks() else hideSavedTracks()
        }


        queryInput.setText(countValue)
        cleanButton.visibility = cleanButtonVisibility(countValue)
        when (requestStatusFlag) {
            NO_REQUEST -> {
                hideAll()
            }

            REQUEST_DONE -> {
                recyclerView.adapter = trackAdapter
                recyclerView.visibility = View.VISIBLE
            }

            NO_RESULT -> showNoResults()
            NO_CONNECTION -> showConnectionError()
        }

        //


        updateButton.setOnClickListener {
            tracksRequest()
        }

        cleanButton.setOnClickListener {
            queryInput.setText("")
            requestStatusFlag = NO_REQUEST
            errorLayout.visibility = View.GONE
            trackList.clear()
            hideSavedTracks()
            val inputMethodManager =
                getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            inputMethodManager?.hideSoftInputFromWindow(queryInput.windowToken, 0)
            queryInput.clearFocus()
        }

        cleanHistoryButton.setOnClickListener {
            trackInteractor.cleanHistory()
            hideSavedTracks()
        }

        queryInput.addTextChangedListener(simpleTextWatcher)

        recyclerView.layoutManager = LinearLayoutManager(this)

        queryInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                tracksRequest()
            }
            false
        }

    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(PRODUCT_AMOUNT, countValue)
    }


    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        countValue = savedInstanceState.getString(PRODUCT_AMOUNT, AMOUNT_DEF)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) finish()
        return super.onOptionsItemSelected(item)
    }

    private fun cleanButtonVisibility(s: CharSequence?): Int {
        return if (s.isNullOrEmpty()) {
            View.GONE
        } else {
            View.VISIBLE
        }
    }


    private companion object {
        const val PRODUCT_AMOUNT = "TEXT"
        const val AMOUNT_DEF = ""
        const val NO_REQUEST = "no request"
        const val REQUEST_DONE = "done"
        const val NO_RESULT = "no result"
        const val NO_CONNECTION = "no connection"
        private var countValue: String = AMOUNT_DEF
        private var requestStatusFlag: String = NO_REQUEST
        private val trackList = ArrayList<Track>()
        private const val SEARCH_DEBOUNCE_DELAY = 2000L
        private const val CLICK_DEBOUNCE_DELAY = 1000L
        const val SEARCH_HISTORY = "Search history"
        const val HISTORY_KEY = "key"
        private const val TRACK = "track"
    }


    private fun clickDebounce(): Boolean {
        val current = isClickAllowed
        if (isClickAllowed) {
            isClickAllowed = false
            handler.postDelayed({ isClickAllowed = true }, CLICK_DEBOUNCE_DELAY)
        }
        return current
    }

    override fun onClickTrackHolder(track: Track) {
        if (clickDebounce()) {
            val trackInteractor = provideTracksInteractor(sharedPreferences)
            trackInteractor.saveTrack(track)
            val displayIntent = Intent(this, PlayerActivity::class.java)
            displayIntent.putExtra(TRACK,track)
            startActivity(displayIntent)
        }

    }
}