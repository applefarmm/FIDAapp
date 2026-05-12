package com.fida.app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.fida.app.R
import com.fida.app.models.Quest
import com.google.android.material.button.MaterialButton

class QuestAdapter(
    private val quests: List<Quest>,
    private val onAccept: (Quest) -> Unit
) : RecyclerView.Adapter<QuestAdapter.QuestViewHolder>() {

    class QuestViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvQuestIcon: TextView = view.findViewById(R.id.tvQuestIcon)
        val tvQuestTitle: TextView = view.findViewById(R.id.tvQuestTitle)
        val tvQuestDescription: TextView = view.findViewById(R.id.tvQuestDescription)
        val tvQuestRewards: TextView = view.findViewById(R.id.tvQuestRewards)
        val btnAcceptQuest: MaterialButton = view.findViewById(R.id.btnAcceptQuest)
        val tvQuestType: TextView = view.findViewById(R.id.tvQuestType)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_quest, parent, false)
        return QuestViewHolder(view)
    }

    override fun onBindViewHolder(holder: QuestViewHolder, position: Int) {
        val quest = quests[position]

        holder.tvQuestIcon.text = quest.icon
        holder.tvQuestTitle.text = quest.title
        holder.tvQuestDescription.text = quest.description
        holder.tvQuestRewards.text = "+${quest.xpReward} XP | +${quest.coinReward} 🪙"
        holder.tvQuestType.text = quest.type.uppercase()

        if (quest.accepted) {
            holder.btnAcceptQuest.text = if (quest.completed) "Completed ✓" else "In Progress"
            holder.btnAcceptQuest.isEnabled = false
        } else {
            holder.btnAcceptQuest.text = "Accept"
            holder.btnAcceptQuest.isEnabled = true
            holder.btnAcceptQuest.setOnClickListener { onAccept(quest) }
        }
    }

    override fun getItemCount() = quests.size
}