package com.fida.app.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.fida.app.R
import com.fida.app.models.HealthProfile
import com.fida.app.services.HealthSuggestionService
import com.fida.app.utils.FirestoreRepository
import com.fida.app.utils.PreferenceHelper
import com.google.gson.Gson
import kotlinx.coroutines.launch

class HealthTipsFragment : Fragment() {

    private lateinit var prefs: PreferenceHelper
    private lateinit var healthService: HealthSuggestionService
    private val uid: String by lazy {
        prefs.getUid() ?: ""
    }

    private lateinit var tvTitle: TextView
    private lateinit var tvDisclaimer: TextView
    private lateinit var tvSuggestions: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var scrollView: ScrollView
    private lateinit var btnRefresh: Button
    private lateinit var btnUpdateProfile: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_health_tips, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prefs = PreferenceHelper(requireContext())
        healthService = HealthSuggestionService(requireContext())

        initializeViews(view)
        loadHealthTips()
    }

    private fun initializeViews(view: View) {
        tvTitle = view.findViewById(R.id.tvTitle)
        tvDisclaimer = view.findViewById(R.id.tvDisclaimer)
        tvSuggestions = view.findViewById(R.id.tvSuggestions)
        progressBar = view.findViewById(R.id.progressBar)
        scrollView = view.findViewById(R.id.scrollView)
        btnRefresh = view.findViewById(R.id.btnRefresh)
        btnUpdateProfile = view.findViewById(R.id.btnUpdateProfile)

        btnRefresh.setOnClickListener { refreshSuggestions() }
        btnUpdateProfile.setOnClickListener { updateProfile() }

        // Set disclaimer text
        tvDisclaimer.text = "⚠️ DISCLAIMER: These are general wellness suggestions and NOT medical advice. " +
                "Please consult a healthcare professional for medical concerns."
    }

    private fun loadHealthTips() {
        if (uid.isEmpty()) {
            tvSuggestions.text = "Please log in to view health tips."
            return
        }

        // Load health profile from Firestore
        FirestoreRepository.getHealthProfile(uid) { profile ->
            if (profile != null) {
                generateSuggestions(profile)
            } else {
                tvSuggestions.text = "No health profile found. Please complete the health questionnaire first."
                btnUpdateProfile.visibility = View.VISIBLE
            }
        }
    }

    private fun generateSuggestions(profile: HealthProfile) {
        progressBar.visibility = View.VISIBLE
        scrollView.visibility = View.GONE

        viewLifecycleOwner.lifecycleScope.launch {
            val result = healthService.generateSuggestions(profile)

            progressBar.visibility = View.GONE
            scrollView.visibility = View.VISIBLE

            result.onSuccess { suggestions ->
                tvSuggestions.text = suggestions
                // Save to profile
                val updatedProfile = profile.copy(
                    aiSuggestions = suggestions,
                    suggestionsCachedAt = System.currentTimeMillis()
                )
                saveProfileWithSuggestions(updatedProfile)
            }

            result.onFailure { error ->
                tvSuggestions.text = "Error generating suggestions: ${error.message}\n\n" +
                        "Please ensure:\n" +
                        "1. API key is configured in AndroidManifest.xml\n" +
                        "2. Internet connection is available\n" +
                        "3. Try again later"
                Toast.makeText(
                    requireContext(),
                    "Failed to generate suggestions",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun saveProfileWithSuggestions(profile: HealthProfile) {
        FirestoreRepository.updateUser(uid, mapOf(
            "aiSuggestions" to profile.aiSuggestions,
            "suggestionsCachedAt" to profile.suggestionsCachedAt
        )) { success ->
            if (!success) {
                Toast.makeText(
                    requireContext(),
                    "Failed to save suggestions",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun refreshSuggestions() {
        if (uid.isEmpty()) {
            Toast.makeText(requireContext(), "Please log in first", Toast.LENGTH_SHORT).show()
            return
        }

        // Clear cache
        healthService.clearCache(uid)

        // Reload
        loadHealthTips()
    }

    private fun updateProfile() {
        // Navigate to HealthQuestionnaireActivity
        val intent = android.content.Intent(requireContext(), com.fida.app.HealthQuestionnaireActivity::class.java)
        intent.putExtra("uid", uid)
        startActivity(intent)
    }
}
