package com.fida.app

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.fida.app.utils.PreferenceHelper
import com.google.android.material.button.MaterialButton

class PreRunActivity : AppCompatActivity() {

    private lateinit var prefs: PreferenceHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pre_run)
        supportActionBar?.hide()

        prefs = PreferenceHelper(this)

        setupViews()
    }

    private fun setupViews() {
        val rgGoalType = findViewById<RadioGroup>(R.id.rgGoalType)
        val etGoalValue = findViewById<EditText>(R.id.etGoalValue)
        val btnStartRun = findViewById<MaterialButton>(R.id.btnStartRun)
        val tvBack = findViewById<TextView>(R.id.tvBackToHome)

        btnStartRun.setOnClickListener {
            val selectedGoalTypeId = rgGoalType.checkedRadioButtonId
            val goalType = when (selectedGoalTypeId) {
                R.id.rbDistance -> 0 // Distance
                R.id.rbTime -> 1      // Time
                R.id.rbCalories -> 2  // Calories
                else -> 0 // Default to distance
            }

            val goalValue = etGoalValue.text.toString().toFloatOrNull() ?: 0f

            if (goalValue == 0f) {
                etGoalValue.error = "Please enter a valid goal value"
                return@setOnClickListener
            }

            val uid = prefs.getUid() ?: ""

            startActivity(Intent(this, RunTrackingActivity::class.java).apply {
                putExtra("goalType", goalType)
                putExtra("goalValue", goalValue)
                putExtra("uid", uid)
            })
            finish()
        }

        tvBack.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }
    }
}
