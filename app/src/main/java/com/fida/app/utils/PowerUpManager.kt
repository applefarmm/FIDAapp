package com.fida.app.utils

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object PowerUpManager {

    private val db = FirebaseFirestore.getInstance()

    data class ActivePowerUps(
        val coinBoosterExpiry: Long = 0,
        val xpBoosterExpiry: Long = 0,
        val energyBoosterExpiry: Long = 0,
        val tripleXpBoosterExpiry: Long = 0,
        val coinBooster24hExpiry: Long = 0,
        val superEnergyBoosterExpiry: Long = 0,
        val hasCoinBoost: Boolean = false,
        val hasXpBoost: Boolean = false,
        val hasEnergyBoost: Boolean = false,
        val hasTripleXpBoost: Boolean = false,
        val hasCoinBoost24h: Boolean = false,
        val hasSuperEnergyBoost: Boolean = false
    )

    suspend fun getActivePowerUps(uid: String): ActivePowerUps {
        if (uid.isEmpty()) return ActivePowerUps()

        try {
            val doc = db.collection("users").document(uid).get().await()
            if (!doc.exists()) return ActivePowerUps()

            val powerUps = doc.get("activePowerUps") as? Map<String, Any>
            val now = System.currentTimeMillis()

            val coinExpiry = (powerUps?.get("coinBoosterExpiry") as? Long) ?: 0
            val xpExpiry = (powerUps?.get("xpBoosterExpiry") as? Long) ?: 0
            val energyExpiry = (powerUps?.get("energyBoosterExpiry") as? Long) ?: 0
            val tripleXpExpiry = (powerUps?.get("tripleXpBoosterExpiry") as? Long) ?: 0
            val coin24hExpiry = (powerUps?.get("coinBooster24hExpiry") as? Long) ?: 0
            val superEnergyExpiry = (powerUps?.get("superEnergyBoosterExpiry") as? Long) ?: 0

            return ActivePowerUps(
                coinBoosterExpiry = coinExpiry,
                xpBoosterExpiry = xpExpiry,
                energyBoosterExpiry = energyExpiry,
                tripleXpBoosterExpiry = tripleXpExpiry,
                coinBooster24hExpiry = coin24hExpiry,
                superEnergyBoosterExpiry = superEnergyExpiry,
                hasCoinBoost = coinExpiry > now,
                hasXpBoost = xpExpiry > now,
                hasEnergyBoost = energyExpiry > now,
                hasTripleXpBoost = tripleXpExpiry > now,
                hasCoinBoost24h = coin24hExpiry > now,
                hasSuperEnergyBoost = superEnergyExpiry > now
            )
        } catch (e: Exception) {
            Log.e("PowerUpManager", "Failed to get power-ups: ${e.message}")
            return ActivePowerUps()
        }
    }

    fun applyCoinBoost(coins: Int, powerUps: ActivePowerUps): Int {
        var multiplier = 1
        if (powerUps.hasCoinBoost) multiplier *= 2
        if (powerUps.hasCoinBoost24h) multiplier *= 2
        if (powerUps.hasEnergyBoost) multiplier *= 2
        if (powerUps.hasSuperEnergyBoost) multiplier *= 3
        return coins * multiplier
    }

    fun applyXpBoost(xp: Int, powerUps: ActivePowerUps): Int {
        var multiplier = 1
        if (powerUps.hasXpBoost) multiplier *= 2
        if (powerUps.hasTripleXpBoost) multiplier *= 3
        if (powerUps.hasEnergyBoost) multiplier *= 2
        if (powerUps.hasSuperEnergyBoost) multiplier *= 3
        return xp * multiplier
    }

    fun clearExpiredPowerUps(uid: String, powerUps: ActivePowerUps) {
        if (uid.isEmpty()) return

        val now = System.currentTimeMillis()
        val updates = mutableMapOf<String, Any?>()

        if (!powerUps.hasCoinBoost && powerUps.coinBoosterExpiry > 0) {
            updates["activePowerUps.coinBoosterExpiry"] = 0
        }
        if (!powerUps.hasXpBoost && powerUps.xpBoosterExpiry > 0) {
            updates["activePowerUps.xpBoosterExpiry"] = 0
        }
        if (!powerUps.hasEnergyBoost && powerUps.energyBoosterExpiry > 0) {
            updates["activePowerUps.energyBoosterExpiry"] = 0
        }
        if (!powerUps.hasTripleXpBoost && powerUps.tripleXpBoosterExpiry > 0) {
            updates["activePowerUps.tripleXpBoosterExpiry"] = 0
        }
        if (!powerUps.hasCoinBoost24h && powerUps.coinBooster24hExpiry > 0) {
            updates["activePowerUps.coinBooster24hExpiry"] = 0
        }
        if (!powerUps.hasSuperEnergyBoost && powerUps.superEnergyBoosterExpiry > 0) {
            updates["activePowerUps.superEnergyBoosterExpiry"] = 0
        }

        if (updates.isNotEmpty()) {
            db.collection("users").document(uid).update(updates)
                .addOnSuccessListener { Log.d("PowerUpManager", "Expired power-ups cleared") }
                .addOnFailureListener { e -> Log.e("PowerUpManager", "Failed to clear: ${e.message}") }
        }
    }

    fun consumeOneTimeBooster(uid: String, boosterType: String) {
        if (uid.isEmpty()) return

        db.collection("users").document(uid)
            .update("activePowerUps.${boosterType}Expiry", 0)
            .addOnSuccessListener { Log.d("PowerUpManager", "$boosterType consumed") }
            .addOnFailureListener { e -> Log.e("PowerUpManager", "Failed to consume: ${e.message}") }
    }
}