package com.fida.app.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.fida.app.R
import com.fida.app.SettingsActivity
import com.fida.app.utils.PreferenceHelper

class NotificationSettingsFragment : Fragment() {

    private lateinit var prefs: PreferenceHelper

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?):
            View? {
        val view = inflater.inflate(R.layout.fragment_notification_settings, container, false)
        prefs = PreferenceHelper(requireContext())
        setupViews(view)
        return view
    }

    private fun setupViews(view: View) {
        val tvBack = view.findViewById<TextView>(R.id.tvBackToSettingsNotifications)

        setupToggleListener(view, R.id.switchRunReminder, "runReminderEnabled")
        setupToggleListener(view, R.id.switchWaterReminder, "waterReminderEnabled")
        setupToggleListener(view, R.id.switchSleepReminder, "sleepReminderEnabled")

        tvBack.setOnClickListener {
            activity?.let {
                (it as com.fida.app.SettingsActivity).loadFragment(SettingsFragment())
            }
        }
    }

    private fun setupToggleListener(view: View, switchId: Int, preferenceKey: String) {
        val switchToggle = view.findViewById<CompoundButton>(switchId)
        switchToggle.isChecked = prefs.getBoolean(preferenceKey) ?: true // Default to true if not set

        switchToggle.setOnCheckedChangeListener {
                _, isChecked ->
            prefs.saveBoolean(preferenceKey, isChecked)
        }
    }
}
