package com.fida.app.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.fida.app.R
import com.fida.app.utils.PreferenceHelper

class StreakProtectionFragment : Fragment() {

    private lateinit var prefs: PreferenceHelper

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?):
            View? {
        val view = inflater.inflate(R.layout.fragment_streak_protection, container, false)
        prefs = PreferenceHelper(requireContext())
        setupViews(view)
        return view
    }

    private fun setupViews(view: View) {
        val tvShieldCount = view.findViewById<TextView>(R.id.tvShieldCount)
        val btnUseShield = view.findViewById<Button>(R.id.btnUseShield)

        var availableShields = prefs.getInt("streakShields") ?: 0
        tvShieldCount.text = "Available Shields: $availableShields"

        btnUseShield.isEnabled = availableShields > 0

        btnUseShield.setOnClickListener {
            if (availableShields > 0) {
                availableShields--
                prefs.saveInt("streakShields", availableShields)
                tvShieldCount.text = "Available Shields: $availableShields"
                btnUseShield.isEnabled = availableShields > 0
                Toast.makeText(context, "Streak Shield used! Your streak is safe for today.", Toast.LENGTH_LONG).show()
                // TODO: Add logic to mark today's streak as 'protected'
            } else {
                Toast.makeText(context, "You don't have any shields.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
