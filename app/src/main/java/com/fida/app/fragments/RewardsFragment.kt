package com.fida.app.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fida.app.R
import com.fida.app.adapters.RewardAdapter
import com.fida.app.models.Reward
import com.fida.app.utils.FirestoreRepository
import com.fida.app.utils.PreferenceHelper

class RewardsFragment : Fragment() {

    private lateinit var rewardAdapter: RewardAdapter
    private val rewards = mutableListOf<Reward>()
    private lateinit var prefs: PreferenceHelper
    private var userCoins = 0
    private var userGems = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?):
            View? {
        val view = inflater.inflate(R.layout.fragment_rewards, container, false)
        prefs = PreferenceHelper(requireContext())
        setupViews(view)
        loadData()
        return view
    }

    private fun setupViews(view: View) {
        val rvRewards = view.findViewById<RecyclerView>(R.id.rvRewards)

        rewardAdapter = RewardAdapter(rewards) { reward ->
            purchaseReward(reward)
        }
        rvRewards.layoutManager = LinearLayoutManager(context)
        rvRewards.adapter = rewardAdapter
    }

    private fun loadData() {
        val uid = prefs.getUid() ?: return

        // Load user balance from Firestore
        FirestoreRepository.getUser(uid) { userData ->
            if (userData == null || !isAdded) return@getUser
            activity?.runOnUiThread {
                userCoins = (userData["coins"] as? Long)?.toInt() ?: 0
                userGems = (userData["gems"] as? Long)?.toInt() ?: 0
                updateBalanceDisplay()
            }
        }

        // Check and seed rewards if empty, then load
        FirestoreRepository.checkAndSeedRewards { seeded ->
            if (!isAdded) return@checkAndSeedRewards
            FirestoreRepository.getRewards { rewardList ->
                if (rewardList == null || !isAdded) return@getRewards
                activity?.runOnUiThread {
                    rewards.clear()
                    rewards.addAll(rewardList.map { Reward.fromMap(it) })
                    rewardAdapter.notifyDataSetChanged()
                }
            }
        }
    }

    private fun purchaseReward(reward: Reward) {
        val uid = prefs.getUid() ?: return

        when (reward.type) {
            Reward.Type.COIN -> {
                if (userCoins >= reward.cost) {
                    FirestoreRepository.purchaseReward(uid, reward.id, reward.cost, "coin") { success ->
                        if (success && isAdded) {
                            activity?.runOnUiThread {
                                userCoins -= reward.cost
                                updateBalanceDisplay()
                                Toast.makeText(context, "Purchased ${reward.name}!", Toast.LENGTH_SHORT).show()
                            }
                        } else if (isAdded) {
                            activity?.runOnUiThread {
                                Toast.makeText(context, "Purchase failed", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } else {
                    Toast.makeText(context, "Not enough coins! Need ${reward.cost}", Toast.LENGTH_SHORT).show()
                }
            }
            Reward.Type.GEM -> {
                if (userGems >= reward.cost) {
                    FirestoreRepository.purchaseReward(uid, reward.id, reward.cost, "gem") { success ->
                        if (success && isAdded) {
                            activity?.runOnUiThread {
                                userGems -= reward.cost
                                updateBalanceDisplay()
                                Toast.makeText(context, "Purchased ${reward.name}!", Toast.LENGTH_SHORT).show()
                            }
                        } else if (isAdded) {
                            activity?.runOnUiThread {
                                Toast.makeText(context, "Purchase failed", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } else {
                    Toast.makeText(context, "Not enough gems! Need ${reward.cost}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateBalanceDisplay() {
        val tvBalance = view?.findViewById<TextView>(R.id.tvRewardBalance) ?: return
        tvBalance.text = "Balance: $userCoins Coins, $userGems Gems"
    }
}
