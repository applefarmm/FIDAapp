package com.fida.app.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.fida.app.R
import com.fida.app.utils.PreferenceHelper
import com.google.android.material.button.MaterialButton

class AccountSettingsFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?):
            View? {
        val view = inflater.inflate(R.layout.fragment_account_settings, container, false)
        setupViews(view)
        return view
    }

    private fun setupViews(view: View) {
        val prefs = PreferenceHelper(requireContext())
        val tvUsername = view.findViewById<EditText>(R.id.etAccountName)
        val tvEmail = view.findViewById<TextView>(R.id.tvAccountEmail)
        val ivProfilePhoto = view.findViewById<ImageView>(R.id.ivAccountPhoto)
        val btnSaveChanges = view.findViewById<MaterialButton>(R.id.btnSaveChangesAccount)
        val tvBack = view.findViewById<TextView>(R.id.tvBackToSettings)

        // Load current user data
        tvUsername.setText(prefs.getUsername() ?: "Athlete")
        tvEmail.text = prefs.getEmail() ?: ""
        // TODO: Load profile photo if available

        tvBack.setOnClickListener {
            // Navigate back to the main SettingsFragment
            activity?.let {
                (it as com.fida.app.SettingsActivity).loadFragment(SettingsFragment())
            }
        }

        btnSaveChanges.setOnClickListener {
            // TODO: Implement saving changes (name, photo, password, linked accounts)
            val newName = tvUsername.text.toString()
            // Update username in preferences or Firestore if needed
            prefs.saveUsername(newName)
            Toast.makeText(context, "Changes saved!", Toast.LENGTH_SHORT).show()
        }
    }
}
