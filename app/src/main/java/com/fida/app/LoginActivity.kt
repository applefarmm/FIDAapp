package com.fida.app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.fida.app.databinding.ActivityLoginBinding
import com.fida.app.utils.PreferenceHelper
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.firestore
import java.security.MessageDigest

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth
    private val db = Firebase.firestore

    // Facebook SDK - disabled (not configured in manifest)
    // private var callbackManager: com.facebook.CallbackManager? = null
    // private var isFacebookEnabled = false

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            Log.d("LoginActivity", "Google sign-in successful: ${account.email}")
            if (account.idToken != null) {
                firebaseAuthWithGoogle(account.idToken!!)
            } else {
                Log.e("LoginActivity", "Google sign-in failed: idToken is null")
                Toast.makeText(this, "Google Sign-In failed: No ID token received. Please check Firebase configuration.", Toast.LENGTH_LONG).show()
            }
        } catch (e: ApiException) {
            Log.e("LoginActivity", "Google sign-in failed: ${e.statusCode} - ${e.message}")
            val errorMessage = when (e.statusCode) {
                12501 -> "Sign-in cancelled"
                12500 -> "Sign-in failed. Please check your internet connection."
                10 -> "Developer error. Please check SHA-1 certificate in Firebase Console."
                else -> "Google Sign-In failed: ${e.message}"
            }
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        auth = FirebaseAuth.getInstance()

        // Facebook SDK disabled - button already hidden in layout
        // try {
        //     callbackManager = com.facebook.CallbackManager.Factory.create()
        //     isFacebookEnabled = true
        //     setupFacebookLogin()
        // } catch (e: Exception) {
        //     Log.w("LoginActivity", "Facebook SDK not configured: ${e.message}")
        //     isFacebookEnabled = false
        //     binding.facebookSignInButton.visibility = android.view.View.GONE
        // }

        // Configure Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Regular login
        binding.loginButton.setOnClickListener {
            val username = binding.loginUsername.text.toString().trim()
            val password = binding.loginPassword.text.toString().trim()
            if (username.isNotEmpty() && password.isNotEmpty()) {
                loginUser(username, password)
            } else {
                Toast.makeText(this, "Please Fill Out All Fields!", Toast.LENGTH_SHORT).show()
            }
        }

        // Google Sign-In
        binding.googleSignInButton.setOnClickListener {
            signInWithGoogle()
        }

        binding.signupRedirect.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
            finish()
        }

        // Forgot Password
        binding.tvForgotPassword.setOnClickListener {
            showForgotPasswordDialog()
        }
    }

    // Facebook login disabled - SDK not configured
    // private fun setupFacebookLogin() {
    //     if (!isFacebookEnabled || callbackManager == null) return
    //
    //     binding.facebookSignInButton.setPermissions("email", "public_profile")
    //     binding.facebookSignInButton.registerCallback(callbackManager!!, object : com.facebook.FacebookCallback<com.facebook.login.LoginResult> {
    //         override fun onSuccess(result: com.facebook.login.LoginResult) {
    //             Log.d("LoginActivity", "Facebook login successful")
    //             firebaseAuthWithFacebook(result.accessToken.token)
    //         }
    //
    //         override fun onCancel() {
    //             Log.d("LoginActivity", "Facebook login cancelled")
    //             Toast.makeText(this@LoginActivity, "Facebook Sign-In cancelled", Toast.LENGTH_SHORT).show()
    //         }
    //
    //         override fun onError(error: com.facebook.FacebookException) {
    //             Log.e("LoginActivity", "Facebook login error: ${error.message}")
    //             Toast.makeText(this@LoginActivity, "Facebook Sign-In failed: ${error.message}", Toast.LENGTH_SHORT).show()
    //         }
    //     })
    // }
    //
    // override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    //     super.onActivityResult(requestCode, resultCode, data)
    //     callbackManager?.onActivityResult(requestCode, resultCode, data)
    // }

    private fun signInWithGoogle() {
        googleSignInClient.signOut().addOnCompleteListener {
            googleSignInLauncher.launch(googleSignInClient.signInIntent)
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)

        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { signInTask ->
                if (signInTask.isSuccessful) {
                    val firebaseUser = auth.currentUser
                    if (firebaseUser != null) {
                        Log.d("LoginActivity", "Firebase auth successful for: ${firebaseUser.email}")
                        checkUserInFirestore(firebaseUser.uid, firebaseUser.email ?: "", firebaseUser.displayName ?: "User")
                    }
                } else {
                    Log.e("LoginActivity", "Firebase auth failed: ${signInTask.exception?.message}")
                    Toast.makeText(this, "Authentication failed: ${signInTask.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // Facebook auth disabled
    // private fun firebaseAuthWithFacebook(token: String) {
    //     val credential = com.google.firebase.auth.FacebookAuthProvider.getCredential(token)
    //
    //     auth.signInWithCredential(credential)
    //         .addOnCompleteListener(this) { signInTask ->
    //             if (signInTask.isSuccessful) {
    //                 val firebaseUser = auth.currentUser
    //                 if (firebaseUser != null) {
    //                     Log.d("LoginActivity", "Firebase auth successful for Facebook user: ${firebaseUser.email}")
    //                     checkUserInFirestore(firebaseUser.uid, firebaseUser.email ?: "", firebaseUser.displayName ?: "User")
    //                 }
    //             } else {
    //                 Log.e("LoginActivity", "Firebase auth failed: ${signInTask.exception?.message}")
    //                 Toast.makeText(this, "Authentication failed: ${signInTask.exception?.message}", Toast.LENGTH_SHORT).show()
    //             }
    //         }
    // }

    private fun checkUserInFirestore(uid: String, email: String, displayName: String) {
        db.collection("users").document(uid)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val profileSetupDone = document.getBoolean("profileSetupDone") ?: false

                    // Save UID to preferences
                    val prefs = PreferenceHelper(this)
                    prefs.setUid(uid)

                    if (!profileSetupDone) {
                        startActivity(Intent(this, ProfileSetup1Activity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show()
                        navigateToHome()
                    }
                } else {
                    // New social user - create Firestore document
                    createSocialUserInFirestore(uid, email, displayName)
                }
            }
            .addOnFailureListener { e ->
                Log.e("LoginActivity", "Error checking user: ${e.message}")
                Toast.makeText(this, "Error checking user: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun createSocialUserInFirestore(uid: String, email: String, displayName: String) {
        val user = hashMapOf(
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

        db.collection("users").document(uid)
            .set(user)
            .addOnSuccessListener {
                // Save UID to preferences
                val prefs = PreferenceHelper(this)
                prefs.setUid(uid)

                Toast.makeText(this, "Account created!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, ProfileSetup1Activity::class.java))
                finish()
            }
            .addOnFailureListener { e ->
                Log.e("LoginActivity", "Error creating account: ${e.message}")
                Toast.makeText(this, "Error creating account: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private fun navigateToHome() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }

    private fun loginUser(username: String, password: String) {
        // First, find the user's email from Firestore using username
        db.collection("users")
            .whereEqualTo("username", username)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val result = task.result
                    if (result != null && !result.isEmpty) {
                        val doc = result.documents[0]
                        val email = doc.getString("email")
                        val uid = doc.getString("uid")

                        if (email != null) {
                            // Use Firebase Auth to sign in with email/password
                            auth.signInWithEmailAndPassword(email, password)
                                .addOnSuccessListener {
                                    val profileSetupDone = doc.getBoolean("profileSetupDone") ?: false

                                    // Store uid in preferences
                                    if (uid != null) {
                                        val prefs = PreferenceHelper(this)
                                        prefs.setUid(uid)
                                    }

                                    if (!profileSetupDone) {
                                        Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show()
                                        startActivity(Intent(this, ProfileSetup1Activity::class.java))
                                        finish()
                                    } else {
                                        Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show()
                                        navigateToHome()
                                    }
                                }
                                .addOnFailureListener { e ->
                                    Log.e("LoginActivity", "Firebase Auth login failed: ${e.message}")
                                    Toast.makeText(this, "Incorrect password", Toast.LENGTH_SHORT).show()
                                }
                        } else {
                            Toast.makeText(this, "User data incomplete", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this, "User doesn't exist", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Login failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun showForgotPasswordDialog() {
        val view = LayoutInflater.from(this).inflate(R.layout.layout_forgot_password_dialog, null)
        val editText = view.findViewById<TextInputEditText>(R.id.etResetEmail)

        val alertDialog = MaterialAlertDialogBuilder(this)
            .setTitle("Reset Password")
            .setMessage("Enter your email address to receive a password reset link")
            .setView(view)
            .setPositiveButton("Send Reset Link") { _, _ ->
                val email = editText.text.toString().trim()
                if (email.isNotEmpty() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    sendPasswordResetEmail(email)
                } else {
                    Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()

        alertDialog.setOnShowListener {
            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(resources.getColor(R.color.blue, theme))
            alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(resources.getColor(R.color.grey_500, theme))
        }

        alertDialog.show()
    }

    private fun sendPasswordResetEmail(email: String) {
        // Send Firebase Auth reset email directly
        // Firebase Auth will handle checking if the email exists
        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                Toast.makeText(this, "Password reset email sent! Check your inbox.", Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener { e ->
                Log.e("LoginActivity", "Password reset failed: ${e.message}")
                val errorMessage = when {
                    e.message?.contains("no user record", ignoreCase = true) == true ->
                        "No account found with this email"
                    e.message?.contains("invalid email", ignoreCase = true) == true ->
                        "Invalid email address"
                    else -> "Failed to send reset email: ${e.message}"
                }
                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
            }
    }
}