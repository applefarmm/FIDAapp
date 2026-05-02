package com.fida.app.fragments

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.fida.app.R
import com.fida.app.models.Achievement

class BadgeUnlockedFragment : DialogFragment() {

    companion object {
        private const val ARG_ACHIEVEMENT = "achievement"

        fun newInstance(achievement: Achievement): BadgeUnlockedFragment {
            val fragment = BadgeUnlockedFragment()
            val args = Bundle()
            args.putParcelable(ARG_ACHIEVEMENT, achievement)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?):
            View? {
        val view = inflater.inflate(R.layout.fragment_badge_unlocked, container, false)

        // Remove title bar
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) // Set background transparent

        setupViews(view)
        return view
    }

    private fun setupViews(view: View) {
        val ivBadgeIcon = view.findViewById<ImageView>(R.id.ivBadgeIcon)
        val tvBadgeName = view.findViewById<TextView>(R.id.tvBadgeName)
        val tvBadgeDescription = view.findViewById<TextView>(R.id.tvBadgeDescription)
        val btnClose = view.findViewById<TextView>(R.id.btnCloseBadgePopup)

        val achievement = arguments?.getParcelable<Achievement>(ARG_ACHIEVEMENT)

        if (achievement != null) {
            Glide.with(this).load(achievement.iconUrl).into(ivBadgeIcon)
            tvBadgeName.text = achievement.title
            tvBadgeDescription.text = achievement.description
        }

        btnClose.setOnClickListener {
            dismiss()
        }
    }
}
