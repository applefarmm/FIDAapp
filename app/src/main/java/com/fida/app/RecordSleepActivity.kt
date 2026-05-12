package com.fida.app

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.fida.app.databinding.ActivityRecordSleepBinding
import com.fida.app.utils.FirestoreRepository
import com.fida.app.utils.PowerUpIndicator
import com.fida.app.utils.PowerUpManager
import com.fida.app.utils.PreferenceHelper
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class RecordSleepActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRecordSleepBinding
    private lateinit var prefs: PreferenceHelper
    private var bedTime: Long = 0
    private var wakeTime: Long = 0
    private var sleepHours: Float = 0f

    private val uid: String by lazy {
        intent.getStringExtra("uid")
            ?: PreferenceHelper(this).getUid()
            ?: ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecordSleepBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        prefs = PreferenceHelper(this)

        setupViews()
        PowerUpIndicator.bind(this, uid, binding.root)
    }

    private fun setupViews() {
        binding.bedTimeSelector.setOnClickListener { showTimePicker(true) }
        binding.wakeTimeSelector.setOnClickListener { showTimePicker(false) }
        binding.btnLogSleep.setOnClickListener { logSleep() }
        binding.ivBackSleep.setOnClickListener { finish() }
    }

    private fun showTimePicker(isBedTime: Boolean) {
        val picker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_12H)
            .setHour(if (isBedTime) 22 else 6)
            .setMinute(0)
            .setTitleText(if (isBedTime) "Select Bedtime" else "Select Wake-up Time")
            .build()

        picker.addOnPositiveButtonClickListener {
            val calendar = java.util.Calendar.getInstance()
            calendar.set(java.util.Calendar.HOUR_OF_DAY, picker.hour)
            calendar.set(java.util.Calendar.MINUTE, picker.minute)
            if (isBedTime) {
                bedTime = calendar.timeInMillis
                binding.bedTime.text = formatTime(bedTime)
            } else {
                wakeTime = calendar.timeInMillis
                binding.wakeTime.text = formatTime(wakeTime)
            }
            updateDuration()
        }
        picker.show(supportFragmentManager, "time_picker")
    }

    private fun logSleep() {
        if (bedTime <= 0 || wakeTime <= 0) {
            Toast.makeText(this, "Please select both bedtime and wake-up time.", Toast.LENGTH_SHORT).show()
            return
        }

        var duration = wakeTime - bedTime
        if (duration < 0) {
            duration += TimeUnit.DAYS.toMillis(1)
        }
        sleepHours = duration.toFloat() / TimeUnit.HOURS.toMillis(1)

        if (uid.isNotEmpty()) {
            FirestoreRepository.logDailyActivity(uid, "sleepTime", sleepHours)
        }

        awardSleepXPWithBoost()
        Toast.makeText(this, "Sleep logged: %.1f hours".format(sleepHours), Toast.LENGTH_LONG).show()
        finish()
    }

    private fun awardSleepXPWithBoost() {
        lifecycleScope.launch {
            val powerUps = PowerUpManager.getActivePowerUps(uid)

            val baseXp = 75
            val boostedXp = PowerUpManager.applyXpBoost(baseXp, powerUps)

            if (uid.isNotEmpty()) {
                FirestoreRepository.incrementUserField(uid, "xp", boostedXp.toLong()) { success ->
                    if (success) {
                        val currentXp = prefs.getInt("xp") ?: 0
                        prefs.saveInt("xp", currentXp + boostedXp)
                    }
                }
            }

            PowerUpManager.clearExpiredPowerUps(uid, powerUps)

            val hasBoost = powerUps.hasXpBoost || powerUps.hasEnergyBoost
            val message = if (hasBoost) {
                val multiplier = boostedXp / baseXp
                "Boosted: You earned $boostedXp XP (${multiplier}x)!"
            } else {
                "You earned $boostedXp XP!"
            }
            Toast.makeText(this@RecordSleepActivity, message, Toast.LENGTH_LONG).show()
        }
    }

    private fun updateDuration() {
        if (bedTime > 0 && wakeTime > 0) {
            var duration = wakeTime - bedTime
            if (duration < 0) {
                duration += TimeUnit.DAYS.toMillis(1)
            }
            val hours = TimeUnit.MILLISECONDS.toHours(duration)
            val minutes = TimeUnit.MILLISECONDS.toMinutes(duration) % 60
            binding.tvSleepDuration.text = "Duration: ${hours}h ${minutes}m"
        }
    }

    private fun formatTime(timeInMillis: Long): String {
        val format = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault())
        return format.format(java.util.Date(timeInMillis))
    }
}