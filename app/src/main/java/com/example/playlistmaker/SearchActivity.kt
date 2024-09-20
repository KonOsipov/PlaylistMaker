package com.example.playlistmaker

import android.content.Context
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


class SearchActivity : AppCompatActivity() {

    private val trackList = ArrayList<Track>()

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

        val iTunesBaseUrl = "https://itunes.apple.com"
        val retrofit = Retrofit.Builder()
            .baseUrl(iTunesBaseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val iTunesService = retrofit.create(ITunesSearchApi::class.java)


        val trackAdapter = TrackAdapter(trackList)


        inputEditText.setText(countValue)

        val simpleTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                cleanButton.visibility = cleanButtonVisibility(s)
                countValue = s.toString()
            }

            override fun afterTextChanged(s: Editable?) {
            }
        }
        inputEditText.addTextChangedListener(simpleTextWatcher)

        fun hideAll() {

            recyclerView.visibility = View.GONE
            errorLayout.visibility = View.GONE
            updateButton.visibility = View.GONE
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
            hideAll()
            val inputMethodManager =
                getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            inputMethodManager?.hideSoftInputFromWindow(inputEditText.windowToken, 0)
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


}