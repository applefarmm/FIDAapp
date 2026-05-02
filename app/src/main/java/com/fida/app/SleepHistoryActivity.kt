package com.fida.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.fida.app.databinding.ActivitySleepHistoryBinding
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry

class SleepHistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySleepHistoryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySleepHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        setupChart()
    }

    private fun setupChart() {
        // TODO: Fetch actual sleep history
        val entries = ArrayList<BarEntry>()
        entries.add(BarEntry(0f, 7.5f))
        entries.add(BarEntry(1f, 8.2f))
        entries.add(BarEntry(2f, 6.8f))
        entries.add(BarEntry(3f, 7.0f))
        entries.add(BarEntry(4f, 8.5f))
        entries.add(BarEntry(5f, 7.8f))
        entries.add(BarEntry(6f, 6.5f))

        val dataSet = BarDataSet(entries, "Sleep Duration (hours)")
        val barData = BarData(dataSet)
        binding.sleepBarChart.data = barData
        binding.sleepBarChart.invalidate() // refresh
    }
}
