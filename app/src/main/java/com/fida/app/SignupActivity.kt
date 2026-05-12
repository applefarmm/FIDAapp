package com.fida.app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.fida.app.utils.FirestoreRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException

class SignupActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)
        supportActionBar?.hide()

        auth = FirebaseAuth.getInstance()

        val etEmail = findViewById<EditText>(R.id.etSignupEmail)
        val etPassword = findViewById<EditText>(R.id.etSignupPassword)
        val etConfirmPassword = findViewById<EditText>(R.id.etSignupConfirmPassword)
        val btnSignup = findViewById<Button>(R.id.btnSignup)
        val tvLogin = findViewById<TextView>(R.id.tvLoginRedirect)

        btnSignup.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Strong password validation
            val passwordValidation = validatePassword(password)
            if (!passwordValidation.first) {
                Toast.makeText(this, passwordValidation.second, Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener { result ->
                    val user = result.user
                    Log.d("SignupActivity", "User created successfully: ${user?.uid}")

                    if (user != null) {
                        // Create Firestore document with default values
                        val defaultDoc = FirestoreRepository.defaultUserDoc(
                            user.uid,
                            email,
                            email.substringBefore("@") // default username from email
                        )
                        FirestoreRepository.createUser(user.uid, defaultDoc) { success ->
                            if (success) {
                                Log.d("SignupActivity", "Firestore document created")
                                startActivity(Intent(this, ProfileSetup1Activity::class.java))
                                finish()
                            } else {
                                Log.e("SignupActivity", "Failed to create Firestore document")
                                Toast.makeText(this, "Account created but profile setup failed. Please try logging in.", Toast.LENGTH_LONG).show()
                                startActivity(Intent(this, LoginActivity::class.java))
                                finish()
                            }
                        }
                    } else {
                        Log.e("SignupActivity", "User is null after signup")
                        Toast.makeText(this, "Signup failed", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("SignupActivity", "Signup failed: ${e.message}", e)
                    
                    val errorMessage = when (e) {
                        is FirebaseAuthWeakPasswordException -> "Password is too weak."
                        is FirebaseAuthInvalidCredentialsException -> "Invalid email format."
                        is FirebaseAuthUserCollisionException -> "This email is already registered."
                        is FirebaseAuthException -> {
                            when (e.errorCode) {
                                "ERROR_OPERATION_NOT_ALLOWED" -> "Email/Password sign-in is NOT enabled in Firebase Console. Go to Authentication > Sign-in method and enable it."
                                "ERROR_INVALID_EMAIL" -> "The email address is badly formatted."
                                else -> e.localizedMessage ?: "Firebase error: ${e.errorCode}"
                            }
                        }
                        else -> e.localizedMessage ?: "Signup failed. Please check your internet connection."
                    }
                    
                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                }
        }

        tvLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun validatePassword(password: String): Pair<Boolean, String> {
        if (password.length < 8) {
            return Pair(false, "Password must be at least 8 characters")
        }
        if (!password.any { it.isUpperCase() }) {
            return Pair(false, "Password must contain at least one uppercase letter")
        }
        if (!password.any { it.isLowerCase() }) {
            return Pair(false, "Password must contain at least one lowercase letter")
        }
        if (!password.any { it.isDigit() }) {
            return Pair(false, "Password must contain at least one number")
        }
        if (!password.any { !it.isLetterOrDigit() }) {
            return Pair(false, "Password must contain at least one special character (!@#$%^&*)")
        }
        return Pair(true, "")
    }
}
