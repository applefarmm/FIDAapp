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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?):
            View? {
        val view = inflater.inflate(R.layout.fragment_streak_protection, container, false)
        setupViews(view)
        return view
    }

    private fun setupViews(view: View) {
        val tvExplanation = view.findViewById<TextView>(R.id.tvStreakProtectionExplanation)
        val tvShieldCount = view.findViewById<TextView>(R.id.tvShieldCount)
        val btnUseShield = view.findViewById<Button>(R.id.btnUseShield)

        val prefs = PreferenceHelper(requireContext())
        val availableShields = prefs.getInt("streakShields") ?: 0

        tvShieldCount.text = "Available Shields: $availableShields"

        // TODO: Implement actual logic for using a shield
        btnUseShield.setOnClickListener {
            if (availableShields > 0) {
                // Logic to use a shield for the current day
                // Decrement shield count, mark streak as protected for the day
                prefs.saveInt("streakShields", availableShields - 1)
                Toast.makeText(context, "Streak Shield used!", Toast.LENGTH_SHORT).show()
                // Update UI to reflect shield usage
                view.findViewById<TextView>(R.id.tvShieldCount).text = "Available Shields: ${availableShields - 1}"
                btnUseShield.isEnabled = false // Disable after use
            } else {
                Toast.makeText(context, "You don't have enough shields.", Toast.LENGTH_SHORT).show()
            }
        }
        btnUseShield.isEnabled = availableShields > 0
    }
}
