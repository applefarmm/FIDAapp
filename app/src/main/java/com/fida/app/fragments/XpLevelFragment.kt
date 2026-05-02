package com.fida.app.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.fida.app.R

class XpLevelFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?):
            View? {
        val view = inflater.inflate(R.layout.fragment_xp_level, container, false)
        setupViews(view)
        return view
    }

    private fun setupViews(view: View) {
        val tvCurrentLevel = view.findViewById<TextView>(R.id.tvCurrentLevel)
        val xpProgressBar = view.findViewById<ProgressBar>(R.id.xpProgressBar)
        val tvXpProgress = view.findViewById<TextView>(R.id.tvXpProgress)
        val tvActivityXpBreakdown = view.findViewById<TextView>(R.id.tvActivityXpBreakdown)
        val tvLevelMilestones = view.findViewById<TextView>(R.id.tvLevelMilestones)

        // TODO: Fetch user data (level, xp, etc.) and populate views
        // Example data:
        val currentLevel = 5
        val currentXp = 750
        val xpToNextLevel = 1000

        tvCurrentLevel.text = "Level $currentLevel"
        xpProgressBar.max = xpToNextLevel
        xpProgressBar.progress = currentXp
        tvXpProgress.text = "$currentXp / $xpToNextLevel XP"

        // Example breakdown (replace with actual data fetching)
        tvActivityXpBreakdown.text = "Run XP: 400 | Water XP: 150 | Sleep XP: 200"
        tvLevelMilestones.text = "Level 10: New Avatar | Level 15: Special Badge"
    }
}
