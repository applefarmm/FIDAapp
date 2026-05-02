package com.fida.app.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.fida.app.R
import com.fida.app.models.Achievement

class AchievementDetailFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?):
            View? {
        val view = inflater.inflate(R.layout.fragment_achievement_detail, container, false)
        setupViews(view)
        return view
    }

    private fun setupViews(view: View) {
        val ivAchievementIcon = view.findViewById<ImageView>(R.id.ivAchievementIconDetail)
        val tvAchievementTitle = view.findViewById<TextView>(R.id.tvAchievementTitleDetail)
        val tvAchievementDescription = view.findViewById<TextView>(R.id.tvAchievementDescriptionDetail)
        val tvUnlockProgress = view.findViewById<TextView>(R.id.tvUnlockProgress)
        val tvReward = view.findViewById<TextView>(R.id.tvReward)

        // TODO: Get Achievement object passed via arguments or activity
        // For now, using placeholder data
        val achievement = Achievement("First Run", "Complete your first run", "https://example.com/icon_first_run.png", true)

        if (achievement.isUnlocked) {
            Glide.with(this).load(achievement.iconUrl).into(ivAchievementIcon)
            tvAchievementTitle.text = achievement.title
            tvAchievementDescription.text = achievement.description
            tvUnlockProgress.visibility = View.GONE // Hide progress if unlocked
            tvReward.text = "Reward: 50 XP, 20 Coins"
        } else {
            // Show locked state
            ivAchievementIcon.setColorFilter(resources.getColor(R.color.grey_500, null))
            tvAchievementTitle.text = "Locked Achievement"
            tvAchievementDescription.text = "Complete actions to unlock"
            tvUnlockProgress.visibility = View.VISIBLE
            tvUnlockProgress.text = "Progress: 75%"
            tvReward.text = "Reward: 50 XP, 20 Coins (upon unlock)"
        }
    }
}
