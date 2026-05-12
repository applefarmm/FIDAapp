package com.fida.app

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.fida.app.utils.PreferenceHelper
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        supportActionBar?.hide()

        val logo = findViewById<ImageView>(R.id.splashLogo)
        val appName = findViewById<TextView>(R.id.splashAppName)
        val tagline = findViewById<TextView>(R.id.splashTagline)

        // Removed animation start as it was causing issues in context
        // val fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in)
        // logo.startAnimation(fadeIn)
        // appName.startAnimation(fadeIn)
        // tagline.startAnimation(fadeIn)

        Handler(Looper.getMainLooper()).postDelayed({
            val prefs = PreferenceHelper(this)
            val user = FirebaseAuth.getInstance().currentUser

            when {
                user != null && prefs.isProfileSetupComplete() -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                }
                user != null -> {
                    startActivity(Intent(this, ProfileSetup1Activity::class.java))
                }
                prefs.isOnboardingDone() -> {
                    startActivity(Intent(this, LoginActivity::class.java))
                }
                else -> {
                    startActivity(Intent(this, OnboardingActivity::class.java))
                }
            }
            finish()
        }, 2000)
    }
}
