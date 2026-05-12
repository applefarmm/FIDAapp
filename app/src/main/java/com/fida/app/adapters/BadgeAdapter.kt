package com.fida.app.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.fida.app.R
import com.fida.app.models.Badge

class BadgeAdapter(
    private val badges: List<Badge>,
    private val onBadgeClick: (Badge) -> Unit
) : RecyclerView.Adapter<BadgeAdapter.BadgeViewHolder>() {

    class BadgeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivBadgeIcon: ImageView = view.findViewById(R.id.ivBadgeIcon)
        val tvBadgeTitle: TextView = view.findViewById(R.id.tvBadgeTitle)
        val tvBadgeDescription: TextView = view.findViewById(R.id.tvBadgeDescription)
        val tvBadgeRarity: TextView = view.findViewById(R.id.tvBadgeRarity)
        val tvBadgeCategory: TextView = view.findViewById(R.id.tvBadgeCategory)
        val tvBadgeProgress: TextView = view.findViewById(R.id.tvBadgeProgress)
        val progressBar: ProgressBar = view.findViewById(R.id.progressBarBadge)
        val tvBadgeRewards: TextView = view.findViewById(R.id.tvBadgeRewards)
        val ivLockOverlay: ImageView = view.findViewById(R.id.ivLockOverlay)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BadgeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_badge, parent, false)
        return BadgeViewHolder(view)
    }

    override fun onBindViewHolder(holder: BadgeViewHolder, position: Int) {
        val badge = badges[position]

        holder.tvBadgeTitle.text = badge.title
        holder.tvBadgeDescription.text = badge.description
        holder.tvBadgeCategory.text = badge.category.uppercase()
        holder.tvBadgeRarity.text = badge.rarity.uppercase()

        val rarityColor = getRarityColor(badge.rarity)
        holder.tvBadgeRarity.setTextColor(rarityColor)

        if (badge.isUnlocked) {
            Glide.with(holder.itemView.context)
                .load(badge.iconUrl)
                .placeholder(R.drawable.ic_badge_placeholder)
                .into(holder.ivBadgeIcon)
            holder.ivLockOverlay.visibility = View.GONE
            holder.progressBar.visibility = View.GONE
            holder.tvBadgeProgress.visibility = View.GONE
            holder.tvBadgeRewards.visibility = View.VISIBLE
            holder.tvBadgeRewards.text = "Rewards: +${badge.xpReward} XP, +${badge.coinReward} Coins"
        } else {
            Glide.with(holder.itemView.context)
                .load(badge.iconUrl)
                .placeholder(R.drawable.ic_badge_placeholder)
                .into(holder.ivBadgeIcon)
            holder.ivBadgeIcon.setColorFilter(Color.parseColor("#888888"))
            holder.ivLockOverlay.visibility = View.VISIBLE

            holder.progressBar.visibility = View.VISIBLE
            holder.progressBar.progress = badge.progressPercent
            holder.tvBadgeProgress.visibility = View.VISIBLE
            holder.tvBadgeProgress.text = "${badge.progress}/${badge.targetProgress}"
            holder.tvBadgeRewards.visibility = View.GONE
        }

        holder.itemView.setOnClickListener { onBadgeClick(badge) }
    }

    private fun getRarityColor(rarity: String): Int {
        return when (rarity.lowercase()) {
            "legendary" -> Color.parseColor("#FFD700")
            "epic" -> Color.parseColor("#9C27B0")
            "rare" -> Color.parseColor("#2196F3")
            else -> Color.parseColor("#888888")
        }
    }

    override fun getItemCount() = badges.size
}