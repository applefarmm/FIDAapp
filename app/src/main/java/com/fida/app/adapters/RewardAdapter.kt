package com.fida.app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.fida.app.R
import com.fida.app.models.Reward

class RewardAdapter(private val rewards: List<Reward>, private val onItemClick: (Reward) -> Unit) :
    RecyclerView.Adapter<RewardAdapter.RewardViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RewardViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_reward, parent, false)
        return RewardViewHolder(view)
    }

    override fun onBindViewHolder(holder: RewardViewHolder, position: Int) {
        val reward = rewards[position]
        holder.bind(reward)
        holder.itemView.setOnClickListener { onItemClick(reward) }
    }

    override fun getItemCount(): Int = rewards.size

    inner class RewardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivRewardIcon: ImageView = itemView.findViewById(R.id.ivRewardIcon)
        private val tvRewardName: TextView = itemView.findViewById(R.id.tvRewardName)
        private val tvRewardDescription: TextView = itemView.findViewById(R.id.tvRewardDescription)
        private val tvRewardCost: TextView = itemView.findViewById(R.id.tvRewardCost)

        fun bind(reward: Reward) {
            Glide.with(itemView.context).load(reward.imageUrl).into(ivRewardIcon)
            tvRewardName.text = reward.name
            tvRewardDescription.text = reward.description
            tvRewardCost.text = "${reward.cost} ${if (reward.type == Reward.Type.COIN) "Coins" else "Gems"}"
        }
    }
}
