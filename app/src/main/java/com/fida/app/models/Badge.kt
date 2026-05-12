package com.fida.app.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Badge(
    val id: String = "",
    val title: String,
    val description: String,
    val iconUrl: String,
    val category: String, // running, water, sleep, streak, general
    val requirement: String, // e.g., "Complete 10 runs"
    val xpReward: Int = 0,
    val coinReward: Int = 0,
    val gemReward: Int = 0,
    val rarity: String = "common", // common, rare, epic, legendary
    val isUnlocked: Boolean = false,
    val unlockedDate: Long = 0,
    val progress: Int = 0,
    val targetProgress: Int = 1
) : Parcelable {
    val progressPercent: Int
        get() = if (targetProgress > 0) (progress * 100) / targetProgress else 0
}