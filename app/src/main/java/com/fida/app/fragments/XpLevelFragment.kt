package com.fida.app.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.fida.app.R
import com.fida.app.utils.FirestoreRepository
import com.fida.app.utils.PreferenceHelper

class XpLevelFragment : Fragment() {

    private lateinit var prefs: PreferenceHelper

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?):
            View? {
        val view = inflater.inflate(R.layout.fragment_xp_level, container, false)
        prefs = PreferenceHelper(requireContext())
        setupViews(view)
        loadXpBreakdown(view)
        return view
    }

    private fun setupViews(view: View) {
        val tvCurrentLevel = view.findViewById<TextView>(R.id.tvCurrentLevel)
        val xpProgressBar = view.findViewById<ProgressBar>(R.id.xpProgressBar)
        val tvXpProgress = view.findViewById<TextView>(R.id.tvXpProgress)
        val tvLevelMilestones = view.findViewById<TextView>(R.id.tvLevelMilestones)

        val currentLevel = prefs.getInt("level") ?: 1
        val currentXp = prefs.getInt("xp") ?: 0
        val xpToNextLevel = prefs.getInt("maxXp") ?: 100

        tvCurrentLevel.text = "Level $currentLevel"
        xpProgressBar.max = xpToNextLevel
        xpProgressBar.progress = currentXp
        tvXpProgress.text = "$currentXp / $xpToNextLevel XP"

        tvLevelMilestones.text = buildLevelMilestones(currentLevel)
    }

    private fun buildLevelMilestones(currentLevel: Int): String {
        val milestones = mutableListOf<String>()
        if (currentLevel < 5) milestones.add("Level 5: 500 Coins Bonus")
        if (currentLevel < 10) milestones.add("Level 10: New Avatar Unlock")
        if (currentLevel < 15) milestones.add("Level 15: Epic Badge")
        if (currentLevel < 20) milestones.add("Level 20: Special Title")
        return milestones.take(3).joinToString("\n")
    }

    private fun loadXpBreakdown(view: View) {
        val uid = prefs.getUid() ?: return
        val tvActivityXpBreakdown = view.findViewById<TextView>(R.id.tvActivityXpBreakdown)

        FirestoreRepository.getUser(uid) { userData ->
            if (userData == null) return@getUser

            val allDays = userData["allDays"] as? Map<String, Map<String, Any>> ?: emptyMap()

            var runXp = 0
            var waterXp = 0
            var sleepXp = 0
            var stepsXp = 0

            for ((_, dayData) in allDays) {
                runXp += (dayData["runXp"] as? Number)?.toInt() ?: 0
                waterXp += (dayData["waterXp"] as? Number)?.toInt() ?: 0
                sleepXp += (dayData["sleepXp"] as? Number)?.toInt() ?: 0
                stepsXp += (dayData["stepsXp"] as? Number)?.toInt() ?: 0
            }

            activity?.runOnUiThread {
                tvActivityXpBreakdown.text = buildString {
                    append("🏃 Run XP: $runXp\n")
                    append("💧 Water XP: $waterXp\n")
                    append("😴 Sleep XP: $sleepXp\n")
                    append("👟 Steps XP: $stepsXp")
                }
            }
        }
    }
}
