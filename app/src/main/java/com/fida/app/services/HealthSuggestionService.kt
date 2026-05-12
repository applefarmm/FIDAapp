package com.fida.app.services

import android.content.Context
import com.fida.app.models.HealthProfile
import com.fida.app.utils.PreferenceHelper
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class HealthSuggestionService(private val context: Context) {

    private val prefs = PreferenceHelper(context)
    private val client = OkHttpClient()
    private val gson = Gson()

    private fun getApiKey(): String {
        // Get API key from BuildConfig or AndroidManifest metadata
        return try {
            val ai = context.packageManager.getApplicationInfo(
                context.packageName,
                android.content.pm.PackageManager.GET_META_DATA
            )
            ai.metaData.getString("com.google.ai.generativeai.api_key") ?: ""
        } catch (e: Exception) {
            ""
        }
    }

    suspend fun generateSuggestions(profile: HealthProfile): Result<String> = withContext(Dispatchers.IO) {
        return@withContext try {
            // Check cache first
            val cached = getCachedSuggestions(profile.uid)
            if (cached != null && !isCacheExpired(profile.uid)) {
                return@withContext Result.success(cached)
            }

            val apiKey = getApiKey()
            if (apiKey.isEmpty()) {
                return@withContext Result.failure(Exception("API key not configured"))
            }

            // Build prompt
            val prompt = buildPrompt(profile)

            // Call Gemini API via REST
            val response = callGeminiAPI(apiKey, prompt)

            if (response.isSuccess) {
                val suggestions = response.getOrNull() ?: "Unable to generate suggestions at this time."
                cacheSuggestions(profile.uid, suggestions)
                Result.success(suggestions)
            } else {
                Result.failure(response.exceptionOrNull() ?: Exception("Unknown error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun callGeminiAPI(apiKey: String, prompt: String): Result<String> = withContext(Dispatchers.IO) {
        return@withContext try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=$apiKey"

            android.util.Log.d("HealthSuggestionService", "Calling Gemini API: $url")

            val requestBody = mapOf(
                "contents" to listOf(
                    mapOf(
                        "parts" to listOf(
                            mapOf("text" to prompt)
                        )
                    )
                )
            )

            val jsonBody = gson.toJson(requestBody)
            val body = jsonBody.toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url(url)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .build()

            val httpResponse = client.newCall(request).execute()

            android.util.Log.d("HealthSuggestionService", "API Response Code: ${httpResponse.code}")

            if (httpResponse.isSuccessful) {
                val responseBody = httpResponse.body?.string() ?: ""
                android.util.Log.d("HealthSuggestionService", "API Response: $responseBody")
                
                val responseJson = gson.fromJson(responseBody, Map::class.java)

                @Suppress("UNCHECKED_CAST")
                val candidates = responseJson["candidates"] as? List<Map<String, Any>>
                val firstCandidate = candidates?.firstOrNull()

                @Suppress("UNCHECKED_CAST")
                val content = firstCandidate?.get("content") as? Map<String, Any>

                @Suppress("UNCHECKED_CAST")
                val parts = content?.get("parts") as? List<Map<String, Any>>
                val firstPart = parts?.firstOrNull()
                val text = firstPart?.get("text") as? String

                if (text != null) {
                    Result.success(text)
                } else {
                    Result.failure(Exception("Invalid response format from API"))
                }
            } else {
                val errorBody = httpResponse.body?.string() ?: "No error details"
                android.util.Log.e("HealthSuggestionService", "API Error ${httpResponse.code}: $errorBody")
                Result.failure(Exception("API Error ${httpResponse.code}: $errorBody"))
            }
        } catch (e: Exception) {
            android.util.Log.e("HealthSuggestionService", "Exception calling Gemini API", e)
            Result.failure(e)
        }
    }

    private fun buildPrompt(profile: HealthProfile): String {
        return """
You are a health and wellness assistant for a fitness app called FIDA. 
Your role is to provide personalized wellness suggestions based on user health data.

IMPORTANT DISCLAIMER: You are NOT a doctor. Always include disclaimers that your suggestions 
are general wellness tips and NOT medical advice. Users should consult healthcare 
professionals for medical concerns.

Provide suggestions in a friendly, encouraging tone.
Focus on actionable, safe recommendations.

Based on this health profile, provide 3-5 personalized wellness suggestions:

- Age: ${profile.age}
- Weight: ${profile.weight}kg, Height: ${profile.height}cm, BMI: ${String.format("%.1f", profile.bmi)}
- Activity Level: ${profile.activityLevel}
- Sleep: ${profile.sleepHours} hours/night
- Stress Level: ${profile.stressLevel}/10
- Shortness of Breath: ${profile.shortnessOfBreath}
- Fitness Goals: ${profile.fitnessGoals.joinToString(", ")}
- Chronic Conditions: ${profile.chronicConditions.joinToString(", ")}
- Last Checkup: ${profile.lastCheckupDate}
- Smoking Status: ${profile.smokingStatus}
- Alcohol Frequency: ${profile.alcoholFrequency}

Provide suggestions for:
1. Exercise recommendations
2. Nutrition tips
3. Sleep improvement
4. Stress management
5. When to see a doctor

Keep each suggestion concise (1-2 sentences).
Start with a disclaimer that this is not medical advice.
        """.trimIndent()
    }

    private fun cacheSuggestions(uid: String, suggestions: String) {
        prefs.saveString("health_suggestions_$uid", suggestions)
        prefs.saveLong("health_suggestions_cache_time_$uid", System.currentTimeMillis())
    }

    private fun getCachedSuggestions(uid: String): String? {
        return prefs.getString("health_suggestions_$uid")
    }

    private fun isCacheExpired(uid: String): Boolean {
        val cacheTime = prefs.getLong("health_suggestions_cache_time_$uid") ?: 0
        val currentTime = System.currentTimeMillis()
        val sevenDaysMs = 7 * 24 * 60 * 60 * 1000L
        return (currentTime - cacheTime) > sevenDaysMs
    }

    fun clearCache(uid: String) {
        prefs.remove("health_suggestions_$uid")
        prefs.remove("health_suggestions_cache_time_$uid")
    }
}
