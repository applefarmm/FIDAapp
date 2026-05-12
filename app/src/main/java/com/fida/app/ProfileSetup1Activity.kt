package com.fida.app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.fida.app.utils.PreferenceHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileSetup1Activity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var prefs: PreferenceHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_setup1)
        supportActionBar?.hide()

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        prefs = PreferenceHelper(this)

        // Enforce Login: If no user is logged in, force navigation to Login
        if (auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        val etName = findViewById<EditText>(R.id.etName)
        val rgGender = findViewById<RadioGroup>(R.id.rgGender)
        val btnNext = findViewById<Button>(R.id.btnNext)

        btnNext.setOnClickListener {
            val name = etName.text.toString().trim()
            if (name.isEmpty()) {
                etName.error = "Name is required"
                return@setOnClickListener
            }

            val currentUser = auth.currentUser
            if (currentUser == null) {
                Log.e("ProfileSetup1", "No authenticated user found")
                Toast.makeText(this, "Session expired. Please login again.", Toast.LENGTH_LONG).show()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
                return@setOnClickListener
            }

            val gender = when (rgGender.checkedRadioButtonId) {
                R.id.rbFemale -> "female"
                R.id.rbOther -> "other"
                else -> "male"
            }
            val uid = currentUser.uid
            Log.d("ProfileSetup1", "Saving profile for uid: $uid, name: $name, gender: $gender")

            btnNext.isEnabled = false
            btnNext.text = "Saving..."

            // First check if document exists, then write
            db.collection("users").document(uid).get()
                .addOnCompleteListener { checkTask ->
                    if (checkTask.isSuccessful && checkTask.result?.exists() == true) {
                        Log.d("ProfileSetup1", "Document exists, updating...")
                        updateDocument(uid, name, gender, btnNext)
                    } else if (checkTask.isSuccessful && checkTask.result?.exists() == false) {
                        Log.d("ProfileSetup1", "Document does NOT exist, creating...")
                        // Create document with all required fields
                        val fullData = hashMapOf(
                            "uid" to uid,
                            "email" to (currentUser.email ?: ""),
                            "name" to name,
                            "gender" to gender,
                            "profileSetupDone" to false,
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
                            "badges" to emptyMap<String, Any>(),
                            "allDays" to emptyMap<String, Any>()
                        )
                        createDocument(uid, fullData, name, btnNext)
                    } else {
                        Log.e("ProfileSetup1", "Check failed: ${checkTask.exception?.message}")
                        btnNext.isEnabled = true
                        btnNext.text = "Next"
                        Toast.makeText(this, "Error checking profile: ${checkTask.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }

    private fun updateDocument(uid: String, name: String, gender: String, btnNext: Button) {
        val userData = hashMapOf(
            "name" to name,
            "gender" to gender
        )
        db.collection("users").document(uid)
            .update(userData as Map<String, Any>)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("ProfileSetup1", "Update successful, navigating to ProfileSetup2")
                    prefs.setUid(uid)
                    prefs.saveUsername(name)
                    startActivity(Intent(this, ProfileSetup2Activity::class.java))
                    finish()
                } else {
                    Log.e("ProfileSetup1", "Update failed: ${task.exception?.message}")
                    btnNext.isEnabled = true
                    btnNext.text = "Next"
                    Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun createDocument(uid: String, fullData: HashMap<String, Any>, name: String, btnNext: Button) {
        db.collection("users").document(uid)
            .set(fullData)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("ProfileSetup1", "Create successful, navigating to ProfileSetup2")
                    prefs.setUid(uid)
                    prefs.saveUsername(name)
                    startActivity(Intent(this, ProfileSetup2Activity::class.java))
                    finish()
                } else {
                    Log.e("ProfileSetup1", "Create failed: ${task.exception?.message}")
                    btnNext.isEnabled = true
                    btnNext.text = "Next"
                    Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }
}