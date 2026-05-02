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
import com.fida.app.adapters.TransactionAdapter
import com.fida.app.models.Transaction
import com.fida.app.utils.PreferenceHelper

class WalletFragment : Fragment() {

    private lateinit var transactionAdapter: TransactionAdapter
    private val transactions = mutableListOf<Transaction>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?):
            View? {
        val view = inflater.inflate(R.layout.fragment_wallet, container, false)
        setupViews(view)
        loadWalletData()
        return view
    }

    private fun setupViews(view: View) {
        val tvCurrentBalance = view.findViewById<TextView>(R.id.tvCurrentBalance)
        val rvTransactionHistory = view.findViewById<RecyclerView>(R.id.rvTransactionHistory)

        // TODO: Fetch and display current balance from preferences or Firestore
        val prefs = PreferenceHelper(requireContext())
        val coins = prefs.getInt("coins") ?: 0
        val gems = prefs.getInt("gems") ?: 0
        tvCurrentBalance.text = "Coins: $coins | Gems: $gems"

        transactionAdapter = TransactionAdapter(transactions)
        rvTransactionHistory.layoutManager = LinearLayoutManager(context)
        rvTransactionHistory.adapter = transactionAdapter
    }

    private fun loadWalletData() {
        // TODO: Fetch transaction history from Firestore or local storage
        // Placeholder data:
        transactions.add(Transaction("Run Reward", "+50 Coins", "2023-10-27 10:00 AM", Transaction.Type.EARNED))
        transactions.add(Transaction("Daily Login Bonus", "+10 Gems", "2023-10-26 09:00 AM", Transaction.Type.EARNED))
        transactions.add(Transaction("Bought Power-up", "-20 Coins", "2023-10-25 08:00 AM", Transaction.Type.SPENT))

        transactionAdapter.notifyDataSetChanged()
    }
}
