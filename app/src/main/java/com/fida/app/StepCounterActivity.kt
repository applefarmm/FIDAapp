package com.fida.app

import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.DefaultValueFormatter
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.fida.app.services.StepTrackingService
import com.fida.app.utils.PreferenceHelper
import java.math.RoundingMode
import java.text.DecimalFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class StepCounterActivity : AppCompatActivity() {
    private val db = Firebase.firestore

    private var totalSteps = 0f
    private var stepTarget = 0

    // Firestore doc ID — use uid from SharedPreferences, NOT display username
    private val safeUid: String by lazy {
        intent.getStringExtra("uid")
            ?: PreferenceHelper(this).getUid()
            ?: ""
    }

    // Refresh handler for real-time updates
    private val refreshHandler = Handler(Looper.getMainLooper())
    private val refreshRunnable = object : Runnable {
        override fun run() {
            loadData(safeUid)
            refreshHandler.postDelayed(this, 5000) // Refresh every 5 seconds
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_step_counter)

        supportActionBar?.elevation = 0F
        supportActionBar?.title = "Steps"

        // Back to Main button
        findViewById<com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton>(R.id.btnBackToMain).setOnClickListener {
            finish()
        }

        // Start step tracking service
        StepTrackingService.start(this)

        loadData(safeUid)

        val maxStepTextView: TextView = findViewById(R.id.maxStep_stepScreen)
        val stepProgressCircular = findViewById<com.mikhaellopez.circularprogressbar.CircularProgressBar>(R.id.stepProgressCircular_stepScreen)
        val setStepTargetButton: Button = findViewById(R.id.setStepTarget_stepScreen)

        setStepTargetButton.setOnClickListener {
            val view: View = LayoutInflater.from(this).inflate(R.layout.layout_max_step_dialog, null)
            val editText: TextInputEditText = view.findViewById(R.id.editText)

            val alertDialog: AlertDialog = MaterialAlertDialogBuilder(this)
                .setTitle("Set Target")
                .setView(view)
                .setPositiveButton("Ok") { _, _ ->
                    val targetInput = editText.text.toString()
                    if (targetInput.isNotEmpty()) {
                        stepTarget = targetInput.toInt()
                        maxStepTextView.text = stepTarget.toString()
                        stepProgressCircular.progressMax = stepTarget.toFloat()
                        // Save to SharedPreferences for service notification
                        PreferenceHelper(this).saveMaxSteps(stepTarget)
                        Toast.makeText(this, "Target set to $stepTarget", Toast.LENGTH_SHORT).show()
                        updateStepTarget(safeUid)
                    } else {
                        Toast.makeText(this, "Please enter a valid number", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("Cancel", null).create()

            alertDialog.setOnShowListener {
                alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(resources.getColor(R.color.primaryTextColor, theme))
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(resources.getColor(R.color.primaryTextColor, theme))
            }

            alertDialog.show()
        }

        sendDataToScreen(safeUid)
    }

    override fun onStart() {
        super.onStart()
        loadData(safeUid)
    }

    override fun onResume() {
        super.onResume()
        // Start periodic refresh for real-time step updates
        refreshHandler.post(refreshRunnable)
    }

    override fun onPause() {
        super.onPause()
        // Stop periodic refresh
        refreshHandler.removeCallbacks(refreshRunnable)
    }

    private fun loadData(uid: String) {
        if (uid.isEmpty()) return

        val currentTime = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val userRef = db.collection("users").document(uid)

        val stepCounterTextView: TextView = findViewById(R.id.stepsTaken_stepScreen)
        val maxStepTextView: TextView = findViewById(R.id.maxStep_stepScreen)
        val stepProgressCircular = findViewById<com.mikhaellopez.circularprogressbar.CircularProgressBar>(R.id.stepProgressCircular_stepScreen)

        userRef.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val doc = task.result
                if (doc != null && doc.exists()) {
                    val data = doc.data
                    if (data != null) {
                        @Suppress("UNCHECKED_CAST")
                        val allDays = data["allDays"] as? Map<String, Any>
                        val todayData = allDays?.get(currentTime) as? Map<String, Any>

                        if (todayData != null) {
                            totalSteps = (todayData["stepCounter"] as? Long)?.toFloat() ?: 0f
                            stepCounterTextView.text = totalSteps.toInt().toString()
                            stepProgressCircular.setProgressWithAnimation(totalSteps)
                            
                            // Update other stats
                            convertStepToKm(totalSteps)
                            convertStepToCal(totalSteps)
                            estimateActiveTime(totalSteps)
                        }

                        stepTarget = (data["maxStep"] as? Long)?.toInt() ?: 2500
                        maxStepTextView.text = "Goal: ${DecimalFormat("#,###").format(stepTarget)}"
                        stepProgressCircular.progressMax = stepTarget.toFloat()
                    }
                }
            }
        }
    }

    private fun updateStepTarget(uid: String) {
        if (uid.isEmpty()) return

        val userRef = db.collection("users").document(uid)
        userRef.update("maxStep", stepTarget)
            .addOnSuccessListener {
                Log.d("StepCounterActivity", "Step target updated to $stepTarget")
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error updating step target", Toast.LENGTH_SHORT).show()
                Log.e("StepCounterActivity", e.toString())
            }
    }

    private fun sendDataToScreen(uid: String) {
        if (uid.isEmpty()) return

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val monthDateFormatter = DateTimeFormatter.ofPattern("M/d")
        val currentTimeNoFormat = LocalDate.now()
        val currentTime = LocalDate.now().format(formatter)
        val userRef = db.collection("users").document(uid)

        val barChart = findViewById<BarChart>(R.id.stepChart_stepScreen)
        val dataList: ArrayList<BarEntry> = ArrayList()
        val xAxisLabels: MutableList<String> = ArrayList()

        userRef.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val doc = task.result
                if (doc != null && doc.exists()) {
                    val data = doc.data
                    if (data != null) {
                        @Suppress("UNCHECKED_CAST")
                        val allDays = data["allDays"] as? Map<String, Any>

                        for (i in 6 downTo 0) {
                            var stepCounter = 0f
                            val dayKey = currentTimeNoFormat.minusDays(i.toLong()).format(formatter)
                            val dayData = allDays?.get(dayKey) as? Map<String, Any>
                            if (dayData != null) {
                                stepCounter = (dayData["stepCounter"] as? Long)?.toFloat() ?: 0f
                            }
                            dataList.add(BarEntry((6 - i).toFloat(), stepCounter))
                            xAxisLabels.add(currentTimeNoFormat.minusDays(i.toLong()).format(monthDateFormatter))
                        }

                        val barDataSet = BarDataSet(dataList, "Steps")
                        barDataSet.valueFormatter = DefaultValueFormatter(0)
                        barDataSet.color = Color.WHITE
                        barDataSet.valueTextColor = Color.WHITE

                        barChart.xAxis.textColor = Color.WHITE
                        barChart.axisLeft.textColor = Color.WHITE
                        barChart.axisRight.textColor = Color.WHITE
                        barChart.legend.textColor = Color.WHITE
                        barChart.xAxis.setDrawGridLines(false)
                        barChart.axisLeft.setDrawGridLines(false)
                        barChart.axisRight.isEnabled = false

                        val barData = BarData(barDataSet)
                        barChart.xAxis.valueFormatter = IndexAxisValueFormatter(xAxisLabels)
                        barChart.xAxis.position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
                        barChart.setFitBars(true)
                        barChart.data = barData
                        barChart.description.isEnabled = false
                        barChart.animateY(700, Easing.EaseOutSine)
                        barChart.setTouchEnabled(false)

                        // Update distance and calories burnt for today
                        val todayData = allDays?.get(currentTime) as? Map<String, Any>
                        if (todayData != null) {
                            val todaySteps = (todayData["stepCounter"] as? Long)?.toFloat() ?: 0f
                            convertStepToKm(todaySteps)
                            convertStepToCal(todaySteps)
                            estimateActiveTime(todaySteps)
                        }
                    }
                }
            }
        }
    }

    // Convert total steps to km
    private fun convertStepToKm(totalSteps: Float) {
        val MAGIC_NUMBER = 1312.335958
        val distanceTextView: TextView = findViewById(R.id.distance_StepScreen)
        val df = DecimalFormat("#.##").apply { roundingMode = RoundingMode.CEILING }
        val km = totalSteps / MAGIC_NUMBER
        distanceTextView.text = df.format(km)
    }

    // Convert total steps to calories
    private fun convertStepToCal(totalSteps: Float) {
        val MAGIC_NUMBER = 28.985507
        val calTextView: TextView = findViewById(R.id.cal_stepScreen)
        val cal = totalSteps / MAGIC_NUMBER
        calTextView.text = cal.toInt().toString()
    }

    // Estimate active time based on steps (approx 100 steps per minute)
    private fun estimateActiveTime(totalSteps: Float) {
        val timeTextView: TextView = findViewById(R.id.time_StepScreen)
        val minutes = (totalSteps / 100).toInt()
        timeTextView.text = minutes.toString()
    }
}