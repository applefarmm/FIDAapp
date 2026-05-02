package com.fida.app.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fida.app.R
import com.fida.app.adapters.FaqAdapter
import com.fida.app.models.FaqItem

class HelpFaqFragment : Fragment() {

    private lateinit var faqAdapter: FaqAdapter
    private val faqItems = mutableListOf<FaqItem>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_help_faq, container, false)
        setupViews(view)
        loadFaqData()
        return view
    }

    private fun setupViews(view: View) {
        val rvFaq = view.findViewById<RecyclerView>(R.id.rvFaq)
        faqAdapter = FaqAdapter(faqItems)
        rvFaq.layoutManager = LinearLayoutManager(context)
        rvFaq.adapter = faqAdapter
    }

    private fun loadFaqData() {
        // Placeholder data for FAQ items
        faqItems.add(FaqItem("How does XP work?", "XP is earned by completing activities and challenges. It contributes to your level."))
        faqItems.add(FaqItem("How do streaks work?", "Streaks are maintained by completing daily goals consecutively. Missing a day breaks the streak."))
        faqItems.add(FaqItem("What are shields?", "Shields protect your streak from being broken if you miss a day. You can earn or buy them."))
        faqItems.add(FaqItem("How do I contact support?", "You can contact support via email at support@fidaapp.com or through the 'Contact Us' section in settings."))
        faqItems.add(FaqItem("Where can I find the Privacy Policy?", "The Privacy Policy is available in the 'About' section of the app settings."))
        faqItems.add(FaqItem("Where can I find the Terms of Service?", "The Terms of Service are also available in the 'About' section of the app settings."))

        faqAdapter.notifyDataSetChanged()
    }
}
