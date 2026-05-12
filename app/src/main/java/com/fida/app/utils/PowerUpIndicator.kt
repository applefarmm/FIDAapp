package com.fida.app.utils

import android.view.View
import com.fida.app.R
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

object PowerUpIndicator {

    fun bind(
        lifecycleOwner: LifecycleOwner,
        uid: String,
        rootView: View
    ) {
        lifecycleOwner.lifecycleScope.launch {
            val powerUps = PowerUpManager.getActivePowerUps(uid)
            updateIndicator(rootView, powerUps)
        }
    }

    private fun updateIndicator(rootView: View, powerUps: PowerUpManager.ActivePowerUps) {
        val indicator = rootView.findViewById<LinearLayout>(R.id.powerUpIndicator)
        if (indicator == null) return

        val hasAnyBoost = powerUps.hasCoinBoost || powerUps.hasXpBoost || powerUps.hasEnergyBoost ||
            powerUps.hasTripleXpBoost || powerUps.hasCoinBoost24h || powerUps.hasSuperEnergyBoost

        if (!hasAnyBoost) {
            indicator.visibility = View.GONE
            return
        }

        indicator.visibility = View.VISIBLE

        val ivCoinBoost = rootView.findViewById<ImageView>(R.id.ivCoinBoost)
        val tvCoinMultiplier = rootView.findViewById<TextView>(R.id.tvCoinMultiplier)
        val ivXpBoost = rootView.findViewById<ImageView>(R.id.ivXpBoost)
        val tvXpMultiplier = rootView.findViewById<TextView>(R.id.tvXpMultiplier)
        val ivEnergyBoost = rootView.findViewById<ImageView>(R.id.ivEnergyBoost)
        val tvEnergyMultiplier = rootView.findViewById<TextView>(R.id.tvEnergyMultiplier)
        val tvTimeRemaining = rootView.findViewById<TextView>(R.id.tvTimeRemaining)

        var coinMultiplier = 1
        var xpMultiplier = 1

        // Show coin boost (2x)
        if (powerUps.hasCoinBoost || powerUps.hasCoinBoost24h) {
            ivCoinBoost?.visibility = View.VISIBLE
            tvCoinMultiplier?.visibility = View.VISIBLE
            if (powerUps.hasCoinBoost) coinMultiplier *= 2
            if (powerUps.hasCoinBoost24h) coinMultiplier *= 2
        } else {
            ivCoinBoost?.visibility = View.GONE
            tvCoinMultiplier?.visibility = View.GONE
        }

        // Show XP boost (2x or 3x)
        if (powerUps.hasXpBoost || powerUps.hasTripleXpBoost) {
            ivXpBoost?.visibility = View.VISIBLE
            tvXpMultiplier?.visibility = View.VISIBLE
            if (powerUps.hasXpBoost) xpMultiplier *= 2
            if (powerUps.hasTripleXpBoost) xpMultiplier *= 3
        } else {
            ivXpBoost?.visibility = View.GONE
            tvXpMultiplier?.visibility = View.GONE
        }

        // Show energy/super energy boost (applies to both)
        if (powerUps.hasEnergyBoost || powerUps.hasSuperEnergyBoost) {
            ivEnergyBoost?.visibility = View.VISIBLE
            tvEnergyMultiplier?.visibility = View.VISIBLE
            if (powerUps.hasEnergyBoost) {
                coinMultiplier *= 2
                xpMultiplier *= 2
            }
            if (powerUps.hasSuperEnergyBoost) {
                coinMultiplier *= 3
                xpMultiplier *= 3
            }
            tvEnergyMultiplier?.text = if (powerUps.hasSuperEnergyBoost) "3x" else "2x"
        } else {
            ivEnergyBoost?.visibility = View.GONE
            tvEnergyMultiplier?.visibility = View.GONE
        }

        // Update multiplier text
        tvCoinMultiplier?.text = "${coinMultiplier}x"
        tvXpMultiplier?.text = "${xpMultiplier}x"

        // Show time remaining - find earliest expiry
        val expiryList = mutableListOf<Long>()
        if (powerUps.hasCoinBoost) expiryList.add(powerUps.coinBoosterExpiry)
        if (powerUps.hasXpBoost) expiryList.add(powerUps.xpBoosterExpiry)
        if (powerUps.hasEnergyBoost) expiryList.add(powerUps.energyBoosterExpiry)
        if (powerUps.hasTripleXpBoost) expiryList.add(powerUps.tripleXpBoosterExpiry)
        if (powerUps.hasCoinBoost24h) expiryList.add(powerUps.coinBooster24hExpiry)
        if (powerUps.hasSuperEnergyBoost) expiryList.add(powerUps.superEnergyBoosterExpiry)

        val earliestExpiry = expiryList.minOrNull()

        if (earliestExpiry != null) {
            val remainingMs = earliestExpiry - System.currentTimeMillis()
            val remainingHours = remainingMs / (60 * 60 * 1000)
            val remainingMins = (remainingMs / (60 * 1000)) % 60

            tvTimeRemaining?.visibility = View.VISIBLE
            tvTimeRemaining?.text = if (remainingHours > 0) {
                "${remainingHours}h ${remainingMins}m left"
            } else {
                "${remainingMins}m left"
            }
        } else {
            tvTimeRemaining?.visibility = View.GONE
        }
    }
}