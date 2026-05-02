package com.fida.app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.fida.app.R
import com.fida.app.models.ActivityLog

class ActivityHistoryAdapter(private val logs: List<ActivityLog>, private val onItemClick: (ActivityLog) -> Unit) :
    RecyclerView.Adapter<ActivityHistoryAdapter.ActivityLogViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActivityLogViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_activity_log, parent, false)
        return ActivityLogViewHolder(view)
    }

    override fun onBindViewHolder(holder: ActivityLogViewHolder, position: Int) {
        val log = logs[position]
        holder.bind(log)
        holder.itemView.setOnClickListener { onItemClick(log) }
    }

    override fun getItemCount(): Int = logs.size

    inner class ActivityLogViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvActivityType: TextView = itemView.findViewById(R.id.tvActivityType)
        private val tvActivityTimestamp: TextView = itemView.findViewById(R.id.tvActivityTimestamp)
        private val tvActivitySummary: TextView = itemView.findViewById(R.id.tvActivitySummary)

        fun bind(log: ActivityLog) {
            tvActivityType.text = when (log.type) {
                ActivityLog.ActivityType.RUN -> "Run"
                ActivityLog.ActivityType.WATER -> "Water"
                ActivityLog.ActivityType.SLEEP -> "Sleep"
                else -> "Other"
            }
            // Set the background color based on activity type
            val backgroundColor = ContextCompat.getColor(itemView.context, log.colorResId)
            itemView.setBackgroundColor(backgroundColor)

            tvActivityTimestamp.text = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
                .format(java.util.Date(log.timestamp))

            tvActivitySummary.text = log.summary
        }
    }
}
