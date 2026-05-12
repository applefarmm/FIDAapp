package com.fida.app.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.fida.app.R
import com.fida.app.SettingsActivity
import com.fida.app.ShopActivity
import com.fida.app.adapters.AchievementsAdapter
import com.fida.app.models.Achievement
import com.fida.app.utils.FirestoreRepository
import com.fida.app.utils.GameManager
import com.fida.app.utils.PreferenceHelper

class ProfileFragment : Fragment() {

    private lateinit var achievementsAdapter: AchievementsAdapter
    private val earnedBadges = mutableListOf<Achievement>()
    private lateinit var prefs: PreferenceHelper

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?):
            View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        prefs = PreferenceHelper(requireContext())
        setupViews(view)
        loadUserData()
        return view
    }

    private fun setupViews(view: View) {
        val tvSettingsShortcut = view.findViewById<TextView>(R.id.tvSettingsShortcut)
        val tvShopShortcut = view.findViewById<TextView>(R.id.tvShopShortcut)
        val rvBadges = view.findViewById<RecyclerView>(R.id.rvBadges)

        tvSettingsShortcut.setOnClickListener {
            startActivity(Intent(requireContext(), SettingsActivity::class.java))
        }

        tvShopShortcut.setOnClickListener {
            startActivity(Intent(requireContext(), ShopActivity::class.java))
        }

        achievementsAdapter = AchievementsAdapter(earnedBadges)
        rvBadges.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        rvBadges.adapter = achievementsAdapter
    }

    private fun loadUserData() {
        val uid = prefs.getUid()
        if (uid == null) {
            // Show placeholder data if no user
            setUsernamePlaceholder()
            loadPlaceholderBadges()
            return
        }

        FirestoreRepository.getUser(uid) { data ->
            if (data == null || !isAdded) {
                activity?.runOnUiThread {
                    setUsernamePlaceholder()
                    loadPlaceholderBadges()
                }
            } else {
                activity?.runOnUiThread {
                    val tvUsername = view?.findViewById<TextView>(R.id.tvProfileUsername)
                    val tvLevel = view?.findViewById<TextView>(R.id.tvProfileLevel)
                    val tvXP = view?.findViewById<TextView>(R.id.tvProfileXP)
                    val ivProfilePic = view?.findViewById<ImageView>(R.id.ivProfilePic)
                    val tvTotalRuns = view?.findViewById<TextView>(R.id.tvTotalRuns)
                    val tvTotalDistance = view?.findViewById<TextView>(R.id.tvTotalDistance)

                    val name = data["name"]?.toString() ?: data["username"]?.toString() ?: "Athlete"
                    tvUsername?.text = name
                    prefs.saveUsername(name)

                    val level = (data["level"] as? Long)?.toInt() ?: 1
                    tvLevel?.text = "Level $level"

                    val xp = (data["xp"] as? Long)?.toInt() ?: 0
                    tvXP?.text = "XP: $xp"

                    // Fixed: model field is "profilePicture", not "profilePicUrl"
                    val profilePicUrl = data["profilePicture"]?.toString()
                    if (!profilePicUrl.isNullOrEmpty() && ivProfilePic != null) {
                        Glide.with(this).load(profilePicUrl).into(ivProfilePic)
                    } else {
                        ivProfilePic?.setImageResource(R.drawable.ic_profile_placeholder)
                    }

                    // Load activity stats
                    val totalRuns = (data["totalRuns"] as? Long)?.toInt() ?: 0
                    tvTotalRuns?.text = totalRuns.toString()

                    val totalDistance = (data["totalDistance"] as? Long)?.toInt() ?: 0
                    tvTotalDistance?.text = "$totalDistance km"

                    // Load badges from user data
                    loadBadgesFromData(data)
                }
            }
        }
    }

    private fun setUsernamePlaceholder() {
        activity?.runOnUiThread {
            val tvUsername = view?.findViewById<TextView>(R.id.tvProfileUsername)
            tvUsername?.text = prefs.getUsername() ?: "Athlete"
        }
    }

    private fun loadPlaceholderBadges() {
        earnedBadges.clear()
        earnedBadges.add(Achievement("First Run", "Complete your first run", "", true))
        earnedBadges.add(Achievement("5k Runner", "Run 5 kilometers", "", false))
        achievementsAdapter.notifyDataSetChanged()
    }

    private fun loadBadgesFromData(data: Map<String, Any>?) {
        earnedBadges.clear()
        val badges = data?.get("badges") as? Map<String, Any>
        if (badges != null) {
            // Build a map of badge ID → Badge for lookup
            val badgeMap = GameManager.allBadges.associateBy { it.id }
            for ((badgeId, unlocked) in badges) {
                val isUnlocked = (unlocked as? Boolean) ?: false
                val badgeDef = badgeMap[badgeId]
                val title = badgeDef?.title ?: badgeId.replace("_", " ").replaceFirstChar { it.uppercase() }
                val description = badgeDef?.description ?: "Badge: $badgeId"
                earnedBadges.add(Achievement(title, description, badgeDef?.emoji ?: "🏅", isUnlocked))
            }
        }
        if (earnedBadges.isEmpty()) {
            loadPlaceholderBadges()
        } else {
            achievementsAdapter.notifyDataSetChanged()
        }
    }
}
