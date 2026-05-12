package com.fida.app.models

data class User(
    val uid: String = "",
    val username: String = "",
    val name: String = "",
    val email: String = "",
    val age: Int = 0,
    val gender: String = "",
    val height: Float = 0f,
    val weight: Float = 0f,
    val fitnessGoal: String = "",
    val profilePicture: String = "",
    val avatar: String = "avatar_1",
    // Gamification
    val xp: Int = 0,
    val maxXp: Int = 100,
    val level: Int = 1,
    val coins: Int = 0,
    val gems: Int = 0,
    val streakDays: Int = 0,
    val streakShields: Int = 0,
    val lastActiveDate: String = "",
    // Legacy fields kept for compatibility
    val exp: Int = 0,
    val maxExp: Int = 100,
    val coin: Int = 0,
    val health: Int = 100,
    val maxStep: Int = 2500,
    val maxWater: Int = 8,
    val profileSetupDone: Boolean = false
)
