package com.fida.app.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.fida.app.R
import com.fida.app.utils.FirestoreRepository
import com.fida.app.utils.PreferenceHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DailyRewardFragment : Fragment() {

    private lateinit var prefs: PreferenceHelper

    // 7-day reward cycle configuration
    private val rewardCycle = listOf(
        RewardDay(1, RewardType.COINS, 10),
        RewardDay(2, RewardType.COINS, 15),
        RewardDay(3, RewardType.COINS, 20),
        RewardDay(4, RewardType.SHIELD, 1),
        RewardDay(5, RewardType.COINS, 30),
        RewardDay(6, RewardType.GEMS, 3),
        RewardDay(7, RewardType.SPECIAL, 50) // 50 coins + 5 gems + 2 shields
    )

    data class RewardDay(val day: Int, val type: RewardType, val amount: Int)
    enum class RewardType { COINS, GEMS, SHIELD, SPECIAL }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_daily_reward, container, false)
        prefs = PreferenceHelper(requireContext())
        setupViews(view)
        return view
    }

    private fun setupViews(view: View) {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val lastClaimedDate = prefs.getString("lastClaimedDate")
        val currentDay = prefs.getInt("rewardDay") ?: 0

        val canClaim = today != lastClaimedDate
        val displayDay = if (canClaim) currentDay + 1 else currentDay
        val dayIndex = ((displayDay - 1) % 7)

        updateCalendarView(view, dayIndex, canClaim)
        updateCurrentRewardDisplay(view, dayIndex)
        updateNextRewardPreview(view, dayIndex)
        setupClaimButton(view, canClaim, today, currentDay)
    }

    private fun updateCalendarView(view: View, currentDayIndex: Int, canClaim: Boolean) {
        for (i in 0..6) {
            val dayContainerId = resources.getIdentifier("day${i + 1}Container", "id", requireContext().packageName)
            val dayContainer = view.findViewById<LinearLayout>(dayContainerId)

            when {
                i < currentDayIndex -> {
                    // Already claimed
                    dayContainer.setBackgroundResource(R.drawable.bg_reward_day_claimed)
                    dayContainer.findViewById<TextView>(resources.getIdentifier("tvDay${i + 1}", "id", requireContext().packageName))
                        ?.setTextColor(Color.parseColor("#4CAF50"))
                }
                i == currentDayIndex && canClaim -> {
                    // Current day, can claim
                    dayContainer.setBackgroundResource(R.drawable.bg_reward_day_current)
                }
                i == 6 -> {
                    // Day 7 special
                    dayContainer.setBackgroundResource(R.drawable.bg_reward_day_special)
                }
                else -> {
                    // Future days
                    dayContainer.setBackgroundResource(R.drawable.bg_reward_day_inactive)
                }
            }
        }
    }

    private fun updateCurrentRewardDisplay(view: View, dayIndex: Int) {
        val reward = rewardCycle[dayIndex]

        val tvCurrentRewardDay = view.findViewById<TextView>(R.id.tvCurrentRewardDay)
        val ivRewardIcon = view.findViewById<ImageView>(R.id.ivRewardIcon)
        val tvRewardAmount = view.findViewById<TextView>(R.id.tvRewardAmount)

        tvCurrentRewardDay.text = "Day ${reward.day} Reward"

        when (reward.type) {
            RewardType.COINS -> {
                ivRewardIcon.setImageResource(R.drawable.ic_coin)
                ivRewardIcon.setColorFilter(Color.parseColor("#414BB2"))
                tvRewardAmount.text = "+${reward.amount} Coins"
            }
            RewardType.GEMS -> {
                ivRewardIcon.setImageResource(R.drawable.ic_gem)
                ivRewardIcon.setColorFilter(null)
                tvRewardAmount.text = "+${reward.amount} Gems"
            }
            RewardType.SHIELD -> {
                ivRewardIcon.setImageResource(R.drawable.ic_shield)
                ivRewardIcon.setColorFilter(Color.parseColor("#4CAF50"))
                tvRewardAmount.text = "+${reward.amount} Streak Shield"
            }
            RewardType.SPECIAL -> {
                ivRewardIcon.setImageResource(R.drawable.ic_gem)
                ivRewardIcon.setColorFilter(Color.parseColor("#9C27B0"))
                tvRewardAmount.text = "+50 Coins, +5 Gems, +2 Shields!"
            }
        }
    }

    private fun updateNextRewardPreview(view: View, dayIndex: Int) {
        val tvNextRewardPreview = view.findViewById<TextView>(R.id.tvNextRewardPreview)

        if (dayIndex == 6) {
            tvNextRewardPreview.text = "Cycle resets: Day 1: +10 Coins"
        } else {
            val nextReward = rewardCycle[dayIndex + 1]
            val previewText = when (nextReward.type) {
                RewardType.COINS -> "Day ${nextReward.day}: +${nextReward.amount} Coins"
                RewardType.GEMS -> "Day ${nextReward.day}: +${nextReward.amount} Gems"
                RewardType.SHIELD -> "Day ${nextReward.day}: +${nextReward.amount} Shield"
                RewardType.SPECIAL -> "Day 7: Special Bonus!"
            }
            tvNextRewardPreview.text = previewText
        }
    }

    private fun setupClaimButton(view: View, canClaim: Boolean, today: String, currentDay: Int) {
        val btnClaimReward = view.findViewById<Button>(R.id.btnClaimReward)
        btnClaimReward.isEnabled = canClaim

        if (!canClaim) {
            btnClaimReward.text = "Claimed Today"
            btnClaimReward.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#9E9E9E"))
        }

        btnClaimReward.setOnClickListener {
            if (canClaim) {
                claimReward(currentDay)
            }
        }
    }

    private fun claimReward(currentDay: Int) {
        val uid = prefs.getUid() ?: return
        val newDay = currentDay + 1
        val dayIndex = ((newDay - 1) % 7)
        val reward = rewardCycle[dayIndex]

        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        prefs.saveString("lastClaimedDate", today)
        prefs.saveInt("rewardDay", newDay)

        // Award rewards via Firestore
        when (reward.type) {
            RewardType.COINS -> {
                FirestoreRepository.incrementUserField(uid, "coins", reward.amount.toLong()) {}
            }
            RewardType.GEMS -> {
                FirestoreRepository.incrementUserField(uid, "gems", reward.amount.toLong()) {}
            }
            RewardType.SHIELD -> {
                FirestoreRepository.incrementUserField(uid, "streakShields", reward.amount.toLong()) {}
            }
            RewardType.SPECIAL -> {
                // Special day 7: coins, gems, and shields
                FirestoreRepository.incrementUserField(uid, "coins", 50L) {}
                FirestoreRepository.incrementUserField(uid, "gems", 5L) {}
                FirestoreRepository.incrementUserField(uid, "streakShields", 2L) {}
            }
        }

        val message = when (reward.type) {
            RewardType.COINS -> "+${reward.amount} Coins claimed!"
            RewardType.GEMS -> "+${reward.amount} Gems claimed!"
            RewardType.SHIELD -> "+${reward.amount} Streak Shield claimed!"
            RewardType.SPECIAL -> "Special bonus claimed! +50 Coins, +5 Gems, +2 Shields!"
        }

        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()

        // Refresh the view
        view?.let { setupViews(it) }
    }
}