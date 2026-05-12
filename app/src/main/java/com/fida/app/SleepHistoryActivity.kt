package com.fida.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.fida.app.databinding.ActivitySleepHistoryBinding
import com.fida.app.utils.FirestoreRepository
import com.fida.app.utils.PreferenceHelper
import com.github.mikephil.charting.data.BarEntry
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class SleepHistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySleepHistoryBinding
    private lateinit var prefs: PreferenceHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySleepHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        prefs = PreferenceHelper(this)
        loadSleepHistory()
    }

    private fun loadSleepHistory() {
        val uid = prefs.getUid() ?: return

        FirestoreRepository.getUser(uid) { userData ->
            if (userData == null) return@getUser

            val allDays = userData["allDays"] as? Map<String, Map<String, Any>> ?: emptyMap()

            val entries = ArrayList<BarEntry>()
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

            // Last 7 days
            for (i in 6 downTo 0) {
                val date = Calendar.getInstance()
                date.add(Calendar.DAY_OF_MONTH, -i)
                val dateKey = dateFormat.format(date.time)

                val dayData = allDays[dateKey]
                val sleepHours = (dayData?.get("sleepHours") as? Number)?.toFloat() ?: 0f
                entries.add(BarEntry(6 - i.toFloat(), sleepHours))
            }

            runOnUiThread {
                setupChart(entries)
            }
        }
    }

    private fun setupChart(entries: ArrayList<BarEntry>) {
        val dataSet = com.github.mikephil.charting.data.BarDataSet(entries, "Sleep Duration (hours)")
        dataSet.color = android.graphics.Color.parseColor("#9C27B0")
        val barData = com.github.mikephil.charting.data.BarData(dataSet)
        barData.barWidth = 0.8f

        binding.sleepBarChart.data = barData
        binding.sleepBarChart.description.isEnabled = false
        binding.sleepBarChart.setFitBars(true)
        binding.sleepBarChart.invalidate()
    }
}
