package com.fida.app.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.fida.app.R
import com.fida.app.utils.FirestoreRepository
import com.fida.app.utils.PreferenceHelper

class LeaderboardFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.fragment_leaderboard, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val prefs = PreferenceHelper(requireContext())
        val uid = prefs.getUid() ?: return

        loadLeaderboard(view, uid)
    }

    private fun loadLeaderboard(view: View, currentUid: String) {
        FirestoreRepository.getLeaderboard { users ->
            if (!isAdded) return@getLeaderboard
            activity?.runOnUiThread {
                if (users.isEmpty()) {
                    view.findViewById<TextView>(R.id.tvYourRank).text = "#1"
                    return@runOnUiThread
                }

                // Find current user rank
                val userRank = users.indexOfFirst { it["uid"] == currentUid } + 1
                val userXp = users.find { it["uid"] == currentUid }?.get("xp") as? Long ?: 0L
                view.findViewById<TextView>(R.id.tvYourRank).text = "#${userRank}"
                view.findViewById<TextView>(R.id.tvYourXp).text = "$userXp XP"

                // Update top 3
                if (users.isNotEmpty()) {
                    val rank1 = users[0]
                    view.findViewById<TextView>(R.id.tvRank1Name).text =
                        rank1["name"]?.toString() ?: rank1["username"]?.toString() ?: "Athlete 1"
                    view.findViewById<TextView>(R.id.tvRank1Level).text =
                        "Level ${(rank1["level"] as? Long)?.toInt() ?: 1}"
                    view.findViewById<TextView>(R.id.tvRank1Xp).text =
                        "${(rank1["xp"] as? Long)?.toInt() ?: 0} XP"
                }

                if (users.size >= 2) {
                    val rank2 = users[1]
                    view.findViewById<TextView>(R.id.tvRank2Name).text =
                        rank2["name"]?.toString() ?: rank2["username"]?.toString() ?: "Athlete 2"
                    view.findViewById<TextView>(R.id.tvRank2Level).text =
                        "Level ${(rank2["level"] as? Long)?.toInt() ?: 1}"
                    view.findViewById<TextView>(R.id.tvRank2Xp).text =
                        "${(rank2["xp"] as? Long)?.toInt() ?: 0} XP"
                }

                if (users.size >= 3) {
                    val rank3 = users[2]
                    view.findViewById<TextView>(R.id.tvRank3Name).text =
                        rank3["name"]?.toString() ?: rank3["username"]?.toString() ?: "Athlete 3"
                    view.findViewById<TextView>(R.id.tvRank3Level).text =
                        "Level ${(rank3["level"] as? Long)?.toInt() ?: 1}"
                    view.findViewById<TextView>(R.id.tvRank3Xp).text =
                        "${(rank3["xp"] as? Long)?.toInt() ?: 0} XP"
                }
            }
        }
    }
}
