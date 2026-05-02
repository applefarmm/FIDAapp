package com.fida.app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.fida.app.R
import com.fida.app.models.FaqItem

class FaqAdapter(private val faqItems: List<FaqItem>) :
    RecyclerView.Adapter<FaqAdapter.FaqViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FaqViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_faq, parent, false)
        return FaqViewHolder(view)
    }

    override fun onBindViewHolder(holder: FaqViewHolder, position: Int) {
        val faqItem = faqItems[position]
        holder.bind(faqItem)

        holder.itemView.setOnClickListener {
            faqItem.isExpanded = !faqItem.isExpanded
            notifyItemChanged(position)
        }
    }

    override fun getItemCount(): Int = faqItems.size

    inner class FaqViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvQuestion: TextView = itemView.findViewById(R.id.tvFaqQuestion)
        private val tvAnswer: TextView = itemView.findViewById(R.id.tvFaqAnswer)
        private val faqLayout: ConstraintLayout = itemView.findViewById(R.id.faqItemLayout)

        fun bind(faqItem: FaqItem) {
            tvQuestion.text = faqItem.question
            if (faqItem.isExpanded) {
                tvAnswer.visibility = View.VISIBLE
                tvAnswer.text = faqItem.answer
            } else {
                tvAnswer.visibility = View.GONE
            }
        }
    }
}
