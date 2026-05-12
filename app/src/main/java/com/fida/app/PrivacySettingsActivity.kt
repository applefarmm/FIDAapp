package com.fida.app

import android.os.Bundle
import android.widget.CheckBox
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class PrivacySettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_privacy_settings)
        supportActionBar?.hide()

        setupViews()
    }

    private fun setupViews() {
        findViewById<MaterialButton>(R.id.btnSavePrivacySettings).setOnClickListener {
            finish()
        }
    }
}