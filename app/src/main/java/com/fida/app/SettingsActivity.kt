package com.fida.app

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.fida.app.fragments.AccountSettingsFragment
import com.fida.app.fragments.NotificationSettingsFragment
import com.fida.app.fragments.PrivacySettingsFragment
import com.fida.app.fragments.SettingsFragment

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        supportActionBar?.hide()

        // Load the main SettingsFragment by default (only if not already loaded by FragmentContainerView)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.settingsFragmentContainer, SettingsFragment())
                .commit()
        }

        setupNavigationListeners()
    }

    private fun setupNavigationListeners() {
        findViewById<TextView>(R.id.tvBackToHomeSettings)?.setOnClickListener {
            finish()
        }
    }

    fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.settingsFragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }

    fun navigateToAccountSettings() {
        loadFragment(AccountSettingsFragment())
    }

    fun navigateToNotificationSettings() {
        loadFragment(NotificationSettingsFragment())
    }

    fun navigateToPrivacySettings() {
        loadFragment(PrivacySettingsFragment())
    }
}
