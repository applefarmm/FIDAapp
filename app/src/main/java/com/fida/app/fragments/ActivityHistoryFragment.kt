package com.fida.app.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fida.app.R
import com.fida.app.adapters.ActivityHistoryAdapter
import com.fida.app.models.ActivityLog
import com.fida.app.utils.FirestoreRepository
import com.fida.app.utils.PreferenceHelper

class ActivityHistoryFragment : Fragment() {

    private lateinit var activityHistoryAdapter: ActivityHistoryAdapter
    private val activityLogs = mutableListOf<ActivityLog>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?):
            View? {
        val view = inflater.inflate(R.layout.fragment_activity_history, container, false)
        setupViews(view)
        loadActivityHistory()
        return view
    }

    private fun setupViews(view: View) {
        val rvActivityHistory = view.findViewById<RecyclerView>(R.id.rvActivityHistory)
        activityHistoryAdapter = ActivityHistoryAdapter(activityLogs) { log ->
            println("Clicked on activity log: ${log.id}")
        }
        rvActivityHistory.layoutManager = LinearLayoutManager(context)
        rvActivityHistory.adapter = activityHistoryAdapter
    }

    private fun loadActivityHistory() {
        val uid = PreferenceHelper(requireContext()).getUid() ?: return

        // Fetch Runs
        FirestoreRepository.getRuns(uid) { runDataList ->
            val runs = runDataList?.mapNotNull {
                try {
                    val timestamp = it["timestamp"] as Long
                    val distanceMeters = (it["distanceMeters"] as Number).toFloat()
                    val durationSeconds = (it["durationSeconds"] as Long).toInt()
                    ActivityLog(
                        id = "run_${timestamp}",
                        type = ActivityLog.ActivityType.RUN,
                        timestamp = timestamp,
                        summary = "Distance: %.2f km".format(distanceMeters / 1000) + " | Duration: ${formatDuration(durationSeconds)}",
                        colorResId = R.color.orange_500
                    )
                } catch (e: Exception) {
                    null
                }
            } ?: emptyList()

            activity?.runOnUiThread {
                updateActivityList(runs)
            }
        }
        activityLogs.sortByDescending { it.timestamp }
        activityHistoryAdapter.notifyDataSetChanged()
        updateEmptyState(activityLogs.isEmpty())
    }

    private fun updateActivityList(newLogs: List<ActivityLog>) {
        activityLogs.addAll(newLogs)
        activityHistoryAdapter.notifyDataSetChanged()
        updateEmptyState(activityLogs.isEmpty())
    }

    private fun formatDuration(seconds: Int): String {
        val mins = seconds / 60
        val secs = seconds % 60
        return "%02d:%02d".format(mins, secs)
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        val tvNoActivities = view?.findViewById<TextView>(R.id.tvNoActivities) ?: return
        val rvActivityHistory = view?.findViewById<RecyclerView>(R.id.rvActivityHistory) ?: return
        if (isEmpty) {
            tvNoActivities.visibility = View.VISIBLE
            rvActivityHistory.visibility = View.GONE
        } else {
            tvNoActivities.visibility = View.GONE
            rvActivityHistory.visibility = View.VISIBLE
        }
    }
}
