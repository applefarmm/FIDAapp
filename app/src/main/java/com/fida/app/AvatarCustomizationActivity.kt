package com.fida.app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class AvatarCustomizationActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val avatarOptions = listOf(
        R.drawable.avatar_1,
        R.drawable.avatar_2,
        R.drawable.avatar_3,
        R.drawable.avatar_4,
        R.drawable.avatar_5,
        R.drawable.avatar_6
    )

    private var selectedAvatarIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_avatar_customization)
        supportActionBar?.hide()

        val ivAvatar = findViewById<ImageView>(R.id.ivAvatarPreview)
        val btnPrev = findViewById<Button>(R.id.btnPrevAvatar)
        val btnNext = findViewById<Button>(R.id.btnNextAvatar)
        val btnContinue = findViewById<Button>(R.id.btnContinue)

        // Set initial avatar
        ivAvatar.setImageResource(avatarOptions[selectedAvatarIndex])

        val tvAvatarNumber = findViewById<android.widget.TextView>(R.id.tvAvatarNumber)

        fun updateAvatarDisplay() {
            ivAvatar.setImageResource(avatarOptions[selectedAvatarIndex])
            tvAvatarNumber.text = "${selectedAvatarIndex + 1} / ${avatarOptions.size}"
        }

        btnPrev.setOnClickListener {
            selectedAvatarIndex = (selectedAvatarIndex - 1).coerceAtLeast(0)
            updateAvatarDisplay()
        }

        btnNext.setOnClickListener {
            selectedAvatarIndex = (selectedAvatarIndex + 1).coerceAtMost(avatarOptions.size - 1)
            updateAvatarDisplay()
        }

        updateAvatarDisplay()

        btnContinue.setOnClickListener {
            val uid = auth.currentUser?.uid
            if (uid == null) {
                Toast.makeText(this, "Session expired", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnContinue.isEnabled = false
            btnContinue.text = "Saving..."

            val avatarName = "avatar_${selectedAvatarIndex + 1}"
            val updates = hashMapOf("avatar" to avatarName)

            db.collection("users").document(uid)
                .set(updates, SetOptions.merge())
                .addOnSuccessListener {
                    val intent = Intent(this, ProfileSetup3Activity::class.java)
                    startActivity(intent)
                    finish()
                }
                .addOnFailureListener { e ->
                    btnContinue.isEnabled = true
                    btnContinue.text = "Continue"
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}