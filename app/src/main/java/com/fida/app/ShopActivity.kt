package com.fida.app

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.fida.app.models.ItemType
import com.fida.app.models.PowerUpType
import com.fida.app.models.CosmeticType
import com.fida.app.models.ShopItem
import com.fida.app.utils.PreferenceHelper

class ShopActivity : AppCompatActivity() {
    private val db = Firebase.firestore

    private var health = 0
    private var coins = 0
    private var xp = 0
    private var streakShields = 0

    private val safeUid: String by lazy {
        intent.getStringExtra("uid")
            ?: PreferenceHelper(this).getUid()
            ?: ""
    }

    private val shopItems: List<ShopItem> by lazy {
        createShopItems()
    }

    private lateinit var adapter: ShopAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shop)

        supportActionBar?.elevation = 0F
        supportActionBar?.title = "Shop"

        findViewById<ImageView>(R.id.ivBackShop).setOnClickListener {
            finish()
        }

        setupRecyclerView()
        loadData(safeUid)
    }

    private fun setupRecyclerView() {
        adapter = ShopAdapter(shopItems) { item ->
            showItemDetailBottomSheet(item)
        }

        findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvShopItems).apply {
            layoutManager = LinearLayoutManager(this@ShopActivity)
            adapter = this@ShopActivity.adapter
        }
    }

    private fun createShopItems(): List<ShopItem> {
        return listOf(
            // Health Items
            ShopItem(
                id = "health_potion_small",
                name = "Health Potion",
                description = "Your average health potion to keep you alive.",
                effectDescription = "Instantly recover 20 health",
                price = 50,
                imageRes = R.drawable.healing_potion,
                type = ItemType.HEALTH,
                effectValue = 20
            ),
            ShopItem(
                id = "health_potion_medium",
                name = "Large Health Potion",
                description = "A stronger potion for when you need more healing.",
                effectDescription = "Instantly recover 50 health",
                price = 100,
                imageRes = R.drawable.healing_potion,
                type = ItemType.HEALTH,
                effectValue = 50
            ),
            ShopItem(
                id = "golden_apple",
                name = "Golden Apple",
                description = "For when your health is dwindling and your pockets are brimming with coins.",
                effectDescription = "Instantly recover full health",
                price = 200,
                imageRes = R.drawable.golden_apple,
                type = ItemType.HEALTH,
                effectValue = 100
            ),

            // XP Boosters
            ShopItem(
                id = "xp_scroll",
                name = "XP Scroll",
                description = "A scroll of wisdom that grants instant experience.",
                effectDescription = "Gain 100 XP instantly",
                price = 75,
                imageRes = R.drawable.ic_xp,
                type = ItemType.XP_BOOSTER,
                effectValue = 100
            ),
            ShopItem(
                id = "mega_xp_scroll",
                name = "Mega XP Scroll",
                description = "A powerful scroll filled with ancient knowledge.",
                effectDescription = "Gain 250 XP instantly",
                price = 150,
                imageRes = R.drawable.ic_xp,
                type = ItemType.XP_BOOSTER,
                effectValue = 250
            ),

            // Streak Shields
            ShopItem(
                id = "streak_shield_single",
                name = "Streak Shield",
                description = "Protect your streak from being broken one time.",
                effectDescription = "+1 Streak Shield",
                price = 100,
                imageRes = R.drawable.ic_streak_shield,
                type = ItemType.STREAK_SHIELD,
                effectValue = 1
            ),
            ShopItem(
                id = "streak_shield_bundle",
                name = "Shield Bundle",
                description = "A bundle of shields for streak protection.",
                effectDescription = "+3 Streak Shields",
                price = 250,
                imageRes = R.drawable.ic_streak_shield,
                type = ItemType.STREAK_SHIELD,
                effectValue = 3
            ),

            // Power-ups
            ShopItem(
                id = "coin_booster",
                name = "Coin Booster",
                description = "Double your coin rewards for your next activity.",
                effectDescription = "2x Coins for next run",
                price = 50,
                imageRes = R.drawable.ic_coin,
                type = ItemType.POWER_UP,
                effectValue = 2,
                powerUpType = PowerUpType.COIN_BOOSTER
            ),
            ShopItem(
                id = "xp_booster",
                name = "XP Booster",
                description = "Double your XP gains for your next activity.",
                effectDescription = "2x XP for next activity",
                price = 50,
                imageRes = R.drawable.ic_xp,
                type = ItemType.POWER_UP,
                effectValue = 2,
                powerUpType = PowerUpType.XP_BOOSTER
            ),
            ShopItem(
                id = "energy_booster",
                name = "Energy Booster",
                description = "Maximize your rewards with both coin and XP boost for 24 hours.",
                effectDescription = "2x Coins + 2x XP for 24 hours",
                price = 200,
                imageRes = R.drawable.ic_gem,
                type = ItemType.POWER_UP,
                effectValue = 24,
                powerUpType = PowerUpType.ENERGY_BOOSTER
            ),

            // NEW: More Health Items
            ShopItem(
                id = "mini_health_potion",
                name = "Mini Health Potion",
                description = "A small potion for minor injuries.",
                effectDescription = "Instantly recover 10 health",
                price = 25,
                imageRes = R.drawable.healing_potion,
                type = ItemType.HEALTH,
                effectValue = 10
            ),
            ShopItem(
                id = "super_health_potion",
                name = "Super Health Potion",
                description = "A powerful potion for serious wounds.",
                effectDescription = "Instantly recover 75 health",
                price = 150,
                imageRes = R.drawable.healing_potion,
                type = ItemType.HEALTH,
                effectValue = 75
            ),

            // NEW: More Power-ups
            ShopItem(
                id = "triple_xp_booster",
                name = "Triple XP Booster",
                description = "Triple your XP gains for a quick session.",
                effectDescription = "3x XP for 1 hour",
                price = 250,
                imageRes = R.drawable.ic_level_up,
                type = ItemType.POWER_UP,
                effectValue = 1,
                powerUpType = PowerUpType.TRIPLE_XP_BOOSTER
            ),
            ShopItem(
                id = "coin_booster_24h",
                name = "Coin Boost (24h)",
                description = "Double your coin rewards for a full day.",
                effectDescription = "2x Coins for 24 hours",
                price = 150,
                imageRes = R.drawable.ic_coin,
                type = ItemType.POWER_UP,
                effectValue = 24,
                powerUpType = PowerUpType.COIN_BOOSTER_24H
            ),
            ShopItem(
                id = "super_energy_booster",
                name = "Super Energy Booster",
                description = "Triple both coin and XP rewards for 6 hours.",
                effectDescription = "3x Coins + 3x XP for 6 hours",
                price = 500,
                imageRes = R.drawable.ic_gem,
                type = ItemType.POWER_UP,
                effectValue = 6,
                powerUpType = PowerUpType.SUPER_ENERGY_BOOSTER
            ),

            // NEW: Cosmetics
            ShopItem(
                id = "avatar_frame_fire",
                name = "Fire Avatar Frame",
                description = "Show your passion with a fiery avatar border.",
                effectDescription = "Unlocks Fire frame for your avatar",
                price = 500,
                imageRes = R.drawable.ic_avatar_frame_fire,
                type = ItemType.COSMETIC,
                effectValue = 1,
                cosmeticType = CosmeticType.AVATAR_FRAME_FIRE
            ),
            ShopItem(
                id = "avatar_frame_trophy",
                name = "Trophy Avatar Frame",
                description = "Display your achievements with a golden trophy frame.",
                effectDescription = "Unlocks Trophy frame for your avatar",
                price = 1200,
                imageRes = R.drawable.ic_avatar_frame_trophy,
                type = ItemType.COSMETIC,
                effectValue = 1,
                cosmeticType = CosmeticType.AVATAR_FRAME_TROPHY
            ),
            ShopItem(
                id = "avatar_frame_star",
                name = "Star Avatar Frame",
                description = "Shine bright with a purple star frame.",
                effectDescription = "Unlocks Star frame for your avatar",
                price = 800,
                imageRes = R.drawable.ic_avatar_frame_star,
                type = ItemType.COSMETIC,
                effectValue = 1,
                cosmeticType = CosmeticType.AVATAR_FRAME_STAR
            )
        )
    }

    private fun loadData(uid: String) {
        if (uid.isEmpty()) return

        val userRef = db.collection("users").document(uid)
        val myCoinTextView: TextView = findViewById(R.id.myCoin_shopScreen)

        userRef.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val doc = task.result
                if (doc.exists()) {
                    health = (doc.get("health") as? Long)?.toInt() ?: 0
                    coins = (doc.get("coins") as? Long)?.toInt() ?: 0
                    xp = (doc.get("xp") as? Long)?.toInt() ?: 0
                    streakShields = (doc.get("streakShields") as? Long)?.toInt() ?: 0
                    myCoinTextView.text = coins.toString()
                }
            } else {
                Log.e("ShopActivity", "Failed to load user data: ${task.exception?.message}")
            }
        }
    }

    private fun showItemDetailBottomSheet(item: ShopItem) {
        val bottomSheet = ItemDetailBottomSheet.newInstance(
            item = item,
            userCoins = coins,
            userHealth = health,
            userXp = xp,
            userStreakShields = streakShields,
            onPurchase = { purchasedItem ->
                purchase(purchasedItem)
            }
        )
        bottomSheet.show(supportFragmentManager, ItemDetailBottomSheet.TAG)
    }

    private fun purchase(item: ShopItem) {
        val myCoinTextView: TextView = findViewById(R.id.myCoin_shopScreen)

        val remainder = coins - item.price

        if (remainder < 0) {
            Toast.makeText(this, "Not Enough Coins...", Toast.LENGTH_SHORT).show()
            return
        }

        coins = remainder
        myCoinTextView.text = coins.toString()
        updateCoins(safeUid)

        when (item.type) {
            ItemType.HEALTH -> handleHealthPurchase(item)
            ItemType.XP_BOOSTER -> handleXpPurchase(item)
            ItemType.STREAK_SHIELD -> handleStreakShieldPurchase(item)
            ItemType.POWER_UP -> handlePowerUpPurchase(item)
            ItemType.COSMETIC -> handleCosmeticPurchase(item)
        }

        Toast.makeText(this, "${item.name} purchased!", Toast.LENGTH_SHORT).show()
    }

    private fun handleHealthPurchase(item: ShopItem) {
        val newHealth = health + item.effectValue
        health = if (newHealth > 100) 100 else newHealth
        updateHealth(safeUid)
    }

    private fun handleXpPurchase(item: ShopItem) {
        val userRef = db.collection("users").document(safeUid)
        userRef.get().addOnSuccessListener { doc ->
            var currentXp = (doc.get("xp") as? Long)?.toInt() ?: 0
            var level = (doc.get("level") as? Long)?.toInt() ?: 1
            var currentCoins = (doc.get("coins") as? Long)?.toInt() ?: 0
            val maxXp = com.fida.app.utils.GameManager.xpForLevel(level)

            currentXp += item.effectValue

            while (currentXp >= maxXp) {
                currentXp -= maxXp
                level++
                currentCoins += 50
                val newMaxXp = com.fida.app.utils.GameManager.xpForLevel(level)
                userRef.update(
                    "xp", currentXp,
                    "level", level,
                    "maxXp", newMaxXp,
                    "coins", currentCoins
                )
            }

            userRef.update(
                "xp", currentXp,
                "level", level,
                "maxXp", maxXp
            )
        }
    }

    private fun handleStreakShieldPurchase(item: ShopItem) {
        streakShields += item.effectValue
        db.collection("users").document(safeUid)
            .update("streakShields", streakShields)
    }

    private fun handlePowerUpPurchase(item: ShopItem) {
        val now = System.currentTimeMillis()
        val expiryTime = when (item.powerUpType) {
            PowerUpType.ENERGY_BOOSTER, PowerUpType.COIN_BOOSTER_24H -> now + (item.effectValue * 60 * 60 * 1000) // hours
            PowerUpType.TRIPLE_XP_BOOSTER -> now + (item.effectValue * 60 * 60 * 1000) // hours
            PowerUpType.SUPER_ENERGY_BOOSTER -> now + (item.effectValue * 60 * 60 * 1000) // hours
            else -> now + (24 * 60 * 60 * 1000) // 24 hours default for single-use boosters
        }

        val powerUpField = when (item.powerUpType) {
            PowerUpType.COIN_BOOSTER -> "activePowerUps.coinBoosterExpiry"
            PowerUpType.XP_BOOSTER -> "activePowerUps.xpBoosterExpiry"
            PowerUpType.ENERGY_BOOSTER -> "activePowerUps.energyBoosterExpiry"
            PowerUpType.TRIPLE_XP_BOOSTER -> "activePowerUps.tripleXpBoosterExpiry"
            PowerUpType.COIN_BOOSTER_24H -> "activePowerUps.coinBooster24hExpiry"
            PowerUpType.SUPER_ENERGY_BOOSTER -> "activePowerUps.superEnergyBoosterExpiry"
            null -> return
        }

        db.collection("users").document(safeUid)
            .update(powerUpField, expiryTime)
    }

    private fun handleCosmeticPurchase(item: ShopItem) {
        val cosmeticField = when (item.cosmeticType) {
            CosmeticType.AVATAR_FRAME_FIRE -> "unlockedCosmetics.avatarFrameFire"
            CosmeticType.AVATAR_FRAME_TROPHY -> "unlockedCosmetics.avatarFrameTrophy"
            CosmeticType.AVATAR_FRAME_STAR -> "unlockedCosmetics.avatarFrameStar"
            null -> return
        }

        db.collection("users").document(safeUid)
            .update(cosmeticField, true)
            .addOnSuccessListener {
                Log.d("ShopActivity", "Cosmetic unlocked: ${item.name}")
            }
    }

    private fun updateCoins(uid: String) {
        if (uid.isEmpty()) return
        db.collection("users").document(uid)
            .update("coins", coins)
            .addOnSuccessListener { Log.d("ShopActivity", "Coins updated to $coins") }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error updating coins", Toast.LENGTH_SHORT).show()
                Log.e("ShopActivity", e.toString())
            }
    }

    private fun updateHealth(uid: String) {
        if (uid.isEmpty()) return
        db.collection("users").document(uid)
            .update("health", health)
            .addOnSuccessListener { Log.d("ShopActivity", "Health updated to $health") }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error updating health", Toast.LENGTH_SHORT).show()
                Log.e("ShopActivity", e.toString())
            }
    }
}