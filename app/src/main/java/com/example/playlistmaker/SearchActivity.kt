package com.example.playlistmaker

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Retrofit
import retrofit2.Response
import retrofit2.converter.gson.GsonConverterFactory


class SearchActivity : AppCompatActivity(), TrackHolder.Listener {

    private val trackList = ArrayList<Track>()
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var listener: SharedPreferences.OnSharedPreferenceChangeListener
    private val HISTORY_KEY = "key"

    private lateinit var searchHistory: SearchHistory
    private var isHistoryShows: Boolean = true // показывается история треков или нет


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        setSupportActionBar(findViewById(R.id.my_toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val inputEditText = findViewById<EditText>(R.id.search)
        val cleanButton = findViewById<ImageView>(R.id.clean_icon)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        val errorLayout: LinearLayout = findViewById(R.id.error_layout)
        val updateButton = findViewById<Button>(R.id.update_button)
        val searchErrorImage = findViewById<ImageView>(R.id.search_image_error)
        val searchErrorText = findViewById<TextView>(R.id.search_text_error)
        val cleanHistoryButton = findViewById<Button>(R.id.clean_history_button)
        val youSearchText = findViewById<TextView>(R.id.youSearch)

        val iTunesBaseUrl = "https://itunes.apple.com"
        val retrofit = Retrofit.Builder()
            .baseUrl(iTunesBaseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val iTunesService = retrofit.create(ITunesSearchApi::class.java)


        val trackAdapter = TrackAdapter(trackList, this)
        sharedPreferences = getSharedPreferences(HISTORY_KEY, Context.MODE_PRIVATE)

        searchHistory = SearchHistory(sharedPreferences)

        inputEditText.setText(countValue)

        fun showSavedTracks() {
            recyclerView.setItemViewCacheSize(searchHistory.getSaved().savedTracks.size)
            recyclerView.adapter = TrackAdapter(searchHistory.getSaved().savedTracks, this)
            cleanHistoryButton.visibility = View.VISIBLE
            recyclerView.visibility = View.VISIBLE
            youSearchText.visibility = View.VISIBLE
            isHistoryShows = true
        }

        fun hideSavedTracks() {
            //hintMessage.visibility = View.GONE
            cleanHistoryButton.visibility = View.GONE
            recyclerView.visibility = View.GONE
            youSearchText.visibility = View.GONE
        }


        ///----input listener
        val simpleTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                if (inputEditText.hasFocus() && s?.isEmpty() == true && true
                ) showSavedTracks() else hideSavedTracks()
                searchHistory.getSaved().savedTracks.isNotEmpty()
                cleanButton.visibility = cleanButtonVisibility(s)
                countValue = s.toString()
            }

            override fun afterTextChanged(s: Editable?) {
            }
        }
        inputEditText.addTextChangedListener(simpleTextWatcher)
        ///////

        ////-----focus listener
        inputEditText.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus && inputEditText.text.isEmpty() && searchHistory.getSaved().savedTracks
                    .isNotEmpty()
            ) showSavedTracks() else hideSavedTracks()
        }

        fun hideAll() {

            recyclerView.visibility = View.GONE
            errorLayout.visibility = View.GONE
            updateButton.visibility = View.GONE
            youSearchText.visibility = View.GONE
            cleanHistoryButton.visibility = View.GONE

        }


        fun showConnectionError() {

            trackList.clear()
            recyclerView.adapter = trackAdapter
            errorLayout.visibility = View.VISIBLE
            searchErrorImage.setImageResource(R.drawable.no_internet_error)
            searchErrorText.setText(R.string.no_connection)
            updateButton.visibility = View.VISIBLE
        }

        fun showNoResults() {

            recyclerView.adapter = trackAdapter
            searchErrorImage.setImageResource(R.drawable.no_track_error)
            searchErrorText.setText(R.string.no_find_error)
            errorLayout.visibility = View.VISIBLE
            updateButton.visibility = View.GONE
        }


        fun tracksRequest() {
            isHistoryShows = false
            if (inputEditText.text.isNotEmpty()) {
                hideAll()
                iTunesService.search(inputEditText.text.toString())
                    .enqueue(object : Callback<TrackResponse> {
                        override fun onResponse(
                            call: Call<TrackResponse>,
                            response: Response<TrackResponse>
                        ) {
                            if (response.code() == 200) {
                                trackList.clear()
                                if (response.body()?.results?.isNotEmpty() == true) {
                                    errorLayout.visibility = View.GONE
                                    trackList.addAll(response.body()?.results!!)

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

                        override fun onFailure(call: Call<TrackResponse>, t: Throwable) {

                            showConnectionError()
                        }

                    })
            }


        }
///////////////
        listener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
            if (key == HISTORY_KEY&&isHistoryShows) {

                Log.d("myTag","history changed")
                recyclerView.adapter = TrackAdapter(searchHistory.getSaved().savedTracks, this)

            }
        }
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
/////////////////////

        inputEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                tracksRequest()
            }
            false
        }

        updateButton.setOnClickListener {
            tracksRequest()
        }

        cleanButton.setOnClickListener {
            inputEditText.setText("")
            inputEditText.clearFocus()

            trackList.clear()
            hideAll()
            //hideSavedTracks()
            if (searchHistory.getSaved().savedTracks
                    .isNotEmpty()
            ) {
                showSavedTracks()
            }

            val inputMethodManager =
                getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            inputMethodManager?.hideSoftInputFromWindow(inputEditText.windowToken, 0)
        }

        cleanHistoryButton.setOnClickListener {
            searchHistory.clean()
            hideSavedTracks()
        }


    }

    private var countValue: String = AMOUNT_DEF

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(PRODUCT_AMOUNT, countValue)
    }

    companion object {
        private const val PRODUCT_AMOUNT = "TEXT"
        private const val AMOUNT_DEF = ""
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

    override fun onClickTrackHolder(track: Track) {

        searchHistory.addTrackToHistory(track)


    }}