package com.fida.app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.fida.app.R
import com.fida.app.models.Challenge
import com.google.android.material.button.MaterialButton

class ChallengeAdapter(
    private val challenges: List<Challenge>,
    private val onJoin: (Challenge) -> Unit,
    private val onClaim: (Challenge) -> Unit
) : RecyclerView.Adapter<ChallengeAdapter.ChallengeViewHolder>() {

    class ChallengeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvChallengeIcon: TextView = view.findViewById(R.id.tvChallengeIcon)
        val tvChallengeTitle: TextView = view.findViewById(R.id.tvChallengeTitle)
        val tvChallengeDescription: TextView = view.findViewById(R.id.tvChallengeDescription)
        val tvChallengeRewards: TextView = view.findViewById(R.id.tvChallengeRewards)
        val tvChallengeType: TextView = view.findViewById(R.id.tvChallengeType)
        val tvProgress: TextView = view.findViewById(R.id.tvChallengeProgress)
        val progressBar: ProgressBar = view.findViewById(R.id.progressBarChallenge)
        val btnJoinChallenge: MaterialButton = view.findViewById(R.id.btnJoinChallenge)
        val btnClaimReward: MaterialButton = view.findViewById(R.id.btnClaimReward)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChallengeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_challenge, parent, false)
        return ChallengeViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChallengeViewHolder, position: Int) {
        val challenge = challenges[position]

        holder.tvChallengeIcon.text = challenge.icon
        holder.tvChallengeTitle.text = challenge.title
        holder.tvChallengeDescription.text = challenge.description
        holder.tvChallengeRewards.text = buildRewardText(challenge)
        holder.tvChallengeType.text = challenge.type.uppercase()

        val progressPercent = if (challenge.targetValue > 0) {
            (challenge.progress * 100) / challenge.targetValue
        } else 0

        holder.progressBar.progress = progressPercent
        holder.tvProgress.text = "${challenge.progress}/${challenge.targetValue}"

        when {
            challenge.isCompleted && !challenge.isActive -> {
                holder.btnJoinChallenge.visibility = View.GONE
                holder.btnClaimReward.visibility = View.VISIBLE
                holder.btnClaimReward.text = "Claim Reward"
                holder.btnClaimReward.isEnabled = true
                holder.btnClaimReward.setOnClickListener { onClaim(challenge) }
            }
            challenge.isActive && !challenge.isCompleted -> {
                holder.btnJoinChallenge.visibility = View.GONE
                holder.btnClaimReward.visibility = View.GONE
            }
            challenge.isCompleted && challenge.isActive -> {
                holder.btnJoinChallenge.visibility = View.GONE
                holder.btnClaimReward.visibility = View.VISIBLE
                holder.btnClaimReward.text = "Claimed ✓"
                holder.btnClaimReward.isEnabled = false
            }
            else -> {
                holder.btnJoinChallenge.visibility = View.VISIBLE
                holder.btnClaimReward.visibility = View.GONE
                holder.btnJoinChallenge.text = "Join"
                holder.btnJoinChallenge.isEnabled = true
                holder.btnJoinChallenge.setOnClickListener { onJoin(challenge) }
            }
        }
    }

    private fun buildRewardText(challenge: Challenge): String {
        val rewards = mutableListOf<String>()
        if (challenge.xpReward > 0) rewards.add("+${challenge.xpReward} XP")
        if (challenge.coinReward > 0) rewards.add("+${challenge.coinReward} 🪙")
        if (challenge.gemReward > 0) rewards.add("+${challenge.gemReward} 💎")
        return rewards.joinToString(" | ")
    }

    override fun getItemCount() = challenges.size
}