package com.fida.app.models

data class HealthProfile(
    val uid: String = "",
    val lastUpdated: Long = 0,
    val weight: Float = 0f,
    val height: Float = 0f,
    val age: Int = 0,
    val gender: String = "",
    val bmi: Float = 0f,
    val sleepHours: Int = 7,
    val stressLevel: Int = 5,
    val activityLevel: String = "moderate",
    val fitnessGoals: List<String> = emptyList(),
    val chronicConditions: List<String> = emptyList(),
    val shortnessOfBreath: Boolean = false,
    val lastCheckupDate: String = "",
    val medications: List<String> = emptyList(),
    val allergies: List<String> = emptyList(),
    val smokingStatus: String = "never",
    val alcoholFrequency: String = "rarely",
    val injuries: List<String> = emptyList(),
    val healthRiskFactors: List<String> = emptyList(),
    val aiSuggestions: String = "",
    val suggestionsCachedAt: Long = 0
)
