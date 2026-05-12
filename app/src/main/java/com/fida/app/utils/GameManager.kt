package com.fida.app.utils

import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object GameManager {

    private val db = FirebaseFirestore.getInstance()
    private val fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    // XP thresholds: level N requires N*100 + (N-1)*50 XP
    fun xpForLevel(level: Int): Int = level * 100 + (level - 1) * 50

    fun awardXp(uid: String, amount: Int, context: Context, onLevelUp: ((Int) -> Unit)? = null) {
        val ref = db.collection("users").document(uid)
        ref.get().addOnSuccessListener { doc ->
            if (!doc.exists()) return@addOnSuccessListener
            var xp = doc.getLong("xp")?.toInt() ?: 0
            var level = doc.getLong("level")?.toInt() ?: 1
            var coins = doc.getLong("coins")?.toInt() ?: 0
            var maxXp = xpForLevel(level)

            xp += amount
            while (xp >= maxXp) {
                xp -= maxXp
                level++
                coins += 50
                maxXp = xpForLevel(level)
                onLevelUp?.invoke(level)
            }

            ref.update(
                "xp", xp,
                "level", level,
                "maxXp", maxXp,
                "coins", coins
            )
        }
    }

    fun awardCoins(uid: String, amount: Int) {
        val ref = db.collection("users").document(uid)
        ref.get().addOnSuccessListener { doc ->
            val coins = (doc.getLong("coins")?.toInt() ?: 0) + amount
            ref.update("coins", coins)
        }
    }

    fun updateStreak(uid: String) {
        val today = LocalDate.now().format(fmt)
        val yesterday = LocalDate.now().minusDays(1).format(fmt)
        val ref = db.collection("users").document(uid)
        ref.get().addOnSuccessListener { doc ->
            val last = doc.getString("lastActiveDate") ?: ""
            var streak = doc.getLong("streakDays")?.toInt() ?: 0
            var shields = doc.getLong("streakShields")?.toInt() ?: 0

            when (last) {
                today -> return@addOnSuccessListener
                yesterday -> streak++
                else -> {
                    if (shields > 0) {
                        shields--
                        streak++
                    } else {
                        streak = 1
                    }
                }
            }

            ref.update(
                "streakDays", streak,
                "streakShields", shields,
                "lastActiveDate", today
            )
        }
    }

    fun unlockBadge(uid: String, badgeId: String) {
        val ref = db.collection("users").document(uid)
        ref.update("badges.$badgeId", true)
    }

    // Badge definitions
    val allBadges = listOf(
        Badge("first_run", "First Steps", "Complete your first run", "🏃"),
        Badge("run_5", "Road Runner", "Complete 5 runs", "🏅"),
        Badge("run_10", "Marathon Starter", "Complete 10 runs", "🥈"),
        Badge("water_7", "Hydration Hero", "Hit water goal 7 days in a row", "💧"),
        Badge("sleep_7", "Sleep Champion", "Log sleep 7 days in a row", "😴"),
        Badge("streak_7", "Week Warrior", "Maintain a 7-day streak", "🔥"),
        Badge("streak_30", "Monthly Master", "Maintain a 30-day streak", "⚡"),
        Badge("level_5", "Rising Star", "Reach level 5", "⭐"),
        Badge("level_10", "Elite Athlete", "Reach level 10", "🏆"),
        Badge("steps_10k", "10K Steps", "Walk 10,000 steps in a day", "👟")
    )
}

data class Badge(
    val id: String,
    val title: String,
    val description: String,
    val emoji: String,
    var unlocked: Boolean = false
)
