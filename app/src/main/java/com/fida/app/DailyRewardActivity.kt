package com.fida.app

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.fida.app.utils.FirestoreRepository
import com.fida.app.utils.PowerUpIndicator
import com.fida.app.utils.PowerUpManager
import com.fida.app.utils.PreferenceHelper
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class DailyRewardActivity : AppCompatActivity() {

    private lateinit var prefs: PreferenceHelper
    private var rewardClaimed = false

    private val uid: String by lazy {
        prefs.getUid() ?: ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_daily_reward)
        supportActionBar?.hide()

        prefs = PreferenceHelper(this)

        setupViews()
        PowerUpIndicator.bind(this, uid, findViewById(android.R.id.content))
        checkDailyRewardStatus()
    }

    private fun setupViews() {
        findViewById<MaterialButton>(R.id.btnClaimReward).setOnClickListener {
            if (!rewardClaimed) {
                claimDailyRewardWithBoost()
            }
        }

        findViewById<MaterialButton>(R.id.btnCloseReward).setOnClickListener {
            finish()
        }
    }

    private fun checkDailyRewardStatus() {
        val uid = prefs.getUid() ?: return
        FirestoreRepository.getUser(uid) { data ->
            if (data == null) return@getUser
            val lastRewardDate = data["lastRewardDate"] as? String ?: ""
            val today = java.time.LocalDate.now().toString()
            rewardClaimed = lastRewardDate == today

            runOnUiThread {
                if (rewardClaimed) {
                    findViewById<TextView>(R.id.tvRewardStatus).text = "You already claimed today's reward!"
                    findViewById<MaterialButton>(R.id.btnClaimReward).isEnabled = false
                    findViewById<MaterialButton>(R.id.btnClaimReward).text = "Already Claimed"
                }
            }
        }
    }

    private fun claimDailyRewardWithBoost() {
        lifecycleScope.launch {
            val powerUps = PowerUpManager.getActivePowerUps(uid)

            val baseCoins = baseCoinReward()
            val baseXp = baseXpReward()

            val boostedCoins = PowerUpManager.applyCoinBoost(baseCoins.toInt(), powerUps)
            val boostedXp = PowerUpManager.applyXpBoost(baseXp.toInt(), powerUps)

            val today = java.time.LocalDate.now().toString()

            FirestoreRepository.updateUserFields(uid, mapOf(
                "lastRewardDate" to today,
                "coins" to com.google.firebase.firestore.FieldValue.increment(boostedCoins.toLong()),
                "xp" to com.google.firebase.firestore.FieldValue.increment(boostedXp.toLong())
            )) { success ->
                if (success) {
                    rewardClaimed = true
                    PowerUpManager.clearExpiredPowerUps(uid, powerUps)

                    val prefs = PreferenceHelper(this@DailyRewardActivity)
                    val currentCoins = prefs.getInt("coins") ?: 0
                    val currentXp = prefs.getInt("xp") ?: 0
                    prefs.saveInt("coins", currentCoins + boostedCoins)
                    prefs.saveInt("xp", currentXp + boostedXp)

                    runOnUiThread {
                        val hasBoost = powerUps.hasCoinBoost || powerUps.hasXpBoost || powerUps.hasEnergyBoost
                        val message = if (hasBoost) {
                            val coinMultiplier = boostedCoins / baseCoins.toInt()
                            val xpMultiplier = boostedXp / baseXp.toInt()
                            "Boosted! $boostedCoins Coins (${coinMultiplier}x) & $boostedXp XP (${xpMultiplier}x)"
                        } else {
                            "Reward claimed! $boostedCoins Coins & $boostedXp XP"
                        }
                        findViewById<TextView>(R.id.tvRewardStatus).text = message
                        findViewById<MaterialButton>(R.id.btnClaimReward).isEnabled = false
                        findViewById<MaterialButton>(R.id.btnClaimReward).text = "Claimed!"
                    }
                }
            }
        }
    }

    private fun baseCoinReward(): Long {
        return 10L + (Math.random() * 20L).toLong()
    }

    private fun baseXpReward(): Long {
        return 5L + (Math.random() * 15L).toLong()
    }
}