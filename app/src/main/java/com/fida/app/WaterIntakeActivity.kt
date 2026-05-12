package com.fida.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.fida.app.databinding.ActivityWaterIntakeBinding
import com.fida.app.utils.FirestoreRepository
import com.fida.app.utils.PowerUpIndicator
import com.fida.app.utils.PowerUpManager
import com.fida.app.utils.PreferenceHelper
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class WaterIntakeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWaterIntakeBinding
    private lateinit var prefs: PreferenceHelper
    private var currentIntake = 0
    private var goalIntake = 2000
    private var addedToday = 0

    private val uid: String by lazy {
        intent.getStringExtra("uid")
            ?: PreferenceHelper(this).getUid()
            ?: ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWaterIntakeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        prefs = PreferenceHelper(this)
        goalIntake = prefs.getInt("waterGoal") ?: 2000
        currentIntake = prefs.getInt("currentWaterIntake") ?: 0
        addedToday = prefs.getInt("addedTodayWater") ?: 0

        setupViews()
        PowerUpIndicator.bind(this, uid, binding.root)
        updateUI()
    }

    private fun setupViews() {
        binding.btnAdd250ml.setOnClickListener { addWater(250) }
        binding.btnAdd500ml.setOnClickListener { addWater(500) }
        binding.btnLogCustomWater.setOnClickListener { showCustomAmountDialog() }
        binding.ivBackWater.setOnClickListener { finish() }
    }

    private fun showCustomAmountDialog() {
        val view: View = LayoutInflater.from(this).inflate(R.layout.layout_custom_water_dialog, null)
        val editText: TextInputEditText = view.findViewById(R.id.etCustomAmount)

        val alertDialog: AlertDialog = MaterialAlertDialogBuilder(this)
            .setTitle("Log Custom Amount")
            .setView(view)
            .setPositiveButton("Add") { _, _ ->
                val input = editText.text.toString()
                if (input.isNotEmpty()) {
                    val amount = input.toIntOrNull()
                    if (amount != null && amount > 0) {
                        addWater(amount)
                        Toast.makeText(this, "Added ${amount}ml", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Please enter an amount", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()

        alertDialog.setOnShowListener {
            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(resources.getColor(R.color.blue, theme))
            alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(resources.getColor(R.color.grey_500, theme))
        }

        alertDialog.show()
    }

    private fun addWater(amount: Int) {
        val previousIntake = currentIntake
        currentIntake += amount
        addedToday += amount

        prefs.saveInt("currentWaterIntake", currentIntake)
        prefs.saveInt("addedTodayWater", addedToday)

        if (uid.isNotEmpty()) {
            FirestoreRepository.logDailyActivity(uid, "waterCounter", currentIntake / 250)
        }

        if (previousIntake < goalIntake && currentIntake >= goalIntake) {
            awardGoalXPWithBoost()
        }
        updateUI()
    }

    private fun awardGoalXPWithBoost() {
        lifecycleScope.launch {
            val powerUps = PowerUpManager.getActivePowerUps(uid)

            val baseXp = 50
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
                "Goal reached! Boosted: $boostedXp XP (${multiplier}x)!"
            } else {
                "Goal reached! You earned $boostedXp XP!"
            }
            Toast.makeText(this@WaterIntakeActivity, message, Toast.LENGTH_LONG).show()
        }
    }

    private fun updateUI() {
        binding.tvWaterIntake.text = "${currentIntake}ml / ${goalIntake}ml"
        binding.tvAddedToday.text = "Added today: ${addedToday}ml"
        val progress = (currentIntake.toFloat() / goalIntake.toFloat() * 100).toInt()
        binding.waterProgressBar.progress = progress.toFloat()
    }
}