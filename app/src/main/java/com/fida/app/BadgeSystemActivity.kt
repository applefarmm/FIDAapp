package com.fida.app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fida.app.adapters.BadgeAdapter
import com.fida.app.fragments.BadgeUnlockedFragment
import com.fida.app.models.Badge
import com.fida.app.utils.PreferenceHelper
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class BadgeSystemActivity : AppCompatActivity() {

    private lateinit var prefs: PreferenceHelper
    private lateinit var badgeAdapter: BadgeAdapter
    private val badges = mutableListOf<Badge>()
    private val allBadges = mutableListOf<Badge>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_badge_system)
        supportActionBar?.hide()

        prefs = PreferenceHelper(this)

        setupViews()
        loadBadges()
    }

    private fun setupViews() {
        val rvBadges = findViewById<RecyclerView>(R.id.rvBadges)
        badgeAdapter = BadgeAdapter(badges) { badge ->
            showBadgeDetail(badge)
        }
        rvBadges.layoutManager = LinearLayoutManager(this)
        rvBadges.adapter = badgeAdapter

        val chipGroup = findViewById<ChipGroup>(R.id.chipGroupCategory)
        chipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            filterBadges(checkedIds)
        }

        findViewById<MaterialButton>(R.id.btnClose).setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }
    }

    private fun loadBadges() {
        loadDefaultBadges()
        allBadges.addAll(badges)
        updateUnlockedCount()
    }

    private fun loadDefaultBadges() {
        badges.clear()
        badges.addAll(listOf(
            Badge(
                id = "first_run",
                title = "First Steps",
                description = "Complete your first running session",
                iconUrl = "",
                category = "running",
                requirement = "Complete 1 run",
                xpReward = 50,
                coinReward = 20,
                rarity = "common",
                isUnlocked = true,
                progress = 1,
                targetProgress = 1
            ),
            Badge(
                id = "5k_runner",
                title = "5K Runner",
                description = "Run a total of 5 kilometers",
                iconUrl = "",
                category = "running",
                requirement = "Run 5km total distance",
                xpReward = 100,
                coinReward = 50,
                rarity = "rare",
                isUnlocked = false,
                progress = 3,
                targetProgress = 5
            ),
            Badge(
                id = "10k_runner",
                title = "10K Champion",
                description = "Run a total of 10 kilometers",
                iconUrl = "",
                category = "running",
                requirement = "Run 10km total distance",
                xpReward = 200,
                coinReward = 100,
                rarity = "epic",
                isUnlocked = false,
                progress = 3,
                targetProgress = 10
            ),
            Badge(
                id = "marathon",
                title = "Marathon Master",
                description = "Run a total of 42 kilometers",
                iconUrl = "",
                category = "running",
                requirement = "Run 42km total distance",
                xpReward = 500,
                coinReward = 250,
                gemReward = 10,
                rarity = "legendary",
                isUnlocked = false,
                progress = 3,
                targetProgress = 42
            ),
            Badge(
                id = "water_8",
                title = "Hydration Hero",
                description = "Drink 8 glasses of water in a day",
                iconUrl = "",
                category = "water",
                requirement = "Drink 8 glasses",
                xpReward = 30,
                coinReward = 15,
                rarity = "common",
                isUnlocked = true,
                progress = 8,
                targetProgress = 8
            ),
            Badge(
                id = "water_week",
                title = "Water Warrior",
                description = "Stay hydrated for 7 consecutive days",
                iconUrl = "",
                category = "water",
                requirement = "7 days of 8 glasses",
                xpReward = 150,
                coinReward = 75,
                rarity = "rare",
                isUnlocked = false,
                progress = 5,
                targetProgress = 7
            ),
            Badge(
                id = "sleep_8",
                title = "Sleep Champion",
                description = "Sleep 8+ hours in a night",
                iconUrl = "",
                category = "sleep",
                requirement = "Sleep 8+ hours",
                xpReward = 40,
                coinReward = 20,
                rarity = "common",
                isUnlocked = false,
                progress = 7,
                targetProgress = 8
            ),
            Badge(
                id = "sleep_week",
                title = "Rest Master",
                description = "Get 8+ hours sleep for 7 nights",
                iconUrl = "",
                category = "sleep",
                requirement = "7 nights of 8+ hours",
                xpReward = 200,
                coinReward = 100,
                rarity = "epic",
                isUnlocked = false,
                progress = 3,
                targetProgress = 7
            ),
            Badge(
                id = "streak_7",
                title = "Week Warrior",
                description = "Maintain a 7-day activity streak",
                iconUrl = "",
                category = "streak",
                requirement = "7-day streak",
                xpReward = 100,
                coinReward = 50,
                rarity = "rare",
                isUnlocked = true,
                progress = 7,
                targetProgress = 7
            ),
            Badge(
                id = "streak_30",
                title = "Monthly Master",
                description = "Maintain a 30-day activity streak",
                iconUrl = "",
                category = "streak",
                requirement = "30-day streak",
                xpReward = 300,
                coinReward = 150,
                gemReward = 5,
                rarity = "epic",
                isUnlocked = false,
                progress = 14,
                targetProgress = 30
            ),
            Badge(
                id = "streak_100",
                title = "Centurion",
                description = "Maintain a 100-day activity streak",
                iconUrl = "",
                category = "streak",
                requirement = "100-day streak",
                xpReward = 1000,
                coinReward = 500,
                gemReward = 20,
                rarity = "legendary",
                isUnlocked = false,
                progress = 14,
                targetProgress = 100
            )
        ))
        badgeAdapter.notifyDataSetChanged()
    }

    private fun filterBadges(checkedIds: List<Int>) {
        val selectedCategory = when {
            checkedIds.contains(R.id.chipRunning) -> "running"
            checkedIds.contains(R.id.chipWater) -> "water"
            checkedIds.contains(R.id.chipSleep) -> "sleep"
            checkedIds.contains(R.id.chipStreak) -> "streak"
            else -> null
        }

        badges.clear()
        if (selectedCategory == null) {
            badges.addAll(allBadges)
        } else {
            badges.addAll(allBadges.filter { it.category == selectedCategory })
        }
        badgeAdapter.notifyDataSetChanged()
        updateEmptyState()
    }

    private fun showBadgeDetail(badge: Badge) {
        val detailFragment = BadgeUnlockedFragment.newInstance(
            com.fida.app.models.Achievement(
                badge.title,
                badge.description,
                badge.iconUrl,
                badge.isUnlocked
            )
        )
        detailFragment.show(supportFragmentManager, "badge_detail")
    }

    private fun updateUnlockedCount() {
        val tvCount = findViewById<TextView>(R.id.tvUnlockedCount)
        val unlocked = allBadges.count { it.isUnlocked }
        tvCount.text = "$unlocked/${allBadges.size} unlocked"
    }

    private fun updateEmptyState() {
        val tvNoBadges = findViewById<TextView>(R.id.tvNoBadges)
        val rvBadges = findViewById<RecyclerView>(R.id.rvBadges)
        if (badges.isEmpty()) {
            tvNoBadges.visibility = View.VISIBLE
            rvBadges.visibility = View.GONE
        } else {
            tvNoBadges.visibility = View.GONE
            rvBadges.visibility = View.VISIBLE
        }
    }
}