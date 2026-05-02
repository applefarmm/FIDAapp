package com.fida.app

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.fida.app.models.Run
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RunDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_run_detail)
        supportActionBar?.hide()

        val run = intent.getParcelableExtra<Run>("run")

        if (run != null) {
            setupViews(run)
        } else {
            // Handle case where run data is missing, e.g., show an error message or go back
            finish() // Close the activity if no data is passed
        }
    }

    private fun setupViews(run: Run) {
        val tvRunDate = findViewById<TextView>(R.id.tvRunDateDetail)
        val tvDuration = findViewById<TextView>(R.id.tvDurationDetail)
        val tvDistance = findViewById<TextView>(R.id.tvDistanceDetail)
        val tvGoalType = findViewById<TextView>(R.id.tvGoalTypeDetail)
        val tvGoalValue = findViewById<TextView>(R.id.tvGoalValueDetail)
        val tvCompleted = findViewById<TextView>(R.id.tvCompletedDetail)
        val tvBack = findViewById<TextView>(R.id.tvBackToHistory)

        val date = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            .format(Date(run.timestamp))
        tvRunDate.text = date

        tvDuration.text = formatDuration(run.durationSeconds)
        tvDistance.text = "%.2f km".format(run.distanceMeters / 1000)

        val goalTypeString = when (run.goalType) {
            0 -> "Distance"
            1 -> "Time"
            2 -> "Calories"
            else -> "Unknown"
        }
        tvGoalType.text = goalTypeString
        tvGoalValue.text = "%.1f".format(run.goalValue)

        tvCompleted.text = if (run.completed) "Yes" else "No"

        tvBack.setOnClickListener {
            finish() // Go back to RunHistoryActivity
        }
    }

    private fun formatDuration(seconds: Int): String {
        val mins = seconds / 60
        val secs = seconds % 60
        return "%02d:%02d".format(mins, secs)
    }
}
