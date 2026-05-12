package com.fida.app.utils

import android.content.Context
import com.fida.app.models.User
import com.google.gson.Gson

class PreferenceHelper(context: Context) {

    private val prefs = context.getSharedPreferences("fida_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun saveUser(user: User) {
        prefs.edit().putString("user", gson.toJson(user)).apply()
    }

    fun getUser(): User? {
        val json = prefs.getString("user", null) ?: return null
        return try { gson.fromJson(json, User::class.java) } catch (e: Exception) { null }
    }

    fun setLoggedIn(value: Boolean) = prefs.edit().putBoolean("logged_in", value).apply()
    fun isLoggedIn(): Boolean = prefs.getBoolean("logged_in", false)

    fun setUid(uid: String) = prefs.edit().putString("uid", uid).apply()
    fun getUid(): String? = prefs.getString("uid", null)

    fun setUsername(username: String) = prefs.edit().putString("username", username).apply()
    fun getUsername(): String? = prefs.getString("username", null)
    fun saveUsername(username: String) = setUsername(username)

    fun setEmail(email: String) = prefs.edit().putString("email", email).apply()
    fun getEmail(): String? = prefs.getString("email", null)

    fun setProfileSetupComplete() = prefs.edit().putBoolean("profile_setup_complete", true).apply()
    fun isProfileSetupComplete(): Boolean = prefs.getBoolean("profile_setup_complete", false)

    fun setOnboardingDone() = prefs.edit().putBoolean("onboarding_done", true).apply()
    fun isOnboardingDone(): Boolean = prefs.getBoolean("onboarding_done", false)

    fun saveInt(key: String, value: Int) = prefs.edit().putInt(key, value).apply()
    // Corrected: Check for key existence before getting value to avoid returning default 0 if key is not present
    fun getInt(key: String): Int? {
        return if (prefs.contains(key)) {
            prefs.getInt(key, 0) // Default to 0 if key exists but value is somehow 0
        } else {
            null // Return null if key does not exist
        }
    }

    fun saveString(key: String, value: String) = prefs.edit().putString(key, value).apply()
    fun getString(key: String): String? = prefs.getString(key, null)

    fun saveBoolean(key: String, value: Boolean) = prefs.edit().putBoolean(key, value).apply()
    fun getBoolean(key: String): Boolean? = if (prefs.contains(key)) prefs.getBoolean(key, false) else null

    // Step tracking baseline methods
    fun saveBaselineSteps(steps: Long) = prefs.edit().putLong("baseline_steps", steps).apply()
    fun getBaselineSteps(): Long = prefs.getLong("baseline_steps", 0L)

    fun saveBaselineDate(date: String) = prefs.edit().putString("baseline_date", date).apply()
    fun getBaselineDate(): String? = prefs.getString("baseline_date", null)

    fun saveMaxSteps(maxSteps: Int) = prefs.edit().putInt("max_steps", maxSteps).apply()
    fun getMaxSteps(): Int = prefs.getInt("max_steps", 2500)

    // Health profile methods
    fun saveHealthProfile(uid: String, profile: String) = prefs.edit().putString("health_profile_$uid", profile).apply()
    fun getHealthProfile(uid: String): String? = prefs.getString("health_profile_$uid", null)

    fun saveHealthProfileUpdateTime(uid: String) = prefs.edit().putLong("health_profile_time_$uid", System.currentTimeMillis()).apply()
    fun getHealthProfileUpdateTime(uid: String): Long = prefs.getLong("health_profile_time_$uid", 0L)

    fun saveLong(key: String, value: Long) = prefs.edit().putLong(key, value).apply()
    fun getLong(key: String): Long? = if (prefs.contains(key)) prefs.getLong(key, 0L) else null

    fun remove(key: String) = prefs.edit().remove(key).apply()

    fun clear() = prefs.edit().clear().apply()
}
