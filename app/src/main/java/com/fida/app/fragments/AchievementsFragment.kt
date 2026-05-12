package com.fida.app.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fida.app.R
import com.fida.app.adapters.AchievementsAdapter
import com.fida.app.models.Achievement
import com.fida.app.utils.FirestoreRepository
import com.fida.app.utils.PreferenceHelper

class AchievementsFragment : Fragment() {

    private lateinit var prefs: PreferenceHelper
    private lateinit var achievementsAdapter: AchievementsAdapter
    private val achievements = mutableListOf<Achievement>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_achievements, container, false)
        prefs = PreferenceHelper(requireContext())
        setupViews(view)
        loadAchievements()
        return view
    }

    private fun setupViews(view: View) {
        val rvAchievements = view.findViewById<RecyclerView>(R.id.rvAchievements)
        achievementsAdapter = AchievementsAdapter(achievements)
        rvAchievements.layoutManager = GridLayoutManager(context, 3)
        rvAchievements.adapter = achievementsAdapter
    }

    private fun loadAchievements() {
        val uid = prefs.getUid() ?: return

        FirestoreRepository.getUserAchievements(uid) { userBadges ->
            FirestoreRepository.getGlobalAchievements { globalAchievements ->
                if (globalAchievements != null) {
                    achievements.clear()
                    val unlockedIds = userBadges?.keys ?: emptySet()

                    globalAchievements.map { data ->
                        val achievementId = data["id"] as? String ?: ""
                        Achievement(
                            title = data["title"] as? String ?: "",
                            description = data["description"] as? String ?: "",
                            iconUrl = data["iconUrl"] as? String ?: "",
                            isUnlocked = unlockedIds.contains(achievementId) ||
                                (userBadges?.get(achievementId) as? Boolean ?: false)
                        )
                    }.also { mappedList ->
                        achievements.addAll(mappedList)
                    }

                    activity?.runOnUiThread {
                        achievementsAdapter.notifyDataSetChanged()
                    }
                } else {
                    loadDefaultAchievements()
                }
            }
        }
    }

    private fun loadDefaultAchievements() {
        achievements.clear()
        achievements.addAll(listOf(
            Achievement("First Run", "Complete your first run", "", true),
            Achievement("5k Runner", "Run 5 kilometers in a single session", "", false),
            Achievement("Daily Streak 7", "Maintain a 7-day streak", "", false),
            Achievement("Marathoner", "Log 42km total distance", "", true),
            Achievement("Calorie Burner", "Burn 500 calories in one session", "", false),
            Achievement("Hydration Hero", "Drink 8 glasses of water daily", "", true),
            Achievement("Sleep Master", "Sleep 8+ hours", "", false),
            Achievement("Early Bird", "Log activity before 7 AM", "", false),
            Achievement("Night Owl", "Log activity after 9 PM", "", false),
            Achievement("Week Warrior", "Complete 7 days of tracking", "", true),
            Achievement("Speed Demon", "Run 5km in under 25 minutes", "", false),
            Achievement("Consistency King", "30-day streak", "", false)
        ))
        activity?.runOnUiThread {
            achievementsAdapter.notifyDataSetChanged()
        }
    }
}