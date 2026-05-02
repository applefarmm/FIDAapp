package com.fida.app.models

data class Transaction(
    val description: String,
    val amount: String, // e.g., "+50 Coins", "-20 Gems"
    val timestamp: String, // e.g., "2023-10-27 10:00 AM"
    val type: Type
) {
    enum class Type {
        EARNED, SPENT
    }
}
