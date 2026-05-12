package com.fida.app.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Achievement(
    val title: String,
    val description: String,
    val iconUrl: String,
    val isUnlocked: Boolean
) : Parcelable
