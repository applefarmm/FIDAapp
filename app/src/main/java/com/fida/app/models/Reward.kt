package com.fida.app.models

data class Reward(
    val name: String,
    val description: String,
    val imageUrl: String,
    val cost: Int,
    val type: Type
) {
    enum class Type {
        COIN, GEM
    }
}
