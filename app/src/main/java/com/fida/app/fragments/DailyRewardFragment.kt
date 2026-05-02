package com.fida.app.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.fida.app.R
import com.fida.app.utils.PreferenceHelper

class DailyRewardFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?):
            View? {
        val view = inflater.inflate(R.layout.fragment_daily_reward, container, false)
        setupViews(view)
        return view
    }

    private fun setupViews(view: View) {
        val tvRewardCalendarTitle = view.findViewById<TextView>(R.id.tvRewardCalendarTitle)
        val tvCurrentRewardDay = view.findViewById<TextView>(R.id.tvCurrentRewardDay)
        val ivRewardIcon = view.findViewById<ImageView>(R.id.ivRewardIcon)
        val tvRewardAmount = view.findViewById<TextView>(R.id.tvRewardAmount)
        val btnClaimReward = view.findViewById<Button>(R.id.btnClaimReward)
        val tvTomorrowRewardLabel = view.findViewById<TextView>(R.id.tvTomorrowRewardLabel)
        val ivTomorrowRewardIcon = view.findViewById<ImageView>(R.id.ivTomorrowRewardIcon)
        val tvTomorrowRewardAmount = view.findViewById<TextView>(R.id.tvTomorrowRewardAmount)

        val prefs = PreferenceHelper(requireContext())
        val rewardDay = prefs.getInt("rewardDay") ?: 1 // Current day in the reward cycle
        val currentShields = prefs.getInt("streakShields") ?: 0

        // Update UI for today's reward
        tvCurrentRewardDay.text = "Day $rewardDay"
        when (rewardDay) {
            1 -> {
                ivRewardIcon.setImageResource(R.drawable.ic_coin) // Example icon
                tvRewardAmount.text = "+10 Coins"
            }
            2 -> {
                ivRewardIcon.setImageResource(R.drawable.ic_shield) // Example icon
                tvRewardAmount.text = "+1 Shield"
            }
            3 -> {
                ivRewardIcon.setImageResource(R.drawable.ic_xp) // Example icon
                tvRewardAmount.text = "+50 XP"
            }
            // Add more days as needed
            else -> {
                ivRewardIcon.setImageResource(R.drawable.ic_coin) // Default
                tvRewardAmount.text = "+10 Coins"
            }
        }

        // Enable claim button only for the current day's reward
        btnClaimReward.isEnabled = true // In a real scenario, check if already claimed today
        btnClaimReward.setOnClickListener {
            // TODO: Implement claiming logic - add coins/gems/shields to user profile
            if (rewardDay == 2) { // If today's reward is a shield
                prefs.saveInt("streakShields", currentShields + 1)
            }
            Toast.makeText(context, "Reward Claimed!", Toast.LENGTH_SHORT).show()
            // Advance to next day or reset cycle
            prefs.saveInt("rewardDay", (rewardDay % 7) + 1) // Cycle through 7 days
            updateUIForNextDay()
        }

        // Show tomorrow's reward teaser
        val tomorrowRewardDay = (rewardDay % 7) + 1
        tvTomorrowRewardLabel.text = "Tomorrow: Day $tomorrowRewardDay"
        when (tomorrowRewardDay) {
            1 -> { ivTomorrowRewardIcon.setImageResource(R.drawable.ic_coin); tvTomorrowRewardAmount.text = "+10 Coins" }
            2 -> { ivTomorrowRewardIcon.setImageResource(R.drawable.ic_shield); tvTomorrowRewardAmount.text = "+1 Shield" }
            3 -> { ivTomorrowRewardIcon.setImageResource(R.drawable.ic_xp); tvTomorrowRewardAmount.text = "+50 XP" }
            else -> { ivTomorrowRewardIcon.setImageResource(R.drawable.ic_coin); tvTomorrowRewardAmount.text = "+10 Coins" }
        }
    }

    private fun updateUIForNextDay() {
        val view = view ?: return
        val prefs = PreferenceHelper(requireContext())
        val rewardDay = prefs.getInt("rewardDay") ?: 1
        val currentShields = prefs.getInt("streakShields") ?: 0

        view.findViewById<TextView>(R.id.tvCurrentRewardDay).text = "Day $rewardDay"
        when (rewardDay) {
            1 -> { view.findViewById<ImageView>(R.id.ivRewardIcon).setImageResource(R.drawable.ic_coin); view.findViewById<TextView>(R.id.tvRewardAmount).text = "+10 Coins" }
            2 -> { view.findViewById<ImageView>(R.id.ivRewardIcon).setImageResource(R.drawable.ic_shield); view.findViewById<TextView>(R.id.tvRewardAmount).text = "+1 Shield" }
            3 -> { view.findViewById<ImageView>(R.id.ivRewardIcon).setImageResource(R.drawable.ic_xp); view.findViewById<TextView>(R.id.tvRewardAmount).text = "+50 XP" }
            else -> { view.findViewById<ImageView>(R.id.ivRewardIcon).setImageResource(R.drawable.ic_coin); view.findViewById<TextView>(R.id.tvRewardAmount).text = "+10 Coins" }
        }

        view.findViewById<Button>(R.id.btnClaimReward).isEnabled = true

        val tomorrowRewardDay = (rewardDay % 7) + 1
        view.findViewById<TextView>(R.id.tvTomorrowRewardLabel).text = "Tomorrow: Day $tomorrowRewardDay"
        when (tomorrowRewardDay) {
            1 -> { view.findViewById<ImageView>(R.id.ivTomorrowRewardIcon).setImageResource(R.drawable.ic_coin); view.findViewById<TextView>(R.id.tvTomorrowRewardAmount).text = "+10 Coins" }
            2 -> { view.findViewById<ImageView>(R.id.ivTomorrowRewardIcon).setImageResource(R.drawable.ic_shield); view.findViewById<TextView>(R.id.tvTomorrowRewardAmount).text = "+1 Shield" }
            3 -> { view.findViewById<ImageView>(R.id.ivTomorrowRewardIcon).setImageResource(R.drawable.ic_xp); view.findViewById<TextView>(R.id.tvTomorrowRewardAmount).text = "+50 XP" }
            else -> { view.findViewById<ImageView>(R.id.ivTomorrowRewardIcon).setImageResource(R.drawable.ic_coin); view.findViewById<TextView>(R.id.tvTomorrowRewardAmount).text = "+10 Coins" }
        }
    }
}
