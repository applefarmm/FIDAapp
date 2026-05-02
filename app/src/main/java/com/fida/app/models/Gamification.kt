package com.fida.app.models

data class Gamification(
    val title: String,
    val description: String,
    val icon: String,
    val xpReward: Int,
    val coinReward: Int,
    val type: String, // e.g., level_up, challenge_complete, badge_earned
    var isActive: Boolean = true,
    var isCompleted: Boolean = false
)
