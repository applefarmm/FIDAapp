package com.fida.app.fragments

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.fida.app.R
import com.fida.app.utils.FirestoreRepository
import com.fida.app.utils.PreferenceHelper
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class StreakDashboardFragment : Fragment() {

    private lateinit var prefs: PreferenceHelper

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?):
            View? {
        val view = inflater.inflate(R.layout.fragment_streak_dashboard, container, false)
        prefs = PreferenceHelper(requireContext())
        setupViews(view)
        loadStreakCalendar(view)
        return view
    }

    private fun setupViews(view: View) {
        val tvCurrentStreak = view.findViewById<TextView>(R.id.tvCurrentStreak)
        val tvLongestStreak = view.findViewById<TextView>(R.id.tvLongestStreak)
        val tvStreakShieldCount = view.findViewById<TextView>(R.id.tvStreakShieldCount)

        val currentStreak = prefs.getInt("currentStreak") ?: 0
        val longestStreak = prefs.getInt("longestStreak") ?: 0
        val streakShields = prefs.getInt("streakShields") ?: 0

        tvCurrentStreak.text = "$currentStreak days"
        tvLongestStreak.text = "Longest: $longestStreak days"
        tvStreakShieldCount.text = "x $streakShields"
    }

    private fun loadStreakCalendar(view: View) {
        val uid = prefs.getUid() ?: return
        val calendarContainer = view.findViewById<GridLayout>(R.id.streakCalendarGrid)

        if (calendarContainer == null) return

        FirestoreRepository.getUser(uid) { userData ->
            if (userData == null) return@getUser

            val allDays = userData["allDays"] as? Map<String, Map<String, Any>> ?: emptyMap()
            val currentStreak = (userData["streakDays"] as? Long)?.toInt() ?: 0

            activity?.runOnUiThread {
                renderStreakCalendar(calendarContainer, allDays, currentStreak)
            }
        }
    }

    private fun renderStreakCalendar(
        grid: GridLayout,
        allDays: Map<String, Map<String, Any>>,
        currentStreak: Int
    ) {
        grid.removeAllViews()
        grid.columnCount = 7

        val today = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        // Show last 28 days (4 weeks)
        for (week in 3 downTo 0) {
            for (dayOfWeek in 0..6) {
                val cellDate = Calendar.getInstance()
                cellDate.add(Calendar.DAY_OF_MONTH, -(week * 7 + (6 - dayOfWeek)))

                val dateKey = dateFormat.format(cellDate.time)
                val dayData = allDays[dateKey]

                val hasActivity = dayData != null && hasAnyActivity(dayData)
                val isToday = dateFormat.format(Date()) == dateKey

                val cell = createCalendarCell(hasActivity, isToday, currentStreak > 0)
                grid.addView(cell)
            }
        }
    }

    private fun hasAnyActivity(dayData: Map<String, Any>): Boolean {
        val runDistance = (dayData["runDistance"] as? Number)?.toFloat() ?: 0f
        val waterIntake = (dayData["waterIntake"] as? Number)?.toInt() ?: 0
        val sleepHours = (dayData["sleepHours"] as? Number)?.toFloat() ?: 0f
        val steps = (dayData["steps"] as? Number)?.toInt() ?: 0

        return runDistance > 0 || waterIntake > 0 || sleepHours > 0 || steps > 0
    }

    private fun createCalendarCell(hasActivity: Boolean, isToday: Boolean, hasStreak: Boolean): View {
        val cellSize = 36
        val cellMargin = 2

        val cell = LinearLayout(requireContext()).apply {
            layoutParams = GridLayout.LayoutParams().apply {
                width = cellSize
                height = cellSize
                setMargins(cellMargin, cellMargin, cellMargin, cellMargin)
            }
            gravity = Gravity.CENTER
            orientation = LinearLayout.VERTICAL
        }

        val bgDrawable = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            cornerRadius = cellSize / 2f
            when {
                isToday && hasStreak -> setColor(Color.parseColor("#FF6B35")) // Current streak day
                hasActivity -> setColor(Color.parseColor("#4CAF50")) // Active day
                else -> setColor(Color.parseColor("#E0E0E0")) // Inactive day
            }
        }
        cell.background = bgDrawable

        if (isToday) {
            val dot = View(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(6, 6)
                setBackgroundColor(Color.WHITE)
            }
            cell.addView(dot)
        }

        return cell
    }
}
