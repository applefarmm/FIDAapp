package com.fida.app.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.fida.app.R

class AboutFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_about, container, false)
        setupViews(view)
        return view
    }

    private fun setupViews(view: View) {
        // Display app version (example)
        val tvAppVersion = view.findViewById<TextView>(R.id.tvAppVersion)
        val versionName = "1.0.0" // Replace with actual version fetching logic if available
        tvAppVersion.text = "Version: $versionName"

        // Placeholder for links to Privacy Policy and Terms of Service
        val tvPrivacyPolicy = view.findViewById<TextView>(R.id.tvPrivacyPolicy)
        val tvTermsOfService = view.findViewById<TextView>(R.id.tvTermsOfService)

        // You would typically set OnClickListeners here to open web pages or specific activities
        // For now, they will just display placeholder text or remain static.
    }
}
