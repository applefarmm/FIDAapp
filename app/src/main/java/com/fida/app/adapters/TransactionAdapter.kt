package com.fida.app.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.fida.app.R
import com.fida.app.models.Transaction

class TransactionAdapter(private val transactions: List<Transaction>) :
    RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = transactions[position]
        holder.bind(transaction)
    }

    override fun getItemCount(): Int = transactions.size

    inner class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvDescription: TextView = itemView.findViewById(R.id.tvTransactionDescription)
        private val tvAmount: TextView = itemView.findViewById(R.id.tvTransactionAmount)
        private val tvTimestamp: TextView = itemView.findViewById(R.id.tvTransactionTimestamp)

        fun bind(transaction: Transaction) {
            tvDescription.text = transaction.description
            tvAmount.text = transaction.amount

            // Set color based on transaction type
            if (transaction.type == Transaction.Type.EARNED) {
                tvAmount.setTextColor(Color.parseColor("#4CAF50")) // Green for earned
            } else {
                tvAmount.setTextColor(Color.parseColor("#F44336")) // Red for spent
            }

            tvTimestamp.text = transaction.timestamp
        }
    }
}
