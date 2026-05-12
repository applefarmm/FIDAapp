package com.fida.app.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Challenge(
    val id: String = "",
    val title: String,
    val description: String,
    val icon: String,
    val xpReward: Int,
    val coinReward: Int,
    val gemReward: Int = 0,
    val type: String, // daily, weekly, special
    val targetValue: Int, // e.g., 5 runs, 10km, 7 days
    val progress: Int = 0,
    var isActive: Boolean = false,
    var isCompleted: Boolean = false,
    val startDate: Long = 0,
    val endDate: Long = 0
) : Parcelable