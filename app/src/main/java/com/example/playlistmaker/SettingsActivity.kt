package com.example.playlistmaker

import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
//import android.widget.Button
import android.widget.TextView
import com.google.android.material.switchmaterial.SwitchMaterial

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val settingsBackButton = findViewById<View>(R.id.settings_back)
        settingsBackButton.setOnClickListener {
            finish()
        }

        val themeSwitcher = findViewById<SwitchMaterial>(R.id.themeSwitcher)

        //установка свитчера в соответчиии с темой
        when (resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> themeSwitcher.toggle()
        }
        ////

        themeSwitcher.setOnCheckedChangeListener { switcher, checked ->
            (applicationContext as App).switchTheme(checked)
        }

        val shareButton = findViewById<TextView>(R.id.share_button)
        shareButton.setOnClickListener {
            val message = getString(R.string.share_message)
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_TEXT, message)
            val chooserIntent = Intent.createChooser(shareIntent,null)
            startActivity(chooserIntent)
        }

        val supportButton = findViewById<TextView>(R.id.support_button)
        supportButton.setOnClickListener {
            val subject = getString(R.string.support_subject)
            val message = getString(R.string.support_message)
            val devEmail = getString(R.string.dev_email)
            val supportIntent = Intent(Intent.ACTION_SENDTO)
            supportIntent.data = Uri.parse("mailto:")
            supportIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(devEmail))
            supportIntent.putExtra(Intent.EXTRA_SUBJECT, subject)
            supportIntent.putExtra(Intent.EXTRA_TEXT, message)
            startActivity(supportIntent)
        }

        val agreementButton = findViewById<TextView>(R.id.agreement_button)
        agreementButton.setOnClickListener {
            val agreementText=getString(R.string.agreement_text)
            val agreementIntent = Intent(Intent.ACTION_VIEW,Uri.parse(agreementText))
            startActivity(agreementIntent)
        }
    }
}
