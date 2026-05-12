package com.fida.app

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.fida.app.utils.PreferenceHelper
import com.google.android.material.button.MaterialButton

class PreRunActivity : AppCompatActivity() {

    private lateinit var prefs: PreferenceHelper
    private lateinit var tvGoalUnit: TextView
    private lateinit var etGoalValue: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pre_run)
        supportActionBar?.hide()

        prefs = PreferenceHelper(this)

        setupViews()
    }

    private fun setupViews() {
        val rgGoalType = findViewById<RadioGroup>(R.id.rgGoalType)
        etGoalValue = findViewById<EditText>(R.id.etGoalValue)
        tvGoalUnit = findViewById<TextView>(R.id.tvGoalUnit)
        val btnStartRun = findViewById<MaterialButton>(R.id.btnStartRun)
        val tvBack = findViewById<TextView>(R.id.tvBackToHome)

        // Update unit label when goal type changes
        rgGoalType.setOnCheckedChangeListener { _, checkedId ->
            updateUnitLabel(checkedId)
        }

        // Set initial unit label (Distance is checked by default)
        updateUnitLabel(R.id.rbDistance)

        btnStartRun.setOnClickListener {
            val selectedGoalTypeId = rgGoalType.checkedRadioButtonId
            val goalType = when (selectedGoalTypeId) {
                R.id.rbDistance -> 0 // Distance
                R.id.rbTime -> 1      // Time
                R.id.rbCalories -> 2  // Calories
                else -> 0 // Default to distance
            }

            val goalValue = etGoalValue.text.toString().toFloatOrNull()

            if (goalValue == null || goalValue <= 0f) {
                etGoalValue.error = "Please enter a valid goal value"
                return@setOnClickListener
            }

            val uid = prefs.getUid() ?: ""

            startActivity(Intent(this, RunCountdownActivity::class.java).apply {
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

    private fun updateUnitLabel(checkedId: Int) {
        val unit = when (checkedId) {
            R.id.rbDistance -> "km"
            R.id.rbTime -> "min"
            R.id.rbCalories -> "kcal"
            else -> "km"
        }
        tvGoalUnit.text = unit

        // Update hint based on goal type
        val hint = when (checkedId) {
            R.id.rbDistance -> "5"
            R.id.rbTime -> "30"
            R.id.rbCalories -> "300"
            else -> "5"
        }
        etGoalValue.hint = hint
    }
}