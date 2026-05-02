package com.fida.app.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Run(
    val timestamp: Long,
    val durationSeconds: Int,
    val distanceMeters: Float,
    val goalType: Int, // 0: distance, 1: time, 2: calories
    val goalValue: Float,
    val completed: Boolean
) : Parcelable
