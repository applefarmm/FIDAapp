package com.fida.app.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.fida.app.AboutActivity
import com.fida.app.LoginActivity
import com.fida.app.R
import com.fida.app.SettingsActivity
import com.fida.app.utils.PreferenceHelper
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import androidx.appcompat.app.AppCompatDelegate

class SettingsFragment : Fragment() {

    private lateinit var prefs: PreferenceHelper

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?):
            View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)
        prefs = PreferenceHelper(requireContext())
        setupViews(view)
        return view
    }

    private fun setupViews(view: View) {
        // Navigation to Account Settings
        view.findViewById<TextView>(R.id.tvAccountSettings).setOnClickListener {
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

        // Units Setting
        val tvUnitsDisplay = view.findViewById<TextView>(R.id.tvUnitsDisplay)
        updateUnitsDisplay(tvUnitsDisplay)
        view.findViewById<TextView>(R.id.tvUnitsLabel).setOnClickListener {
            showUnitsDialog()
        }
        tvUnitsDisplay.setOnClickListener {
            showUnitsDialog()
        }

        // Theme Setting
        val tvThemeDisplay = view.findViewById<TextView>(R.id.tvThemeDisplay)
        updateThemeDisplay(tvThemeDisplay)
        view.findViewById<TextView>(R.id.tvThemeLabel).setOnClickListener {
            showThemeDialog()
        }
        tvThemeDisplay.setOnClickListener {
            showThemeDialog()
        }

        // Data Sync Setting
        val tvDataSync = view.findViewById<TextView>(R.id.tvDataSync)
        tvDataSync.setOnClickListener {
            syncData()
        }

        // About
        view.findViewById<TextView>(R.id.tvAboutLabel).setOnClickListener {
            startActivity(Intent(requireContext(), AboutActivity::class.java))
        }

        // Logout
        view.findViewById<TextView>(R.id.tvLogout).setOnClickListener {
            showLogoutDialog()
        }
    }

    private fun updateUnitsDisplay(textView: TextView) {
        val unitPref = prefs.getString("preferred_unit") ?: "km"
        textView.text = unitPref
    }

    private fun updateThemeDisplay(textView: TextView) {
        val themePref = prefs.getString("preferred_theme") ?: "System"
        textView.text = themePref
    }

    private fun showUnitsDialog() {
        val currentUnit = prefs.getString("preferred_unit") ?: "km"
        val units = arrayOf("km", "mi")
        val selectedIndex = units.indexOf(currentUnit)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Select Units")
            .setSingleChoiceItems(units, selectedIndex) { dialog, which ->
                val selectedUnit = units[which]
                prefs.saveString("preferred_unit", selectedUnit)
                updateUnitsDisplay(view?.findViewById(R.id.tvUnitsDisplay) ?: return@setSingleChoiceItems)
                dialog.dismiss()
            }
            .show()
    }

    private fun showThemeDialog() {
        val currentTheme = prefs.getString("preferred_theme") ?: "System"
        val themes = arrayOf("System", "Light", "Dark")
        val selectedIndex = themes.indexOf(currentTheme)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Select Theme")
            .setSingleChoiceItems(themes, selectedIndex) { dialog, which ->
                val selectedTheme = themes[which]
                prefs.saveString("preferred_theme", selectedTheme)
                applyTheme(selectedTheme)
                updateThemeDisplay(view?.findViewById(R.id.tvThemeDisplay) ?: return@setSingleChoiceItems)
                dialog.dismiss()
            }
            .show()
    }

    private fun applyTheme(theme: String) {
        val mode = when (theme) {
            "Light" -> AppCompatDelegate.MODE_NIGHT_NO
            "Dark" -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(mode)
    }

    private fun syncData() {
        Toast.makeText(context, "Data sync completed", Toast.LENGTH_SHORT).show()
    }

    private fun showLogoutDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Log Out")
            .setMessage("Are you sure you want to log out?")
            .setPositiveButton("Log Out") { dialog, _ ->
                prefs.clear()
                val intent = Intent(requireContext(), LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}
