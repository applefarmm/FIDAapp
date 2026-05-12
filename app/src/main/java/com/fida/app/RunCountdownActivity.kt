package com.fida.app

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.fida.app.utils.PreferenceHelper

class RunCountdownActivity : AppCompatActivity() {

    private var goalType: Int = 0
    private var goalValue: Float = 0f
    private lateinit var uid: String
    private var countdownTimer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_run_countdown)
        supportActionBar?.hide()

        // Get intent extras with defaults
        goalType = intent.getIntExtra("goalType", 0)
        goalValue = intent.getFloatExtra("goalValue", 5f)

        val uidExtra = intent.getStringExtra("uid")
        uid = if (uidExtra.isNullOrEmpty()) {
            PreferenceHelper(this).getUid() ?: ""
        } else {
            uidExtra
        }

        // Validate data
        if (uid.isEmpty()) {
            Toast.makeText(this, "Error: User not found. Please log in again.", Toast.LENGTH_LONG).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        if (goalValue <= 0f) {
            goalValue = 5f // Default to 5km if invalid
        }

        startCountdown()
    }

    private fun startCountdown() {
        val tvCountdown = findViewById<TextView>(R.id.tvCountdown)

        countdownTimer = object : CountDownTimer(5000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = (millisUntilFinished / 1000).toInt()
                if (seconds > 0) {
                    tvCountdown.text = seconds.toString()
                    tvCountdown.animate().scaleX(1.3f).scaleY(1.3f).setDuration(200).withEndAction {
                        tvCountdown.animate().scaleX(1f).scaleY(1f).setDuration(200).start()
                    }.start()
                }
            }

            override fun onFinish() {
                tvCountdown.text = "GO!"
                tvCountdown.animate().scaleX(1.5f).scaleY(1.5f).setDuration(300).withEndAction {
                    try {
                        startActivity(Intent(this@RunCountdownActivity, RunTrackingActivity::class.java).apply {
                            putExtra("goalType", goalType)
                            putExtra("goalValue", goalValue)
                            putExtra("uid", uid)
                        })
                    } catch (e: Exception) {
                        Toast.makeText(this@RunCountdownActivity, "Error starting run: ${e.message}", Toast.LENGTH_LONG).show()
                        startActivity(Intent(this@RunCountdownActivity, HomeActivity::class.java))
                    }
                    finish()
                }.start()
            }
        }

        countdownTimer?.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        countdownTimer?.cancel()
    }
}