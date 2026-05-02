package com.fida.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.fida.app.databinding.ActivityWaterHistoryBinding
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry

class WaterHistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWaterHistoryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWaterHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        setupChart()
    }

    private fun setupChart() {
        // TODO: Fetch actual water intake history
        val entries = ArrayList<BarEntry>()
        entries.add(BarEntry(0f, 1800f))
        entries.add(BarEntry(1f, 2200f))
        entries.add(BarEntry(2f, 2000f))
        entries.add(BarEntry(3f, 1500f))
        entries.add(BarEntry(4f, 2500f))
        entries.add(BarEntry(5f, 1900f))
        entries.add(BarEntry(6f, 2100f))

        val dataSet = BarDataSet(entries, "Water Intake (ml)")
        val barData = BarData(dataSet)
        binding.waterBarChart.data = barData
        binding.waterBarChart.invalidate() // refresh
    }
}
