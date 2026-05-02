package com.fida.app.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import androidx.annotation.ColorRes

@Parcelize
data class ActivityLog(
    val id: String, // Unique identifier for the log entry
    val type: ActivityType,
    val timestamp: Long,
    val summary: String, // e.g., "Distance: 5.2 km | Duration: 30:15"
    @ColorRes val colorResId: Int // Resource ID for the color associated with the activity type
) : Parcelable {

    enum class ActivityType {
        RUN, WATER, SLEEP, OTHER // Add more types as needed
    }
}
