package com.fida.app

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.fida.app.databinding.ActivityWaterIntakeBinding
import com.fida.app.utils.PreferenceHelper
import com.mikhaellopez.circularprogressbar.CircularProgressBar

class WaterIntakeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWaterIntakeBinding
    private lateinit var prefs: PreferenceHelper
    private var currentIntake = 0
    private var goalIntake = 2000 // Default 2000ml

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWaterIntakeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        prefs = PreferenceHelper(this)
        goalIntake = prefs.getInt("waterGoal") ?: 2000
        currentIntake = prefs.getInt("currentWaterIntake") ?: 0

        setupViews()
        updateUI()
    }

    private fun setupViews() {
        binding.btnAdd250ml.setOnClickListener {
            addWater(250)
        }
        binding.btnAdd500ml.setOnClickListener {
            addWater(500)
        }
        binding.btnLogCustomWater.setOnClickListener {
            // TODO: Navigate to a screen/dialog to log custom amount
        }
        binding.ivCloseWater.setOnClickListener {
            finish()
        }
    }

    private fun addWater(amount: Int) {
        currentIntake += amount
        prefs.saveInt("currentWaterIntake", currentIntake)
        updateUI()
    }

    private fun updateUI() {
        binding.tvWaterIntake.text = "${currentIntake}ml / ${goalIntake}ml"
        val progress = (currentIntake.toFloat() / goalIntake.toFloat()) * 100
        binding.waterProgressBar.progress = progress
    }
}
