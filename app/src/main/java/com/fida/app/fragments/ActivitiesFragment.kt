package com.fida.app.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.fida.app.PreRunActivity
import com.fida.app.R
import com.fida.app.RunTrackingActivity
import com.fida.app.WaterIntakeActivity // Assuming this activity exists
import com.fida.app.RecordSleepActivity // Assuming this activity exists
import com.fida.app.utils.PreferenceHelper

class ActivitiesFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?):
            View? {
        val view = inflater.inflate(R.layout.fragment_activities, container, false)
        setupViews(view)
        return view
    }

    private fun setupViews(view: View) {
        val btnStartRun = view.findViewById<Button>(R.id.btnStartRun)
        val btnLogWater = view.findViewById<Button>(R.id.btnLogWater)
        val btnRecordSleep = view.findViewById<Button>(R.id.btnRecordSleep)

        btnStartRun.setOnClickListener {
            // Navigate to PreRunActivity to set goals before starting a run
            startActivity(Intent(context, PreRunActivity::class.java))
        }

        btnLogWater.setOnClickListener {
            // Navigate to WaterIntakeActivity
            startActivity(Intent(context, WaterIntakeActivity::class.java))
        }

        btnRecordSleep.setOnClickListener {
            // Navigate to RecordSleepActivity
            startActivity(Intent(context, RecordSleepActivity::class.java))
        }
    }
}
