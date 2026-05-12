package com.fida.app

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.fida.app.utils.PreferenceHelper
import com.google.android.material.button.MaterialButton

class OnboardingActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var btnNext: MaterialButton
    private lateinit var tvSkip: TextView
    private lateinit var dotsLayout: LinearLayout

    private val pages = listOf(
        OnboardingPage("🏃", "Track Your Activities", "Run, log water, and record sleep — all in one place built for your fitness journey."),
        OnboardingPage("⚡", "Earn XP & Level Up", "Every activity earns XP. Level up, unlock badges, and climb the leaderboard."),
        OnboardingPage("🔥", "Build Your Streak", "Stay consistent daily. Use Streak Shields to protect your progress on off days.")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)
        supportActionBar?.hide()

        viewPager = findViewById(R.id.onboardingViewPager)
        btnNext = findViewById(R.id.btnNext)
        tvSkip = findViewById(R.id.tvSkip)
        dotsLayout = findViewById(R.id.dotsLayout)

        viewPager.adapter = OnboardingAdapter(pages)
        setupDots(0)

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                setupDots(position)
                btnNext.text = if (position == pages.lastIndex) "Get Started" else "Next"
            }
        })

        btnNext.setOnClickListener {
            val current = viewPager.currentItem
            if (current < pages.lastIndex) {
                viewPager.currentItem = current + 1
            } else {
                finishOnboarding()
            }
        }

        tvSkip.setOnClickListener { finishOnboarding() }
    }

    private fun finishOnboarding() {
        PreferenceHelper(this).setOnboardingDone()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun setupDots(selected: Int) {
        dotsLayout.removeAllViews()
        pages.indices.forEach { i ->
            val dot = View(this).apply {
                val size = if (i == selected) 12 else 8
                val params = LinearLayout.LayoutParams(
                    (size * resources.displayMetrics.density).toInt(),
                    (size * resources.displayMetrics.density).toInt()
                ).also { it.setMargins(6, 0, 6, 0) }
                layoutParams = params
                background = ContextCompat.getDrawable(
                    this@OnboardingActivity,
                    if (i == selected) R.drawable.dot_active else R.drawable.dot_inactive
                )
            }
            dotsLayout.addView(dot)
        }
    }

    data class OnboardingPage(val emoji: String, val title: String, val desc: String)

    inner class OnboardingAdapter(private val items: List<OnboardingPage>) :
        RecyclerView.Adapter<OnboardingAdapter.VH>() {

        inner class VH(view: View) : RecyclerView.ViewHolder(view) {
            val emoji: TextView = view.findViewById(R.id.onboardingEmoji)
            val title: TextView = view.findViewById(R.id.onboardingTitle)
            val desc: TextView = view.findViewById(R.id.onboardingDesc)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            VH(LayoutInflater.from(parent.context).inflate(R.layout.item_onboarding, parent, false))

        override fun onBindViewHolder(holder: VH, position: Int) {
            holder.emoji.text = items[position].emoji
            holder.title.text = items[position].title
            holder.desc.text = items[position].desc
        }

        override fun getItemCount() = items.size
    }
}
