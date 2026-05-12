package com.fida.app

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import com.fida.app.utils.PreferenceHelper
import com.google.android.material.textfield.TextInputEditText

@Deprecated("Use PreRunActivity instead - includes countdown and better UI")
class RunPreSetupActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_run_pre_setup)
        supportActionBar?.hide()

        val prefs = PreferenceHelper(this)

        val spinnerGoalType = findViewById<Spinner>(R.id.spinnerGoalType)
        val etGoalValue = findViewById<TextInputEditText>(R.id.etGoalValue)
        val btnStartRun = findViewById<Button>(R.id.btnStartRun)

        val goalTypes = arrayOf("Distance (km)", "Time (minutes)", "Calories")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, goalTypes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerGoalType.adapter = adapter

        btnStartRun.setOnClickListener {
            val goalType = spinnerGoalType.selectedItemPosition
            val goalValue = etGoalValue.text.toString().toFloatOrNull() ?: 0f

            val intent = Intent(this, RunTrackingActivity::class.java)
            intent.putExtra("goalType", goalType)
            intent.putExtra("goalValue", goalValue)
            intent.putExtra("uid", prefs.getUid() ?: "")
            startActivity(intent)
        }
    }
}