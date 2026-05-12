package com.fida.app

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fida.app.adapters.QuestAdapter
import com.fida.app.models.Quest
import com.fida.app.utils.FirestoreRepository
import com.fida.app.utils.PreferenceHelper
import com.google.android.material.button.MaterialButton

class QuestBoardActivity : AppCompatActivity() {

    private lateinit var prefs: PreferenceHelper
    private lateinit var questAdapter: QuestAdapter
    private val quests = mutableListOf<Quest>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quest_board)
        supportActionBar?.hide()

        prefs = PreferenceHelper(this)

        setupViews()
        loadQuests()
    }

    private fun setupViews() {
        val rvQuests = findViewById<RecyclerView>(R.id.rvQuests)
        questAdapter = QuestAdapter(quests) { quest ->
            acceptQuest(quest)
        }
        rvQuests.layoutManager = LinearLayoutManager(this)
        rvQuests.adapter = questAdapter

        findViewById<MaterialButton>(R.id.btnCloseQuests).setOnClickListener {
            finish()
        }
    }

    private fun loadQuests() {
        val uid = prefs.getUid() ?: return
        FirestoreRepository.getUserQuests(uid) { questDataList ->
            if (questDataList != null) {
                quests.clear()
                val mappedQuests = questDataList.map { data ->
                    Quest(
                        title = data["title"] as? String ?: "",
                        description = data["description"] as? String ?: "",
                        icon = data["icon"] as? String ?: "",
                        xpReward = (data["xpReward"] as? Long)?.toInt() ?: 0,
                        coinReward = (data["coinReward"] as? Long)?.toInt() ?: 0,
                        type = data["type"] as? String ?: "",
                        accepted = data["accepted"] as? Boolean ?: false,
                        completed = data["completed"] as? Boolean ?: false
                    )
                }
                quests.addAll(mappedQuests)
                runOnUiThread {
                    questAdapter.notifyDataSetChanged()
                    findViewById<TextView>(R.id.tvQuestCount).text = "${quests.size} quests available"
                }
            } else {
                loadDefaultQuests()
            }
        }
    }

    private fun loadDefaultQuests() {
        quests.clear()
        quests.addAll(listOf(
            Quest("Daily Run", "Complete a 30-minute run", "🏃", 50, 20, "daily"),
            Quest("Water Goal", "Drink 8 glasses of water", "💧", 30, 15, "daily"),
            Quest("Sleep Master", "Sleep 8 hours tonight", "😴", 40, 18, "daily"),
            Quest("5K Steps", "Walk 5000 steps", "👣", 25, 10, "daily"),
            Quest("Weekly Warrior", "Complete all daily quests for 7 days", "🏆", 200, 100, "weekly")
        ))
        runOnUiThread {
            questAdapter.notifyDataSetChanged()
            findViewById<TextView>(R.id.tvQuestCount).text = "${quests.size} quests available"
        }
    }

    private fun acceptQuest(quest: Quest) {
        // Mark quest as accepted
        quest.accepted = true
        questAdapter.notifyDataSetChanged()
    }
}
