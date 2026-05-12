package com.fida.app.utils

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object FirestoreRepository {

    private val db = FirebaseFirestore.getInstance()
    private val fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    fun getUser(uid: String, onResult: (Map<String, Any>?) -> Unit) {
        db.collection("users").document(uid).get()
            .addOnSuccessListener { onResult(it.data) }
            .addOnFailureListener { onResult(null) }
    }

    fun createUser(uid: String, data: Map<String, Any>, onDone: (Boolean) -> Unit) {
        db.collection("users").document(uid).set(data)
            .addOnSuccessListener { onDone(true) }
            .addOnFailureListener { onDone(false) }
    }

    fun updateUser(uid: String, data: Map<String, Any>, onDone: ((Boolean) -> Unit)? = null) {
        db.collection("users").document(uid).update(data)
            .addOnSuccessListener { onDone?.invoke(true) }
            .addOnFailureListener { onDone?.invoke(false) }
    }

    fun seedRewards(onDone: (Boolean) -> Unit) {
        val rewards = listOf(
            mapOf(
                "id" to "stamina_boost",
                "name" to "Stamina Boost",
                "description" to "+10% stamina for 1 day",
                "imageUrl" to "ic_stamina_boost",
                "cost" to 100,
                "type" to "coin",
                "active" to true
            ),
            mapOf(
                "id" to "streak_shield",
                "name" to "Streak Shield",
                "description" to "Protect your streak for 1 day",
                "imageUrl" to "ic_shield",
                "cost" to 1,
                "type" to "gem",
                "active" to true
            ),
            mapOf(
                "id" to "xp_doubler",
                "name" to "XP Doubler",
                "description" to "Double XP for 24 hours",
                "imageUrl" to "ic_xp",
                "cost" to 50,
                "type" to "coin",
                "active" to true
            ),
            mapOf(
                "id" to "energy_refill",
                "name" to "Energy Refill",
                "description" to "Instant full energy recovery",
                "imageUrl" to "ic_energy",
                "cost" to 200,
                "type" to "coin",
                "active" to true
            ),
            mapOf(
                "id" to "premium_avatar",
                "name" to "Premium Avatar Pack",
                "description" to "Unlock 6 exclusive avatars",
                "imageUrl" to "ic_profile_placeholder",
                "cost" to 5,
                "type" to "gem",
                "active" to true
            ),
            mapOf(
                "id" to "coin_boost",
                "name" to "Coin Boost",
                "description" to "+20% coins from activities for 3 days",
                "imageUrl" to "ic_coin",
                "cost" to 150,
                "type" to "coin",
                "active" to true
            )
        )

        val batch = db.batch()
        rewards.forEach { reward ->
            val ref = db.collection("rewards").document(reward["id"] as String)
            batch.set(ref, reward)
        }

        batch.commit()
            .addOnSuccessListener { onDone(true) }
            .addOnFailureListener { onDone(false) }
    }

    fun checkAndSeedRewards(onDone: (Boolean) -> Unit) {
        db.collection("rewards").limit(1).get()
            .addOnSuccessListener { snap ->
                if (snap.isEmpty) {
                    seedRewards(onDone)
                } else {
                    onDone(true) // Already seeded
                }
            }
            .addOnFailureListener { onDone(false) }
    }

    fun logDailyActivity(uid: String, field: String, value: Any) {
        val today = LocalDate.now().format(fmt)
        db.collection("users").document(uid)
            .update("allDays.$today.$field", value)
    }

    fun getLeaderboard(onResult: (List<Map<String, Any>>) -> Unit) {
        db.collection("users")
            .orderBy("level", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .orderBy("xp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(50)
            .get()
            .addOnSuccessListener { snap ->
                onResult(snap.documents.mapNotNull { it.data })
            }
            .addOnFailureListener { onResult(emptyList()) }
    }

    fun logRun(uid: String, runData: Map<String, Any>, onDone: (Boolean) -> Unit) {
        db.collection("users").document(uid)
            .collection("runs")
            .add(runData)
            .addOnSuccessListener { onDone(true) }
            .addOnFailureListener { onDone(false) }
    }

    fun getRuns(uid: String, onResult: (List<Map<String, Any>>) -> Unit) {
        db.collection("users").document(uid)
            .collection("runs")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(30)
            .get()
            .addOnSuccessListener { snap ->
                onResult(snap.documents.mapNotNull { it.data })
            }
            .addOnFailureListener { onResult(emptyList()) }
    }

    fun updateUserFields(uid: String, fields: Map<String, Any>, onDone: (Boolean) -> Unit) {
        db.collection("users").document(uid)
            .update(fields)
            .addOnSuccessListener { onDone(true) }
            .addOnFailureListener { onDone(false) }
    }

    fun incrementUserField(uid: String, field: String, amount: Long, onDone: (Boolean) -> Unit) {
        db.collection("users").document(uid)
            .update(field, com.google.firebase.firestore.FieldValue.increment(amount))
            .addOnSuccessListener { onDone(true) }
            .addOnFailureListener { onDone(false) }
    }

    fun getUserQuests(uid: String, onResult: (List<Map<String, Any>>?) -> Unit) {
        db.collection("users").document(uid)
            .collection("quests")
            .whereEqualTo("active", true)
            .get()
            .addOnSuccessListener { snap ->
                onResult(snap.documents.mapNotNull { it.data })
            }
            .addOnFailureListener { onResult(null) }
    }

    fun acceptQuest(uid: String, questId: String, onDone: (Boolean) -> Unit) {
        db.collection("users").document(uid)
            .collection("quests")
            .document(questId)
            .update("accepted", true)
            .addOnSuccessListener { onDone(true) }
            .addOnFailureListener { onDone(false) }
    }

    fun getUserChallenges(uid: String, onResult: (List<Map<String, Any>>?) -> Unit) {
        db.collection("users").document(uid)
            .collection("challenges")
            .get()
            .addOnSuccessListener { snap ->
                onResult(snap.documents.mapNotNull { it.data })
            }
            .addOnFailureListener { onResult(null) }
    }

    fun joinChallenge(uid: String, challengeId: String, onDone: (Boolean) -> Unit) {
        db.collection("users").document(uid)
            .collection("challenges")
            .document(challengeId)
            .update("isActive", true)
            .addOnSuccessListener { onDone(true) }
            .addOnFailureListener { onDone(false) }
    }

    fun claimChallengeReward(uid: String, challengeId: String, rewards: Map<String, Any>, onDone: (Boolean) -> Unit) {
        val challengeRef = db.collection("users").document(uid)
            .collection("challenges").document(challengeId)

        db.runBatch { batch ->
            batch.update(challengeRef, "isActive", false)
            batch.update(db.collection("users").document(uid),
                "xp", FieldValue.increment(rewards["xp"] as? Long ?: 0L),
                "coins", FieldValue.increment(rewards["coins"] as? Long ?: 0L),
                "gems", FieldValue.increment(rewards["gems"] as? Long ?: 0L)
            )
        }.addOnSuccessListener { onDone(true) }
            .addOnFailureListener { onDone(false) }
    }

    fun getUserAchievements(uid: String, onResult: (Map<String, Any>?) -> Unit) {
        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                val badges = doc?.get("badges") as? Map<String, Any>
                onResult(badges)
            }
            .addOnFailureListener { onResult(null) }
    }

    fun getGlobalAchievements(onResult: (List<Map<String, Any>>?) -> Unit) {
        db.collection("achievements")
            .get()
            .addOnSuccessListener { snap ->
                onResult(snap.documents.mapNotNull { it.data })
            }
            .addOnFailureListener { onResult(null) }
    }

    fun getRewards(onResult: (List<Map<String, Any>>?) -> Unit) {
        db.collection("rewards")
            .whereEqualTo("active", true)
            .get()
            .addOnSuccessListener { snap ->
                onResult(snap.documents.mapNotNull { it.data })
            }
            .addOnFailureListener { onResult(null) }
    }

    fun purchaseReward(uid: String, rewardId: String, cost: Int, currencyType: String, onDone: (Boolean) -> Unit) {
        val field = if (currencyType == "coin") "coins" else "gems"
        db.collection("users").document(uid)
            .update(field, FieldValue.increment(-cost.toLong()))
            .addOnSuccessListener { onDone(true) }
            .addOnFailureListener { onDone(false) }
    }

    fun defaultUserDoc(uid: String, email: String, displayName: String): Map<String, Any> = mapOf(
        "uid" to uid,
        "email" to email,
        "username" to displayName,
        "name" to displayName,
        "health" to 100,
        "xp" to 0,
        "maxXp" to 100,
        "exp" to 0,
        "maxExp" to 100,
        "level" to 1,
        "coins" to 0,
        "coin" to 0,
        "gems" to 0,
        "streakDays" to 0,
        "streakShields" to 3,
        "lastActiveDate" to "",
        "maxStep" to 2500,
        "maxWater" to 8,
        "profileSetupDone" to false,
        "badges" to emptyMap<String, Any>(),
        "allDays" to emptyMap<String, Any>()
    )

    fun saveHealthProfile(uid: String, profile: com.fida.app.models.HealthProfile, onDone: (Boolean) -> Unit) {
        if (uid.isEmpty()) {
            android.util.Log.e("FirestoreRepository", "saveHealthProfile: UID is empty")
            onDone(false)
            return
        }

        val profileMap = mapOf(
            "uid" to profile.uid,
            "lastUpdated" to profile.lastUpdated,
            "weight" to profile.weight,
            "height" to profile.height,
            "age" to profile.age,
            "gender" to profile.gender,
            "bmi" to profile.bmi,
            "sleepHours" to profile.sleepHours,
            "stressLevel" to profile.stressLevel,
            "activityLevel" to profile.activityLevel,
            "fitnessGoals" to profile.fitnessGoals,
            "chronicConditions" to profile.chronicConditions,
            "shortnessOfBreath" to profile.shortnessOfBreath,
            "lastCheckupDate" to profile.lastCheckupDate,
            "medications" to profile.medications,
            "allergies" to profile.allergies,
            "smokingStatus" to profile.smokingStatus,
            "alcoholFrequency" to profile.alcoholFrequency,
            "injuries" to profile.injuries
        )

        android.util.Log.d("FirestoreRepository", "Saving health profile for UID: $uid")

        // First ensure user document exists
        db.collection("users").document(uid).get()
            .addOnSuccessListener { userDoc ->
                if (!userDoc.exists()) {
                    // Create user document if it doesn't exist
                    android.util.Log.d("FirestoreRepository", "User document doesn't exist, creating it")
                    db.collection("users").document(uid).set(mapOf("uid" to uid))
                        .addOnSuccessListener {
                            saveHealthProfileToFirestore(uid, profileMap, onDone)
                        }
                        .addOnFailureListener { exception ->
                            android.util.Log.e("FirestoreRepository", "Failed to create user document: ${exception.message}", exception)
                            onDone(false)
                        }
                } else {
                    // User document exists, save health profile
                    saveHealthProfileToFirestore(uid, profileMap, onDone)
                }
            }
            .addOnFailureListener { exception ->
                android.util.Log.e("FirestoreRepository", "Failed to check user document: ${exception.message}", exception)
                onDone(false)
            }
    }

    private fun saveHealthProfileToFirestore(uid: String, profileMap: Map<String, Any>, onDone: (Boolean) -> Unit) {
        db.collection("users").document(uid).collection("healthProfile").document("current")
            .set(profileMap)
            .addOnSuccessListener {
                android.util.Log.d("FirestoreRepository", "Health profile saved successfully")
                onDone(true)
            }
            .addOnFailureListener { exception ->
                android.util.Log.e("FirestoreRepository", "Failed to save health profile: ${exception.message}", exception)
                onDone(false)
            }
    }

    fun getHealthProfile(uid: String, onResult: (com.fida.app.models.HealthProfile?) -> Unit) {
        db.collection("users").document(uid).collection("healthProfile").document("current")
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val profile = doc.toObject(com.fida.app.models.HealthProfile::class.java)
                    onResult(profile)
                } else {
                    onResult(null)
                }
            }
            .addOnFailureListener { onResult(null) }
    }
}
