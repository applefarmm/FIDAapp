package com.fida.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.fida.app.databinding.ActivityWaterHistoryBinding
import com.fida.app.utils.FirestoreRepository
import com.fida.app.utils.PreferenceHelper
import com.github.mikephil.charting.data.BarEntry
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class WaterHistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWaterHistoryBinding
    private lateinit var prefs: PreferenceHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWaterHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        prefs = PreferenceHelper(this)
        loadWaterHistory()
    }

    private fun loadWaterHistory() {
        val uid = prefs.getUid() ?: return

        FirestoreRepository.getUser(uid) { userData ->
            if (userData == null) return@getUser

            val allDays = userData["allDays"] as? Map<String, Map<String, Any>> ?: emptyMap()

            val entries = ArrayList<BarEntry>()
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val today = Calendar.getInstance()

            // Last 7 days
            for (i in 6 downTo 0) {
                val date = Calendar.getInstance()
                date.add(Calendar.DAY_OF_MONTH, -i)
                val dateKey = dateFormat.format(date.time)

                val dayData = allDays[dateKey]
                val waterIntake = (dayData?.get("waterIntake") as? Number)?.toFloat() ?: 0f
                entries.add(BarEntry(6 - i.toFloat(), waterIntake))
            }

            runOnUiThread {
                setupChart(entries)
            }
        }
    }

    private fun setupChart(entries: ArrayList<BarEntry>) {
        val dataSet = com.github.mikephil.charting.data.BarDataSet(entries, "Water Intake (ml)")
        dataSet.color = android.graphics.Color.parseColor("#2196F3")
        val barData = com.github.mikephil.charting.data.BarData(dataSet)
        barData.barWidth = 0.8f

        binding.waterBarChart.data = barData
        binding.waterBarChart.description.isEnabled = false
        binding.waterBarChart.setFitBars(true)
        binding.waterBarChart.invalidate()
    }
}
