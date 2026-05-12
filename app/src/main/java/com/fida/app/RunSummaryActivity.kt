package com.fida.app

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.fida.app.utils.FirestoreRepository
import com.fida.app.utils.PowerUpIndicator
import com.fida.app.utils.PowerUpManager
import com.fida.app.utils.PreferenceHelper
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

class RunSummaryActivity : AppCompatActivity() {

    private lateinit var prefs: PreferenceHelper

    private val uid: String by lazy {
        intent.getStringExtra("uid")
            ?: PreferenceHelper(this).getUid()
            ?: ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_run_summary)
        supportActionBar?.hide()

        prefs = PreferenceHelper(this)

        val duration = intent.getIntExtra("duration", 0)
        val distance = intent.getFloatExtra("distance", 0f)
        val goalReached = intent.getBooleanExtra("goalReached", false)

        val calories = intent.getIntExtra("calories", 0)

        setupViews(duration, distance, goalReached, calories)
        PowerUpIndicator.bind(this, uid, findViewById(android.R.id.content))
        awardRewardsWithBoost(distance, goalReached)
    }

    private fun setupViews(duration: Int, distance: Float, goalReached: Boolean, calories: Int) {
        val tvRunDuration = findViewById<TextView>(R.id.tvRunDuration)
        val tvRunDistance = findViewById<TextView>(R.id.tvRunDistance)
        val tvRunPace = findViewById<TextView>(R.id.tvRunPace)
        val tvRunCalories = findViewById<TextView>(R.id.tvRunCalories)
        val tvGoalStatus = findViewById<TextView>(R.id.tvGoalStatus)
        val btnBackToHome = findViewById<MaterialButton>(R.id.btnBackToHome)
        val btnViewHistory = findViewById<MaterialButton>(R.id.btnViewHistory)

        tvRunDuration.text = formatDuration(duration)
        val distanceKm = distance / 1000
        tvRunDistance.text = "%.2f km".format(distanceKm)

        // Calculate Pace (min/km)
        if (distanceKm > 0) {
            val totalMinutes = duration / 60.0
            val paceDecimal = totalMinutes / distanceKm
            val paceMins = paceDecimal.toInt()
            val paceSecs = ((paceDecimal - paceMins) * 60).toInt()
            tvRunPace.text = "%d'%02d\"/km".format(paceMins, paceSecs)
        } else {
            tvRunPace.text = "0'00\"/km"
        }

        tvRunCalories.text = "$calories kcal"
        tvGoalStatus.text = if (goalReached) "Goal Achieved!" else "Keep pushing!"

        btnBackToHome.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }

        btnViewHistory.setOnClickListener {
            startActivity(Intent(this, RunHistoryActivity::class.java))
        }
    }

    private fun awardRewardsWithBoost(distance: Float, goalReached: Boolean) {
        lifecycleScope.launch {
            val powerUps = PowerUpManager.getActivePowerUps(uid)

            val baseXp = (distance / 100).toInt() * 10
            val baseCoins = if (goalReached) 20 else 10

            val boostedXp = PowerUpManager.applyXpBoost(baseXp, powerUps)
            val boostedCoins = PowerUpManager.applyCoinBoost(baseCoins, powerUps)

            val currentXp = prefs.getInt("xp") ?: 0
            val currentCoins = prefs.getInt("coins") ?: 0

            prefs.saveInt("xp", currentXp + boostedXp)
            prefs.saveInt("coins", currentCoins + boostedCoins)

            FirestoreRepository.incrementUserField(uid, "xp", boostedXp.toLong()) { }
            FirestoreRepository.incrementUserField(uid, "coins", boostedCoins.toLong()) { }

            PowerUpManager.clearExpiredPowerUps(uid, powerUps)

            val message = buildRewardMessage(baseXp, boostedXp, baseCoins, boostedCoins, powerUps)
            Toast.makeText(this@RunSummaryActivity, message, Toast.LENGTH_LONG).show()
        }
    }

    private fun buildRewardMessage(
        baseXp: Int,
        boostedXp: Int,
        baseCoins: Int,
        boostedCoins: Int,
        powerUps: PowerUpManager.ActivePowerUps
    ): String {
        val hasBoost = powerUps.hasXpBoost || powerUps.hasCoinBoost || powerUps.hasEnergyBoost
        return if (hasBoost) {
            val xpMultiplier = boostedXp / baseXp
            val coinMultiplier = boostedCoins / baseCoins
            "Boosted! $boostedXp XP (${xpMultiplier}x) & $boostedCoins Coins (${coinMultiplier}x)"
        } else {
            "You earned $boostedXp XP and $boostedCoins Coins!"
        }
    }

    private fun formatDuration(seconds: Int): String {
        val mins = seconds / 60
        val secs = seconds % 60
        return "%02d:%02d".format(mins, secs)
    }
}