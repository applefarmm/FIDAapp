package com.fida.app.models

data class Quest(
    val title: String,
    val description: String,
    val icon: String,
    val xpReward: Int,
    val coinReward: Int,
    val type: String,
    var accepted: Boolean = false,
    var completed: Boolean = false
)