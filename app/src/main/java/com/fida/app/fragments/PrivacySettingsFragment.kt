package com.fida.app.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.fida.app.MainActivity
import com.fida.app.R
import com.fida.app.SettingsActivity
import com.fida.app.utils.PreferenceHelper
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PrivacySettingsFragment : Fragment() {

    private lateinit var prefs: PreferenceHelper
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?):
        View? {
        val view = inflater.inflate(R.layout.fragment_privacy_settings, container, false)
        prefs = PreferenceHelper(requireContext())
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        setupViews(view)
        return view
    }

    private fun setupViews(view: View) {
        val tvBack = view.findViewById<TextView>(R.id.tvBackToSettingsPrivacy)
        val switchDataSharing = view.findViewById<SwitchMaterial>(R.id.switchDataSharing)
        val switchProfileVisibility = view.findViewById<SwitchMaterial>(R.id.switchProfileVisibility)
        val tvExportData = view.findViewById<TextView>(R.id.tvExportData)
        val tvDeleteAccount = view.findViewById<TextView>(R.id.tvDeleteAccount)

        // Set initial states for toggles
        switchDataSharing.isChecked = prefs.getBoolean("dataSharingEnabled") ?: true
        switchProfileVisibility.isChecked = prefs.getBoolean("profileVisibilityEnabled") ?: true

        // Set listeners for toggles
        switchDataSharing.setOnCheckedChangeListener { _, isChecked ->
            prefs.saveBoolean("dataSharingEnabled", isChecked)
            val status = if (isChecked) "enabled" else "disabled"
            Toast.makeText(context, "Data sharing $status", Toast.LENGTH_SHORT).show()
        }

        switchProfileVisibility.setOnCheckedChangeListener { _, isChecked ->
            prefs.saveBoolean("profileVisibilityEnabled", isChecked)
            val status = if (isChecked) "enabled" else "disabled"
            Toast.makeText(context, "Profile visibility $status", Toast.LENGTH_SHORT).show()
        }

        tvExportData.setOnClickListener {
            exportMyData()
        }

        tvDeleteAccount.setOnClickListener {
            showDeleteAccountDialog()
        }

        tvBack.setOnClickListener {
            activity?.let { (it as SettingsActivity).loadFragment(SettingsFragment()) }
        }
    }

    private fun exportMyData() {
        val user = auth.currentUser
        if (user == null) {
            Toast.makeText(context, "Not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        Toast.makeText(context, "Exporting your data...", Toast.LENGTH_SHORT).show()

        // Collect all user data
        collectAndExportUserData(user.uid)
    }

    private fun collectAndExportUserData(uid: String) {
        val jsonData = JSONObject()

        // Get main user document
        db.collection("users").document(uid).get()
            .addOnSuccessListener { userDoc ->
                if (userDoc.exists()) {
                    jsonData.put("profile", JSONObject(userDoc.data ?: emptyMap<String, Any>()))

                    // Get runs subcollection
                    db.collection("users").document(uid).collection("runs").get()
                        .addOnSuccessListener { runsSnap ->
                            val runsArray = org.json.JSONArray()
                            runsSnap.documents.forEach { runDoc ->
                                runsArray.put(JSONObject(runDoc.data ?: emptyMap<String, Any>()))
                            }
                            jsonData.put("runs", runsArray)

                            // Get quests subcollection
                            db.collection("users").document(uid).collection("quests").get()
                                .addOnSuccessListener { questsSnap ->
                                    val questsArray = org.json.JSONArray()
                                    questsSnap.documents.forEach { questDoc ->
                                        questsArray.put(JSONObject(questDoc.data ?: emptyMap<String, Any>()))
                                    }
                                    jsonData.put("quests", questsArray)

                                    // Get challenges subcollection
                                    db.collection("users").document(uid).collection("challenges").get()
                                        .addOnSuccessListener { challengesSnap ->
                                            val challengesArray = org.json.JSONArray()
                                            challengesSnap.documents.forEach { challengeDoc ->
                                                challengesArray.put(JSONObject(challengeDoc.data ?: emptyMap<String, Any>()))
                                            }
                                            jsonData.put("challenges", challengesArray)

                                            // Save to file
                                            saveExportedData(jsonData)
                                        }
                                        .addOnFailureListener {
                                            saveExportedData(jsonData) // Save what we have
                                        }
                                }
                                .addOnFailureListener {
                                    saveExportedData(jsonData) // Save what we have
                                }
                        }
                        .addOnFailureListener {
                            saveExportedData(jsonData) // Save what we have
                        }
                } else {
                    Toast.makeText(context, "No user data found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Failed to export: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveExportedData(jsonData: JSONObject) {
        try {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "fida_data_export_$timestamp.json"

            // Save to app's external files directory (accessible to user)
            val exportsDir = File(requireContext().getExternalFilesDir(null), "exports")
            if (!exportsDir.exists()) exportsDir.mkdirs()

            val exportFile = File(exportsDir, fileName)
            FileOutputStream(exportFile).use { fos: FileOutputStream ->
                fos.write(jsonData.toString().toByteArray())
            }

            Toast.makeText(
                context,
                "Data exported to: ${exportFile.absolutePath}",
                Toast.LENGTH_LONG
            ).show()

            // Show dialog with file location
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Data Export Complete")
                .setMessage("Your data has been exported to:\n${exportFile.absolutePath}\n\nYou can access this file through your device's file manager.")
                .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                .show()
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to save file: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showDeleteAccountDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Account")
            .setMessage("Are you sure you want to delete your account? This will:\n\n• Delete all your profile data\n• Delete your run history\n• Delete your quests and challenges\n• Remove you from leaderboards\n\nThis action cannot be undone.")
            .setPositiveButton("Delete") { dialog, _ ->
                confirmDeleteAccount()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun confirmDeleteAccount() {
        // Show second confirmation
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Final Confirmation")
            .setMessage("Type DELETE to confirm account deletion.")
            .setPositiveButton("DELETE") { dialog, _ ->
                performAccountDeletion()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun performAccountDeletion() {
        val user = auth.currentUser
        if (user == null) {
            Toast.makeText(context, "Not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        val uid = user.uid

        Toast.makeText(context, "Deleting account...", Toast.LENGTH_SHORT).show()

        // Delete Firestore data first
        deleteFirestoreUserData(uid, user)
    }

    private fun deleteFirestoreUserData(uid: String, firebaseUser: com.google.firebase.auth.FirebaseUser) {
        val userRef = db.collection("users").document(uid)

        // Delete subcollections first
        val batch = db.batch()

        // Delete runs
        userRef.collection("runs").get()
            .addOnSuccessListener { runsSnap ->
                runsSnap.documents.forEach { doc -> batch.delete(doc.reference) }

                // Delete quests
                userRef.collection("quests").get()
                    .addOnSuccessListener { questsSnap ->
                        questsSnap.documents.forEach { doc -> batch.delete(doc.reference) }

                        // Delete challenges
                        userRef.collection("challenges").get()
                            .addOnSuccessListener { challengesSnap ->
                                challengesSnap.documents.forEach { doc -> batch.delete(doc.reference) }

                                // Commit batch and delete main document
                                batch.commit()
                                    .addOnSuccessListener {
                                        // Delete main user document
                                        userRef.delete()
                                            .addOnSuccessListener {
                                                // Delete Firebase Auth account
                                                deleteFirebaseAuthAccount(firebaseUser)
                                            }
                                            .addOnFailureListener { e ->
                                                Toast.makeText(
                                                    context,
                                                    "Failed to delete user data: ${e.message}",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(
                                            context,
                                            "Failed to delete subcollections: ${e.message}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                            }
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error accessing data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun deleteFirebaseAuthAccount(firebaseUser: com.google.firebase.auth.FirebaseUser) {
        firebaseUser.delete()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Clear local preferences
                    prefs.clear()

                    Toast.makeText(context, "Account deleted successfully", Toast.LENGTH_LONG).show()

                    // Navigate to login/signup screen
                    val intent = android.content.Intent(requireContext(), MainActivity::class.java)
                    intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or
                        android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    activity?.finish()
                } else {
                    Toast.makeText(
                        context,
                        "Failed to delete auth account: ${task.exception?.message}\nYou may need to re-login first.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }
}