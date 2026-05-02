package com.fida.app.models

data class Achievement(
    val title: String,
    val description: String,
    val iconUrl: String, // URL to an image or drawable resource ID
    val isUnlocked: Boolean
)
