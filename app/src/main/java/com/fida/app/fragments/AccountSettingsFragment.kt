package com.fida.app.fragments

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.fida.app.R
import com.fida.app.SettingsActivity
import com.fida.app.utils.PreferenceHelper
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.storage.FirebaseStorage

class AccountSettingsFragment : Fragment() {

    private lateinit var prefs: PreferenceHelper
    private lateinit var auth: FirebaseAuth
    private var selectedImageUri: Uri? = null

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            view?.findViewById<ImageView>(R.id.ivAccountPhoto)?.let {
                Glide.with(this).load(uri).into(it)
            }
            uploadPhotoToFirebase(uri)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_account_settings, container, false)
        prefs = PreferenceHelper(requireContext())
        auth = FirebaseAuth.getInstance()
        setupViews(view)
        return view
    }

    private fun setupViews(view: View) {
        val etAccountName = view.findViewById<EditText>(R.id.etAccountName)
        val tvEmail = view.findViewById<TextView>(R.id.tvAccountEmail)
        val ivProfilePhoto = view.findViewById<ImageView>(R.id.ivAccountPhoto)
        val btnSaveChanges = view.findViewById<MaterialButton>(R.id.btnSaveChangesAccount)
        val tvBack = view.findViewById<TextView>(R.id.tvBackToSettings)
        val tvChangePassword = view.findViewById<TextView>(R.id.tvChangePassword)
        val tvLinkedAccounts = view.findViewById<TextView>(R.id.tvLinkedAccounts)
        val tvChangePhoto = view.findViewById<TextView>(R.id.tvChangePhoto)

        // Load current user data
        etAccountName.setText(prefs.getUsername() ?: "Athlete")
        tvEmail.text = prefs.getEmail() ?: ""

        // Load profile photo from Firestore (source of truth)
        val uid = prefs.getUid()
        if (uid != null) {
            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("users").document(uid)
                .get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        val photoUrl = doc.getString("profilePicture")
                        if (!photoUrl.isNullOrEmpty()) {
                            Glide.with(this@AccountSettingsFragment).load(photoUrl).into(ivProfilePhoto)
                        } else {
                            // Fallback to Auth photo if Firestore has none
                            auth.currentUser?.photoUrl?.let {
                                Glide.with(this@AccountSettingsFragment).load(it).into(ivProfilePhoto)
                            }
                        }
                    }
                }
        } else {
            // No uid — fallback to Auth photo
            auth.currentUser?.photoUrl?.let {
                Glide.with(this).load(it).into(ivProfilePhoto)
            }
        }

        tvBack.setOnClickListener {
            activity?.let { (it as SettingsActivity).loadFragment(SettingsFragment()) }
        }

        btnSaveChanges.setOnClickListener {
            val newName = etAccountName.text.toString().trim()
            if (newName.isNotEmpty() && newName != prefs.getUsername()) {
                updateProfileName(newName)
            }
            Toast.makeText(context, "Changes saved!", Toast.LENGTH_SHORT).show()
        }

        tvChangePassword.setOnClickListener {
            showChangePasswordDialog()
        }

        tvLinkedAccounts.setOnClickListener {
            showLinkedAccountsDialog()
        }

        tvChangePhoto.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }
    }

    private fun updateProfileName(newName: String) {
        val user = auth.currentUser ?: return
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(newName)
            .build()

        user.updateProfile(profileUpdates)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    prefs.saveUsername(newName)
                    Toast.makeText(context, "Name updated successfully.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(
                        context,
                        "Failed to update name: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun uploadPhotoToFirebase(uri: Uri) {
        val user = auth.currentUser ?: run {
            Toast.makeText(context, "Not logged in.", Toast.LENGTH_SHORT).show()
            return
        }

        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.reference.child("profile_photos/${user.uid}")
        val uploadTask = storageRef.putFile(uri)

        uploadTask.addOnFailureListener { e ->
            val msg = buildErrorMessage(e)
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            Log.e("AccountSettings", "Storage upload failed", e)
        }

        // Use addOnSuccessListener instead of continueWithTask to get download URL
        // This avoids the deprecation warning on downloadUrl Property
        uploadTask.addOnSuccessListener {
            storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                updateProfilePhoto(downloadUri)
            }.addOnFailureListener { e ->
                Toast.makeText(context, "Upload succeeded but could not get photo URL: ${e.message}", Toast.LENGTH_LONG).show()
                Log.e("AccountSettings", "downloadUrl failed", e)
            }
        }
    }

    private fun buildErrorMessage(e: Exception): String {
        val msg = e.message ?: "Unknown error"
        return when {
            msg.contains("bucket", true) -> "Storage bucket not configured. Go to Firebase Console \u2192 Build \u2192 Storage \u2192 Get started"
            msg.contains("not-found", true) || msg.contains("object does not exist", true) ->
                "Storage bucket not found. Go to Firebase Console \u2192 Build \u2192 Storage \u2192 Get started"
            msg.contains("permission", true) || msg.contains("denied", true) ->
                "Upload denied. Go to Firebase Console \u2192 Storage \u2192 Rules and allow read/write"
            msg.contains("network", true) || msg.contains("connection", true) ->
                "Network error. Check your internet connection"
            else -> "Upload failed: $msg"
        }
    }

    private fun updateProfilePhoto(uri: Uri) {
        val user = auth.currentUser ?: return

        // 1. Update Firebase Auth profile (for auth-based reads)
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setPhotoUri(uri)
            .build()

        user.updateProfile(profileUpdates)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // 2. Save photo URL to Firestore so ProfileFragment can read it
                    val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    db.collection("users").document(user.uid)
                        .update("profilePicture", uri.toString())
                        .addOnSuccessListener {
                            Toast.makeText(context, "Profile photo updated!", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Log.e("AccountSettings", "Failed to save photo URL to Firestore", e)
                            Toast.makeText(context, "Photo uploaded but failed to save to profile", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(
                        context,
                        "Failed to update photo: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun showChangePasswordDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_change_password, null)

        val etCurrentPassword = dialogView.findViewById<EditText>(R.id.etCurrentPassword)
        val etNewPassword = dialogView.findViewById<EditText>(R.id.etNewPassword)
        val etConfirmPassword = dialogView.findViewById<EditText>(R.id.etConfirmPassword)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)
        val btnChange = dialogView.findViewById<Button>(R.id.btnChangePassword)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        btnCancel.setOnClickListener { dialog.dismiss() }

        btnChange.setOnClickListener {
            val currentPassword = etCurrentPassword.text.toString().trim()
            val newPassword = etNewPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()

            if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newPassword != confirmPassword) {
                Toast.makeText(context, "New passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newPassword.length < 6) {
                Toast.makeText(context, "Password must be at least 6 characters", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            changePassword(currentPassword, newPassword, dialog)
        }

        dialog.show()
    }

    private fun changePassword(currentPassword: String, newPassword: String, dialog: AlertDialog) {
        val user = auth.currentUser ?: return
        val email = user.email ?: return

        // Re-authenticate user before changing password
        val credential = com.google.firebase.auth.EmailAuthProvider.getCredential(email, currentPassword)

        user.reauthenticate(credential)
            .addOnCompleteListener { reauthTask ->
                if (reauthTask.isSuccessful) {
                    user.updatePassword(newPassword)
                        .addOnCompleteListener { updateTask ->
                            if (updateTask.isSuccessful) {
                                Toast.makeText(context, "Password changed successfully!", Toast.LENGTH_SHORT).show()
                                dialog.dismiss()
                            } else {
                                Toast.makeText(
                                    context,
                                    "Failed to change password: ${updateTask.exception?.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                } else {
                    Toast.makeText(
                        context,
                        "Current password is incorrect",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun showLinkedAccountsDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_linked_accounts, null)

        val tvGoogleStatus = dialogView.findViewById<TextView>(R.id.tvGoogleStatus)
        val tvFacebookStatus = dialogView.findViewById<TextView>(R.id.tvFacebookStatus)
        val btnGoogleAction = dialogView.findViewById<Button>(R.id.btnGoogleAction)
        val btnFacebookAction = dialogView.findViewById<Button>(R.id.btnFacebookAction)
        val btnClose = dialogView.findViewById<Button>(R.id.btnClose)

        val user = auth.currentUser ?: return
        val providerData = user.providerData

        // Check current linked providers
        val isGoogleLinked = providerData.any { it.providerId == GoogleAuthProvider.PROVIDER_ID }
        val isFacebookLinked = providerData.any { it.providerId == FacebookAuthProvider.PROVIDER_ID }

        tvGoogleStatus.text = if (isGoogleLinked) "Google: Linked" else "Google: Not linked"
        tvFacebookStatus.text = if (isFacebookLinked) "Facebook: Linked" else "Facebook: Not linked"

        btnGoogleAction.text = if (isGoogleLinked) "Unlink" else "Link"
        btnFacebookAction.text = if (isFacebookLinked) "Unlink" else "Link"

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        btnGoogleAction.setOnClickListener {
            if (isGoogleLinked) {
                unlinkProvider(GoogleAuthProvider.PROVIDER_ID, dialog)
            } else {
                Toast.makeText(context, "Link Google from login screen", Toast.LENGTH_SHORT).show()
            }
        }

        btnFacebookAction.setOnClickListener {
            if (isFacebookLinked) {
                unlinkProvider(FacebookAuthProvider.PROVIDER_ID, dialog)
            } else {
                Toast.makeText(context, "Link Facebook from login screen", Toast.LENGTH_SHORT).show()
            }
        }

        btnClose.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }

    private fun unlinkProvider(providerId: String, dialog: AlertDialog) {
        val user = auth.currentUser ?: return

        // Ensure at least one provider remains (email/password)
        if (user.providerData.size <= 1) {
            Toast.makeText(
                context,
                "Cannot unlink - you need at least one login method",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        user.unlink(providerId)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(context, "Provider unlinked successfully", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                    // Refresh dialog
                    showLinkedAccountsDialog()
                } else {
                    Toast.makeText(
                        context,
                        "Failed to unlink: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }
}