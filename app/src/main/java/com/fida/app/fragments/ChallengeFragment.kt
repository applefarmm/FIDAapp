package com.fida.app.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fida.app.R
import com.fida.app.adapters.ChallengeAdapter
import com.fida.app.models.Challenge
import com.fida.app.utils.FirestoreRepository
import com.fida.app.utils.PreferenceHelper

class ChallengeFragment : Fragment() {

    private lateinit var prefs: PreferenceHelper
    private lateinit var challengeAdapter: ChallengeAdapter
    private val challenges = mutableListOf<Challenge>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_challenges, container, false)
        prefs = PreferenceHelper(requireContext())
        setupViews(view)
        loadChallenges()
        return view
    }

    private fun setupViews(view: View) {
        val rvChallenges = view.findViewById<RecyclerView>(R.id.rvChallenges)
        challengeAdapter = ChallengeAdapter(
            challenges,
            onJoin = { challenge -> joinChallenge(challenge) },
            onClaim = { challenge -> claimChallengeReward(challenge) }
        )
        rvChallenges.layoutManager = LinearLayoutManager(context)
        rvChallenges.adapter = challengeAdapter
    }

    private fun loadChallenges() {
        val uid = prefs.getUid() ?: return
        FirestoreRepository.getUserChallenges(uid) { challengeDataList ->
            if (challengeDataList != null && challengeDataList.isNotEmpty()) {
                challenges.clear()
                val mappedChallenges = challengeDataList.map { data ->
                    Challenge(
                        id = data["id"] as? String ?: "",
                        title = data["title"] as? String ?: "",
                        description = data["description"] as? String ?: "",
                        icon = data["icon"] as? String ?: "🏆",
                        xpReward = (data["xpReward"] as? Long)?.toInt() ?: 0,
                        coinReward = (data["coinReward"] as? Long)?.toInt() ?: 0,
                        gemReward = (data["gemReward"] as? Long)?.toInt() ?: 0,
                        type = data["type"] as? String ?: "daily",
                        targetValue = (data["targetValue"] as? Long)?.toInt() ?: 1,
                        progress = (data["progress"] as? Long)?.toInt() ?: 0,
                        isActive = data["isActive"] as? Boolean ?: false,
                        isCompleted = data["isCompleted"] as? Boolean ?: false,
                        startDate = data["startDate"] as? Long ?: 0,
                        endDate = data["endDate"] as? Long ?: 0
                    )
                }
                challenges.addAll(mappedChallenges)
                activity?.runOnUiThread {
                    challengeAdapter.notifyDataSetChanged()
                    updateActiveCount()
                    updateEmptyState(challenges.isEmpty())
                }
            } else {
                loadDefaultChallenges()
            }
        }
    }

    private fun loadDefaultChallenges() {
        challenges.clear()
        challenges.addAll(listOf(
            Challenge(
                title = "7-Day Runner",
                description = "Run for 7 consecutive days",
                icon = "🏃",
                xpReward = 200,
                coinReward = 100,
                gemReward = 5,
                type = "weekly",
                targetValue = 7,
                progress = 3,
                isActive = true,
                isCompleted = false
            ),
            Challenge(
                title = "Water Warrior",
                description = "Drink 8 glasses of water for 5 days",
                icon = "💧",
                xpReward = 150,
                coinReward = 75,
                gemReward = 0,
                type = "weekly",
                targetValue = 5,
                progress = 2,
                isActive = true,
                isCompleted = false
            ),
            Challenge(
                title = "Speed Demon",
                description = "Run 5km in under 25 minutes",
                icon = "⚡",
                xpReward = 300,
                coinReward = 150,
                gemReward = 10,
                type = "special",
                targetValue = 1,
                progress = 0,
                isActive = false,
                isCompleted = false
            ),
            Challenge(
                title = "Sleep Master",
                description = "Sleep 8+ hours for 3 nights",
                icon = "😴",
                xpReward = 100,
                coinReward = 50,
                gemReward = 0,
                type = "daily",
                targetValue = 3,
                progress = 1,
                isActive = false,
                isCompleted = false
            )
        ))
        activity?.runOnUiThread {
            challengeAdapter.notifyDataSetChanged()
            updateActiveCount()
            updateEmptyState(false)
        }
    }

    private fun joinChallenge(challenge: Challenge) {
        val uid = prefs.getUid() ?: return
        val challengeId = challenge.id.ifEmpty { challenge.title.replace(" ", "_").lowercase() }

        FirestoreRepository.joinChallenge(uid, challengeId) { success ->
            if (success) {
                activity?.runOnUiThread {
                    val idx = challenges.indexOf(challenge)
                    if (idx >= 0) {
                        challenges[idx] = challenge.copy(isActive = true)
                        challengeAdapter.notifyItemChanged(idx)
                        updateActiveCount()
                    }
                }
            }
        }
    }

    private fun claimChallengeReward(challenge: Challenge) {
        val uid = prefs.getUid() ?: return
        val challengeId = challenge.id.ifEmpty { challenge.title.replace(" ", "_").lowercase() }

        val rewards = mapOf(
            "xp" to challenge.xpReward.toLong(),
            "coins" to challenge.coinReward.toLong(),
            "gems" to challenge.gemReward.toLong()
        )

        FirestoreRepository.claimChallengeReward(uid, challengeId, rewards) { success ->
            if (success) {
                activity?.runOnUiThread {
                    val idx = challenges.indexOf(challenge)
                    if (idx >= 0) {
                        challenges[idx] = challenge.copy(isActive = false)
                        challengeAdapter.notifyItemChanged(idx)
                        updateActiveCount()
                    }
                }
            }
        }
    }

    private fun updateActiveCount() {
        val tvCount = view?.findViewById<TextView>(R.id.tvActiveChallengesCount) ?: return
        val activeCount = challenges.count { it.isActive && !it.isCompleted }
        tvCount.text = "$activeCount active"
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        val tvNoChallenges = view?.findViewById<TextView>(R.id.tvNoChallenges) ?: return
        val rvChallenges = view?.findViewById<RecyclerView>(R.id.rvChallenges) ?: return
        if (isEmpty) {
            tvNoChallenges.visibility = View.VISIBLE
            rvChallenges.visibility = View.GONE
        } else {
            tvNoChallenges.visibility = View.GONE
            rvChallenges.visibility = View.VISIBLE
        }
    }
}