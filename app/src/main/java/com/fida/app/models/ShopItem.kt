package com.fida.app.models

import androidx.annotation.DrawableRes

enum class ItemType {
    HEALTH,
    XP_BOOSTER,
    STREAK_SHIELD,
    POWER_UP,
    COSMETIC
}

enum class PowerUpType {
    COIN_BOOSTER,
    XP_BOOSTER,
    ENERGY_BOOSTER,
    TRIPLE_XP_BOOSTER,
    COIN_BOOSTER_24H,
    SUPER_ENERGY_BOOSTER
}

enum class CosmeticType {
    AVATAR_FRAME_FIRE,
    AVATAR_FRAME_TROPHY,
    AVATAR_FRAME_STAR
}

data class ShopItem(
    val id: String,
    val name: String,
    val description: String,
    val effectDescription: String,
    val price: Int,
    @DrawableRes val imageRes: Int,
    val type: ItemType,
    val effectValue: Int,
    val powerUpType: PowerUpType? = null,
    val cosmeticType: CosmeticType? = null
)