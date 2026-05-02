package com.fida.app

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.fida.app.models.ActivityLog
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SingleActivityLogDetailScreen : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_single_activity_log_detail)
        supportActionBar?.hide()

        val activityLog = intent.getParcelableExtra<ActivityLog>("activityLog")

        if (activityLog != null) {
            setupViews(activityLog)
        } else {
            // Handle case where activity log data is missing
            finish() // Close the activity if no data is passed
        }
    }

    private fun setupViews(activityLog: ActivityLog) {
        val tvActivityType = findViewById<TextView>(R.id.tvActivityTypeDetail)
        val tvTimestamp = findViewById<TextView>(R.id.tvTimestampDetail)
        val tvSummary = findViewById<TextView>(R.id.tvSummaryDetail)
        val tvBack = findViewById<TextView>(R.id.tvBackToHistoryDetail)

        tvActivityType.text = when (activityLog.type) {
            com.fida.app.models.ActivityLog.ActivityType.RUN -> "Run Details"
            com.fida.app.models.ActivityLog.ActivityType.WATER -> "Water Intake Details"
            com.fida.app.models.ActivityLog.ActivityType.SLEEP -> "Sleep Log Details"
            else -> "Activity Details"
        }

        val date = SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault())
            .format(Date(activityLog.timestamp))
        tvTimestamp.text = date

        tvSummary.text = activityLog.summary

        // TODO: Add more detailed breakdowns based on activity type
        // For example, for runs: display pace, elevation, map snippet
        // For water: display total amount, goal achieved status
        // For sleep: display duration, quality rating, goals met

        tvBack.setOnClickListener {
            finish() // Go back to ActivityHistoryFragment
        }
    }
}
