package com.fida.app.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.fida.app.R
import com.fida.app.SettingsActivity

class SettingsFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?):
            View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)
        setupViews(view)
        return view
    }

    private fun setupViews(view: View) {
        // Navigation to Account Settings
        view.findViewById<TextView>(R.id.tvAccountSettings).setOnClickListener {
            // Assuming SettingsActivity has a method to load AccountSettingsFragment
            (activity as? SettingsActivity)?.navigateToAccountSettings()
        }

        // Navigation to Notification Settings
        view.findViewById<TextView>(R.id.tvNotificationSettings).setOnClickListener {
            (activity as? SettingsActivity)?.navigateToNotificationSettings()
        }

        // Navigation to Privacy Settings
        view.findViewById<TextView>(R.id.tvPrivacySettings).setOnClickListener {
            (activity as? SettingsActivity)?.navigateToPrivacySettings()
        }

        // Placeholder for other settings like Units, Theme, Data Sync
        // view.findViewById<TextView>(R.id.tvUnits).setOnClickListener { ... }
        // view.findViewById<TextView>(R.id.tvTheme).setOnClickListener { ... }
        // view.findViewById<TextView>(R.id.tvDataSync).setOnClickListener { ... }
    }
}
