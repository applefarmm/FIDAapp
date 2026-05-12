package com.fida.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.fida.app.fragments.AchievementsFragment
import com.fida.app.fragments.ActivitiesFragment
import com.fida.app.fragments.DashboardFragment
import com.fida.app.fragments.HealthTipsFragment
import com.fida.app.fragments.LeaderboardFragment
import com.fida.app.fragments.ProfileFragment
import com.fida.app.services.StepTrackingService
import com.fida.app.utils.PreferenceHelper
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomeActivity : AppCompatActivity() {

    private lateinit var prefs: PreferenceHelper

    private val activityRecognitionPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            StepTrackingService.start(this)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        supportActionBar?.hide()

        prefs = PreferenceHelper(this)

        // Note: StepTrackingService started in onResume() to avoid ForegroundServiceStartNotAllowedException

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)

        if (savedInstanceState == null) {
            loadFragment(DashboardFragment())
        }

        bottomNav.setOnItemSelectedListener { item ->
            val fragment: Fragment = when (item.itemId) {
                R.id.nav_home -> DashboardFragment()
                R.id.nav_activities -> ActivitiesFragment()
                R.id.nav_health -> HealthTipsFragment()
                R.id.nav_achievements -> AchievementsFragment()
                R.id.nav_profile -> ProfileFragment()
                else -> DashboardFragment()
            }
            loadFragment(fragment)
            true
        }
    }

    override fun onResume() {
        super.onResume()
        // Start step tracking service with proper permission check
        startStepTrackingServiceIfNeeded()
    }

    private fun startStepTrackingServiceIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val permission = Manifest.permission.ACTIVITY_RECOGNITION
            if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
                StepTrackingService.start(this)
            } else {
                activityRecognitionPermissionLauncher.launch(permission)
            }
        } else {
            // Below Android 10, no runtime permission needed
            StepTrackingService.start(this)
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.homeFragmentContainer, fragment)
            .commit()
    }

    fun getUid(): String = PreferenceHelper(this).getUid() ?: ""
    fun getUsername(): String = PreferenceHelper(this).getUsername() ?: ""
}
