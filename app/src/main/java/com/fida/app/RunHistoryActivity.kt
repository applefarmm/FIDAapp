package com.fida.app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fida.app.adapters.RunHistoryAdapter
import com.fida.app.models.Run
import com.fida.app.utils.FirestoreRepository
import com.fida.app.utils.PreferenceHelper

class RunHistoryActivity : AppCompatActivity() {

    private lateinit var prefs: PreferenceHelper
    private lateinit var runHistoryAdapter: RunHistoryAdapter
    private val runs = mutableListOf<Run>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_run_history)
        supportActionBar?.hide()

        prefs = PreferenceHelper(this)

        setupViews()
        loadRunHistory()
    }

    private fun setupViews() {
        val rvRunHistory = findViewById<RecyclerView>(R.id.rvRunHistory)
        runHistoryAdapter = RunHistoryAdapter(runs) { run ->
            // Handle item click - navigate to RunDetailActivity
            val intent = Intent(this, RunDetailActivity::class.java)
            intent.putExtra("run", run) // Pass the run data
            startActivity(intent)
        }
        rvRunHistory.layoutManager = LinearLayoutManager(this)
        rvRunHistory.adapter = runHistoryAdapter

        val tvBackToHome = findViewById<TextView>(R.id.tvBackToHome)
        tvBackToHome.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }
    }

    private fun loadRunHistory() {
        val uid = prefs.getUid() ?: return
        FirestoreRepository.getRuns(uid) { runDataList ->
            if (runDataList != null) {
                runs.clear()
                val mappedRuns = runDataList.mapNotNull {
                    // Ensure all required fields are present and correctly typed
                    try {
                        val timestamp = it["timestamp"] as Long
                        val durationSeconds = it["durationSeconds"] as Long
                        val distanceMeters = (it["distanceMeters"] as Number).toFloat()
                        val goalType = (it["goalType"] as Long).toInt()
                        val goalValue = (it["goalValue"] as Number).toFloat()
                        val completed = it["completed"] as Boolean

                        Run(
                            timestamp = timestamp,
                            durationSeconds = durationSeconds.toInt(),
                            distanceMeters = distanceMeters,
                            goalType = goalType,
                            goalValue = goalValue,
                            completed = completed
                        )
                    } catch (e: Exception) {
                        // Log error or handle missing data gracefully
                        null
                    }
                }
                runs.addAll(mappedRuns)
                runOnUiThread {
                    runHistoryAdapter.notifyDataSetChanged()
                    updateEmptyState(runs.isEmpty())
                }
            } else {
                runOnUiThread {
                    updateEmptyState(true)
                }
            }
        }
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        val tvNoRuns = findViewById<TextView>(R.id.tvNoRuns)
        val rvRunHistory = findViewById<RecyclerView>(R.id.rvRunHistory)
        if (isEmpty) {
            tvNoRuns.visibility = View.VISIBLE
            rvRunHistory.visibility = View.GONE
        } else {
            tvNoRuns.visibility = View.GONE
            rvRunHistory.visibility = View.VISIBLE
        }
    }
}
