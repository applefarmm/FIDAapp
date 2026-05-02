package com.fida.app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.fida.app.R
import com.fida.app.models.Run

class RunHistoryAdapter(private val runs: List<Run>, private val onItemClick: (Run) -> Unit) : 
    RecyclerView.Adapter<RunHistoryAdapter.RunViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RunViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_run_history, parent, false)
        return RunViewHolder(view)
    }

    override fun onBindViewHolder(holder: RunViewHolder, position: Int) {
        val run = runs[position]
        holder.bind(run)
        holder.itemView.setOnClickListener { onItemClick(run) }
    }

    override fun getItemCount(): Int = runs.size

    inner class RunViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvRunDate: TextView = itemView.findViewById(R.id.tvRunDate)
        private val tvRunDetails: TextView = itemView.findViewById(R.id.tvRunDetails)
        private val tvGoalStatus: TextView = itemView.findViewById(R.id.tvGoalStatus)

        fun bind(run: Run) {
            val date = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
                .format(java.util.Date(run.timestamp))
            tvRunDate.text = date

            val distanceKm = run.distanceMeters / 1000
            val durationFormatted = formatDuration(run.durationSeconds)
            tvRunDetails.text = "Distance: %.2f km | Duration: %s".format(distanceKm, durationFormatted)

            tvGoalStatus.text = if (run.completed) "Goal Achieved!" else "Goal Not Achieved"
        }
    }

    private fun formatDuration(seconds: Int): String {
        val mins = seconds / 60
        val secs = seconds % 60
        return "%02d:%02d".format(mins, secs)
    }
}
