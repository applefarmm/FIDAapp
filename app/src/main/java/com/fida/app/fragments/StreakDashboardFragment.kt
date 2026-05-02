package com.fida.app.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.fida.app.R
import com.fida.app.utils.PreferenceHelper

class StreakDashboardFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?):
            View? {
        val view = inflater.inflate(R.layout.fragment_streak_dashboard, container, false)
        setupViews(view)
        return view
    }

    private fun setupViews(view: View) {
        val tvCurrentStreak = view.findViewById<TextView>(R.id.tvCurrentStreak)
        val tvLongestStreak = view.findViewById<TextView>(R.id.tvLongestStreak)
        val ivStreakShields = view.findViewById<ImageView>(R.id.ivStreakShields)
        val tvStreakShieldCount = view.findViewById<TextView>(R.id.tvStreakShieldCount)

        // TODO: Fetch streak data and shield count from preferences or Firestore
        val prefs = PreferenceHelper(requireContext())
        val currentStreak = prefs.getInt("currentStreak") ?: 0
        val longestStreak = prefs.getInt("longestStreak") ?: 0
        val streakShields = prefs.getInt("streakShields") ?: 0

        tvCurrentStreak.text = "Current Streak: $currentStreak days"
        tvLongestStreak.text = "Longest Streak: $longestStreak days"
        // Assuming you have a shield icon drawable
        // ivStreakShields.setImageResource(R.drawable.ic_streak_shield)
        tvStreakShieldCount.text = "x $streakShields"

        // TODO: Implement logic for streak calendar/heatmap view if needed
        // This might involve a custom view or a library.
    }
}
