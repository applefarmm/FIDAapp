package com.fida.app.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.fida.app.DailyRewardActivity
import com.fida.app.HomeActivity
import com.fida.app.R
import com.fida.app.RecordSleepActivity
import com.fida.app.PreRunActivity
import com.fida.app.WaterIntakeActivity
import com.fida.app.StepCounterActivity
import com.fida.app.utils.FirestoreRepository
import com.fida.app.utils.PreferenceHelper
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.LinearProgressIndicator
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class DashboardFragment : Fragment() {

    private var currentWater = 0
    private var maxWater = 8
    private var currentSleep = 0f
    private val targetSleep = 8f // 8 hours target
    private var currentSteps = 0
    private var maxSteps = 2500
    private val STEPS_TO_KM = 1312.335958f // ~1312 steps = 1 km

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.fragment_dashboard, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val prefs = PreferenceHelper(requireContext())
        val uid = prefs.getUid() ?: return

        // Greeting
        val hour = LocalTime.now().hour
        val greeting = when {
            hour < 12 -> "Good morning!"
            hour < 17 -> "Good afternoon!"
            else -> "Good evening!"
        }
        view.findViewById<TextView>(R.id.tvGreeting).text = greeting
        view.findViewById<TextView>(R.id.tvUsername).text = prefs.getUsername() ?: "Athlete"

        // Load user data
        loadUserData(view, uid)

        // Card navigation
        view.findViewById<CardView>(R.id.cardRun).setOnClickListener {
            startActivity(Intent(requireContext(), PreRunActivity::class.java))
        }
        view.findViewById<CardView>(R.id.cardWater).setOnClickListener {
            startActivity(Intent(requireContext(), WaterIntakeActivity::class.java))
        }
        view.findViewById<CardView>(R.id.cardSleep).setOnClickListener {
            val intent = Intent(requireContext(), RecordSleepActivity::class.java)
            intent.putExtra("username", PreferenceHelper(requireContext()).getUid() ?: "")
            startActivity(intent)
        }
        view.findViewById<CardView>(R.id.cardSteps).setOnClickListener {
            startActivity(Intent(requireContext(), StepCounterActivity::class.java))
        }
        view.findViewById<MaterialButton>(R.id.btnViewSteps).setOnClickListener {
            startActivity(Intent(requireContext(), StepCounterActivity::class.java))
        }
        view.findViewById<MaterialButton>(R.id.btnDailyReward).setOnClickListener {
            startActivity(Intent(requireContext(), DailyRewardActivity::class.java))
        }

        // Quick log buttons
        view.findViewById<MaterialButton>(R.id.btnQuickWater).setOnClickListener {
            quickAddWater(uid, view)
        }
        view.findViewById<MaterialButton>(R.id.btnQuickSleep).setOnClickListener {
            quickAddSleep(uid, view)
        }
    }

    override fun onResume() {
        super.onResume()
        // Reload data when returning from other activities
        val prefs = PreferenceHelper(requireContext())
        val uid = prefs.getUid() ?: return
        view?.let { loadUserData(it, uid) }
    }

    private fun loadUserData(view: View, uid: String) {
        FirestoreRepository.getUser(uid) { data ->
            if (data == null || !isAdded) return@getUser
            activity?.runOnUiThread {
                val level = (data["level"] as? Long)?.toInt() ?: 1
                val xp = (data["xp"] as? Long)?.toInt() ?: 0
                val maxXp = (data["maxXp"] as? Long)?.toInt() ?: 100
                val coins = (data["coins"] as? Long)?.toInt() ?: 0
                val streak = (data["streakDays"] as? Long)?.toInt() ?: 0
                maxWater = (data["maxWater"] as? Long)?.toInt() ?: 8
                maxSteps = (data["maxStep"] as? Long)?.toInt() ?: 2500

                view.findViewById<TextView>(R.id.tvLevel).text = "Level $level"
                view.findViewById<TextView>(R.id.tvXp).text = "$xp XP"
                view.findViewById<TextView>(R.id.tvMaxXp).text = "$maxXp XP"
                view.findViewById<TextView>(R.id.tvCoins).text = "🪙 $coins"
                view.findViewById<TextView>(R.id.tvStreak).text = streak.toString()

                val xpBar = view.findViewById<LinearProgressIndicator>(R.id.xpBar)
                xpBar.max = maxXp
                xpBar.setProgressCompat(xp, true)

                // Today's data
                val today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                @Suppress("UNCHECKED_CAST")
                val allDays = data["allDays"] as? Map<String, Any> ?: emptyMap()
                val todayData = allDays[today] as? Map<String, Any>

                currentWater = (todayData?.get("waterCounter") as? Long)?.toInt() ?: 0
                view.findViewById<TextView>(R.id.tvWaterSummary).text = "$currentWater / $maxWater glasses"

                // Water progress bar
                val waterBar = view.findViewById<LinearProgressIndicator>(R.id.waterProgressBar)
                waterBar.max = maxWater
                waterBar.setProgressCompat(currentWater, true)

                currentSleep = todayData?.get("sleepTime")?.toString()?.toFloatOrNull() ?: 0f
                view.findViewById<TextView>(R.id.tvSleepSummary).text =
                    if (currentSleep > 0) "%.1f hrs logged".format(currentSleep) else "No sleep logged"

                // Sleep progress bar (target 8 hours)
                val sleepBar = view.findViewById<LinearProgressIndicator>(R.id.sleepProgressBar)
                sleepBar.max = targetSleep.toInt()
                sleepBar.setProgressCompat(currentSleep.toInt().coerceAtMost(targetSleep.toInt()), true)

                // Steps data
                currentSteps = (todayData?.get("stepCounter") as? Long)?.toInt() ?: 0
                val distanceKm = currentSteps / STEPS_TO_KM
                view.findViewById<TextView>(R.id.tvStepsSummary).text = "$currentSteps / $maxSteps steps"
                view.findViewById<TextView>(R.id.tvStepsDistance).text = "%.2f km".format(distanceKm)

                // Steps progress bar
                val stepsBar = view.findViewById<LinearProgressIndicator>(R.id.stepsProgressBar)
                stepsBar.max = maxSteps
                stepsBar.setProgressCompat(currentSteps.coerceAtMost(maxSteps), true)
            }
        }
    }

    private fun quickAddWater(uid: String, view: View) {
        currentWater++
        FirestoreRepository.logDailyActivity(uid, "waterCounter", currentWater)

        // Update UI
        view.findViewById<TextView>(R.id.tvWaterSummary).text = "$currentWater / $maxWater glasses"
        val waterBar = view.findViewById<LinearProgressIndicator>(R.id.waterProgressBar)
        waterBar.setProgressCompat(currentWater.coerceAtMost(maxWater), true)

        // Award XP for water logging
        if (currentWater <= maxWater) {
            FirestoreRepository.incrementUserField(uid, "xp", 5L) { success ->
                if (success && isAdded) {
                    activity?.runOnUiThread {
                        Toast.makeText(requireContext(), "+1 Glass logged! +5 XP", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } else {
            Toast.makeText(requireContext(), "Daily goal reached! 🎉", Toast.LENGTH_SHORT).show()
        }
    }

    private fun quickAddSleep(uid: String, view: View) {
        currentSleep += 1f
        FirestoreRepository.logDailyActivity(uid, "sleepTime", currentSleep)

        // Update UI
        view.findViewById<TextView>(R.id.tvSleepSummary).text = "%.1f hrs logged".format(currentSleep)
        val sleepBar = view.findViewById<LinearProgressIndicator>(R.id.sleepProgressBar)
        sleepBar.setProgressCompat(currentSleep.toInt().coerceAtMost(targetSleep.toInt()), true)

        // Award XP for sleep logging
        if (currentSleep <= targetSleep) {
            FirestoreRepository.incrementUserField(uid, "xp", 10L) { success ->
                if (success && isAdded) {
                    activity?.runOnUiThread {
                        Toast.makeText(requireContext(), "+1 Hour logged! +10 XP", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } else {
            Toast.makeText(requireContext(), "Sleep goal reached! 🎉", Toast.LENGTH_SHORT).show()
        }
    }
}
