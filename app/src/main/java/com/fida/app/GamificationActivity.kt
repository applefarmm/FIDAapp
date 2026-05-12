package com.fida.app

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.fida.app.fragments.ChallengeFragment
import com.fida.app.fragments.LevelUpFragment
import com.fida.app.fragments.RewardsFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class GamificationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gamification)
        supportActionBar?.hide()

        // Back button
        findViewById<ImageView>(R.id.ivBackGamification).setOnClickListener {
            finish()
        }

        val bottomNav = findViewById<BottomNavigationView>(R.id.gamificationBottomNav)

        if (savedInstanceState == null) {
            loadGamificationFragment(RewardsFragment())
        }

        bottomNav.setOnItemSelectedListener { item ->
            val fragment: Fragment = when (item.itemId) {
                R.id.nav_rewards -> RewardsFragment()
                R.id.nav_level_up -> LevelUpFragment()
                R.id.nav_challenges -> ChallengeFragment()
                else -> RewardsFragment()
            }
            loadGamificationFragment(fragment)
            true
        }
    }

    private fun loadGamificationFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.gamificationFragmentContainer, fragment)
            .commit()
    }
}
