package com.fida.app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.fida.app.R
import com.fida.app.models.Achievement

class AchievementsAdapter(private val achievements: List<Achievement>) :
    RecyclerView.Adapter<AchievementsAdapter.AchievementViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AchievementViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_achievement, parent, false)
        return AchievementViewHolder(view)
    }

    override fun onBindViewHolder(holder: AchievementViewHolder, position: Int) {
        val achievement = achievements[position]
        holder.bind(achievement)
    }

    override fun getItemCount(): Int = achievements.size

    inner class AchievementViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivAchievementIcon: ImageView = itemView.findViewById(R.id.ivAchievementIcon)
        private val tvAchievementTitle: TextView = itemView.findViewById(R.id.tvAchievementTitle)
        private val tvAchievementDescription: TextView = itemView.findViewById(R.id.tvAchievementDescription)
        private val ivLockIcon: ImageView = itemView.findViewById(R.id.ivLockIcon)

        fun bind(achievement: Achievement) {
            tvAchievementTitle.text = achievement.title
            tvAchievementDescription.text = achievement.description

            if (achievement.isUnlocked) {
                // Load unlocked achievement icon
                Glide.with(itemView.context)
                    .load(achievement.iconUrl)
                    .placeholder(R.drawable.ic_achievement_placeholder) // Placeholder if URL is invalid
                    .into(ivAchievementIcon)
                ivLockIcon.visibility = View.GONE
            } else {
                // Show a locked state
                ivLockIcon.visibility = View.VISIBLE
                ivLockIcon.setImageResource(R.drawable.ic_lock) // Assuming you have a lock icon
                ivAchievementIcon.setColorFilter(itemView.context.resources.getColor(R.color.grey_500, null)) // Grey out the icon
            }
        }
    }
}
