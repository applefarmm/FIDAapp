package com.fida.app

import android.os.Bundle
import android.widget.CheckBox
import androidx.appcompat.app.AppCompatActivity
import com.fida.app.utils.PreferenceHelper
import com.google.android.material.button.MaterialButton

class NotificationSettingsActivity : AppCompatActivity() {

    private lateinit var prefs: PreferenceHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification_settings)
        supportActionBar?.hide()

        prefs = PreferenceHelper(this)
        setupViews()
    }

    private fun setupViews() {
        val cbDailyReminders = findViewById<CheckBox>(R.id.cbDailyReminders)
        val cbAchievementNotifs = findViewById<CheckBox>(R.id.cbAchievementNotifs)
        val cbLeaderboardNotifs = findViewById<CheckBox>(R.id.cbLeaderboardNotifs)
        val cbWaterReminders = findViewById<CheckBox>(R.id.cbWaterReminders)
        val cbSleepReminders = findViewById<CheckBox>(R.id.cbSleepReminders)

        findViewById<MaterialButton>(R.id.btnSaveNotificationSettings).setOnClickListener {
            finish()
        }
    }
}