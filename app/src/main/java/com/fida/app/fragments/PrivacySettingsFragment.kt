package com.fida.app.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.fida.app.R
import com.fida.app.SettingsActivity

class PrivacySettingsFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?):
            View? {
        val view = inflater.inflate(R.layout.fragment_privacy_settings, container, false)
        setupViews(view)
        return view
    }

    private fun setupViews(view: View) {
        val tvBack = view.findViewById<TextView>(R.id.tvBackToSettingsPrivacy)

        // TODO: Implement logic for privacy settings like data sharing, visibility etc.
        // For example, you might have toggles or options here.

        tvBack.setOnClickListener {
            // Navigate back to the main SettingsFragment
            activity?.let {
                (it as com.fida.app.SettingsActivity).loadFragment(SettingsFragment())
            }
        }
    }
}
