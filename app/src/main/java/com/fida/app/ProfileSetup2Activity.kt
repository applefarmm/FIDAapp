package com.fida.app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class ProfileSetup2Activity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_setup2)
        supportActionBar?.hide()

        val etAge = findViewById<EditText>(R.id.etAge)
        val etWeight = findViewById<EditText>(R.id.etWeight)
        val etHeight = findViewById<EditText>(R.id.etHeight)
        val btnNext = findViewById<Button>(R.id.btnNext)

        btnNext.setOnClickListener {
            val ageStr = etAge.text.toString().trim()
            val weightStr = etWeight.text.toString().trim()
            val heightStr = etHeight.text.toString().trim()

            if (ageStr.isEmpty() || weightStr.isEmpty() || heightStr.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val updates = hashMapOf(
                "age" to (ageStr.toIntOrNull() ?: 0),
                "weight" to (weightStr.toFloatOrNull() ?: 0f),
                "height" to (heightStr.toFloatOrNull() ?: 0f)
            )

            val uid = auth.currentUser?.uid
            if (uid == null) {
                Toast.makeText(this, "Error: User not found", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnNext.isEnabled = false
            btnNext.text = "Saving..."

            db.collection("users").document(uid)
                .set(updates, SetOptions.merge())
                .addOnSuccessListener {
                    val intent = Intent(this, AvatarCustomizationActivity::class.java)
                    startActivity(intent)
                }
                .addOnFailureListener { e ->
                    btnNext.isEnabled = true
                    btnNext.text = "Next"
                    Log.e("ProfileSetup2", "Error updating profile", e)
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
