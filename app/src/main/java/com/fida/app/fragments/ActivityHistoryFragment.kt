package com.fida.app.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
            // Handle item click - navigate to SingleActivityLogDetailScreen
            // For now, just log the click, as the detail screen needs to be created
            println("Clicked on activity log: ${log.id}")
            // TODO: Implement navigation to SingleActivityLogDetailScreen
        }
        rvActivityHistory.layoutManager = LinearLayoutManager(context)
        rvActivityHistory.adapter = activityHistoryAdapter
    }

    private fun loadActivityHistory() {
        val uid = PreferenceHelper(requireContext()).getUid() ?: return

        // Fetch all types of activities (runs, water, sleep) and combine them
        // This is a simplified approach; a real implementation might fetch separately or use a unified collection

        // Fetch Runs
        FirestoreRepository.getRuns(uid) { runDataList ->
            val runs = runDataList?.mapNotNull {
                try {
                    val timestamp = it["timestamp"] as Long
                    val distanceMeters = (it["distanceMeters"] as Number).toFloat()
                    val durationSeconds = (it["durationSeconds"] as Long).toInt()
                    ActivityLog(
                        id = "run_${timestamp}", // Unique ID for the log entry
                        type = ActivityLog.ActivityType.RUN,
                        timestamp = timestamp,
                        summary = "Distance: %.2f km".format(distanceMeters / 1000) + " | Duration: ${formatDuration(durationSeconds)}",
                        colorResId = R.color.orange_500 // Example color for runs
                    )
                } catch (e: Exception) {
                    null
                }
            } ?: emptyList()

            activity?.runOnUiThread {
                updateActivityList(runs)
            }
        }

        // Fetch Water Intake (Example - assuming data structure)
        // You'll need to adapt this based on how water intake is stored in Firestore
        // FirestoreRepository.getWaterIntake(uid) { waterDataList -> ... }
        val waterLogs = listOf(
            ActivityLog("water_1678886400", ActivityLog.ActivityType.WATER, 1678886400L, "Intake: 2.5L", R.color.blue_500),
            ActivityLog("water_1678972800", ActivityLog.ActivityType.WATER, 1678972800L, "Intake: 1.8L", R.color.blue_500)
        )
        updateActivityList(waterLogs)

        // Fetch Sleep Recording (Example - assuming data structure)
        // You'll need to adapt this based on how sleep is stored in Firestore
        // FirestoreRepository.getSleepLogs(uid) { sleepDataList -> ... }
        val sleepLogs = listOf(
            ActivityLog("sleep_1678839600", ActivityLog.ActivityType.SLEEP, 1678839600L, "Sleep: 7.5h", R.color.purple_500),
            ActivityLog("sleep_1678926000", ActivityLog.ActivityType.SLEEP, 1678926000L, "Sleep: 8.2h", R.color.purple_500)
        )
        updateActivityList(sleepLogs)

        // Sort combined list by timestamp if necessary
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
        val tvNoActivities = view?.findViewById<TextView>(R.id.tvNoActivities)
        val rvActivityHistory = view?.findViewById<RecyclerView>(R.id.rvActivityHistory)
        if (isEmpty) {
            tvNoActivities?.visibility = View.VISIBLE
            rvActivityHistory?.visibility = View.GONE
        } else {
            tvNoActivities?.visibility = View.GONE
            rvActivityHistory?.visibility = View.VISIBLE
        }
    }
}
