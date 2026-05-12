package com.fida.app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.fida.app.utils.PreferenceHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class ProfileSetup3Activity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private lateinit var prefs: PreferenceHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_setup3)
        supportActionBar?.hide()

        prefs = PreferenceHelper(this)

        val rgFitnessGoal = findViewById<RadioGroup>(R.id.rgFitnessGoal)
        val btnFinish = findViewById<Button>(R.id.btnFinish)

        btnFinish.setOnClickListener {
            val goal = when (rgFitnessGoal.checkedRadioButtonId) {
                R.id.rbLoseWeight -> "Lose Weight"
                R.id.rbBuildMuscle -> "Build Muscle"
                R.id.rbImproveEndurance -> "Improve Endurance"
                R.id.rbStayHealthy -> "Stay Healthy"
                R.id.rbIncreaseFlexibility -> "Increase Flexibility"
                else -> {
                    Toast.makeText(this, "Please select a fitness goal", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }

            val user = auth.currentUser
            if (user == null) {
                Toast.makeText(this, "Session expired", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val uid = user.uid
            val updates = hashMapOf(
                "fitnessGoal" to goal,
                "profileSetupDone" to true,
                "xp" to 0,
                "coins" to 0,
                "level" to 1,
                "dailySteps" to 0,
                "dailyWater" to 0,
                "dailySleep" to 0.0,
                "dailyCoins" to 0
            )

            btnFinish.isEnabled = false
            btnFinish.text = "Finishing..."

            db.collection("users").document(uid)
                .set(updates, SetOptions.merge())
                .addOnSuccessListener {
                    Log.d("ProfileSetup3", "Profile setup complete for $uid")
                    prefs.setProfileSetupComplete()
                    prefs.setLoggedIn(true)
                    
                    val intent = Intent(this, HomeActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
                .addOnFailureListener { e ->
                    btnFinish.isEnabled = true
                    btnFinish.text = "Finish"
                    Log.e("ProfileSetup3", "Error finishing setup", e)
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
