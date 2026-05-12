package com.fida.app.fragments

import android.animation.AnimatorInflater
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.fida.app.R
import com.fida.app.utils.PreferenceHelper

class LevelUpFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?):
            View? {
        val view = inflater.inflate(R.layout.fragment_level_up, container, false)
        setupViews(view)
        return view
    }

    private fun setupViews(view: View) {
        val tvLevelUp = view.findViewById<TextView>(R.id.tvLevelUp)
        val prefs = PreferenceHelper(requireContext())
        val newLevel = prefs.getInt("level") ?: 1
        tvLevelUp.text = "Congratulations! You've reached Level $newLevel!"

        // Simple scale and alpha animation
        val scaleUp = AnimatorInflater.loadAnimator(context, R.animator.scale_up)
        scaleUp.setTarget(tvLevelUp)
        scaleUp.start()
    }
}
