package com.fida.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.fida.app.utils.FirestoreRepository
import com.fida.app.utils.PreferenceHelper
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.Priority
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.material.button.MaterialButton
import java.util.Timer
import java.util.TimerTask

class RunTrackingActivity : AppCompatActivity() {

    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var locationCallback: LocationCallback? = null

    private var startTime: Long = 0
    private var elapsedSeconds: Int = 0
    private var distanceMeters: Float = 0f
    private var previousLat: Double = 0.0
    private var previousLon: Double = 0.0
    private var isTracking: Boolean = true

    private var goalType: Int = 0
    private var goalValue: Float = 0f
    private lateinit var uid: String

    private val timer = Timer()
    private var locationUpdatesStarted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_run_tracking)
        supportActionBar?.hide()

        goalType = intent.getIntExtra("goalType", 0)
        goalValue = intent.getFloatExtra("goalValue", 0f)
        uid = intent.getStringExtra("uid") ?: PreferenceHelper(this).getUid() ?: ""

        setupViews()
        checkGooglePlayServicesAndStartTracking()
    }

    private fun checkGooglePlayServicesAndStartTracking() {
        val googleApiAvailability = GoogleApiAvailability.getInstance()
        val resultCode = googleApiAvailability.isGooglePlayServicesAvailable(this)

        if (resultCode != ConnectionResult.SUCCESS) {
            if (googleApiAvailability.isUserResolvableError(resultCode)) {
                googleApiAvailability.getErrorDialog(this, resultCode, 1000)?.show()
            } else {
                Toast.makeText(this, "Google Play Services not available. Location tracking disabled.", Toast.LENGTH_LONG).show()
            }
            // Start timer anyway even without location
            startTimerOnly()
            return
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        startTracking()
    }

    private fun setupViews() {
        findViewById<MaterialButton>(R.id.btnPauseRun).setOnClickListener {
            if (isTracking) {
                pauseTracking()
            } else {
                resumeTracking()
            }
        }

        findViewById<MaterialButton>(R.id.btnEndRun).setOnClickListener {
            endRun()
        }

        // Update goal display
        val goalText = when (goalType) {
            0 -> "Goal: ${goalValue.toInt()} km"
            1 -> "Goal: ${goalValue.toInt()} minutes"
            2 -> "Goal: ${goalValue.toInt()} calories"
            else -> ""
        }
        findViewById<android.widget.TextView>(R.id.tvRunGoal).text = goalText
    }

    private fun startTimerOnly() {
        startTime = System.currentTimeMillis()

        timer.schedule(object : TimerTask() {
            override fun run() {
                if (isTracking) {
                    elapsedSeconds++
                    runOnUiThread { updateUI() }
                    checkGoalReached()
                }
            }
        }, 1000, 1000)
    }

    private fun startTracking() {
        startTime = System.currentTimeMillis()

        // Check location permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE)
            // Start timer while waiting for permission
            startTimerOnly()
            return
        }

        startLocationUpdates()
        startTimerOnly()
    }

    private fun startLocationUpdates() {
        if (fusedLocationClient == null) return

        try {
            val locationRequest = LocationRequest.Builder(1000)
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .setMinUpdateIntervalMillis(500)
                .build()

            locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    locationResult.lastLocation?.let { location ->
                        if (isTracking && previousLat != 0.0 && previousLon != 0.0) {
                            val dist = calculateDistance(
                                previousLat, previousLon,
                                location.latitude, location.longitude
                            )
                            // Filter out unrealistic jumps (>100m in 1 second)
                            if (dist < 100) {
                                distanceMeters += dist
                            }
                        }
                        previousLat = location.latitude
                        previousLon = location.longitude
                    }
                }
            }

            fusedLocationClient?.requestLocationUpdates(
                locationRequest,
                locationCallback!!,
                Looper.getMainLooper()
            )
            locationUpdatesStarted = true
        } catch (e: SecurityException) {
            Toast.makeText(this, "Location permission denied: ${e.message}", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Location error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates()
            } else {
                Toast.makeText(this, "Location permission denied. Distance tracking disabled.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun pauseTracking() {
        isTracking = false
        findViewById<MaterialButton>(R.id.btnPauseRun).text = "Resume"
    }

    private fun resumeTracking() {
        isTracking = true
        findViewById<MaterialButton>(R.id.btnPauseRun).text = "Pause"
    }

    private fun endRun() {
        isTracking = false
        timer.cancel()

        // Stop location updates
        if (fusedLocationClient != null && locationCallback != null && locationUpdatesStarted) {
            fusedLocationClient?.removeLocationUpdates(locationCallback!!)
        }

        // Save run data
        val runData = mapOf(
            "durationSeconds" to elapsedSeconds,
            "distanceMeters" to distanceMeters,
            "goalType" to goalType,
            "goalValue" to goalValue,
            "timestamp" to System.currentTimeMillis(),
            "completed" to goalReached()
        )
        FirestoreRepository.logRun(uid, runData) { success ->
            if (success) {
                awardXP()
            }
        }

        // Navigate to summary
        startActivity(Intent(this, RunSummaryActivity::class.java).apply {
            putExtra("duration", elapsedSeconds)
            putExtra("distance", distanceMeters)
            putExtra("goalReached", goalReached())
            putExtra("calories", (elapsedSeconds * 0.15f).toInt())
            putExtra("uid", uid)
        })
        finish()
    }

    private fun updateUI() {
        findViewById<android.widget.TextView>(R.id.tvRunDuration).text = formatDuration(elapsedSeconds)
        findViewById<android.widget.TextView>(R.id.tvRunDistance).text =
            "%.2f km".format(distanceMeters / 1000)
        findViewById<android.widget.TextView>(R.id.tvRunPace).text = calculatePace()
    }

    private fun formatDuration(seconds: Int): String {
        val mins = seconds / 60
        val secs = seconds % 60
        return "%02d:%02d".format(mins, secs)
    }

    private fun calculatePace(): String {
        if (distanceMeters < 100) return "--:-- /km"
        val paceSeconds = elapsedSeconds / (distanceMeters / 1000)
        return "%02d:%02d /km".format(paceSeconds / 60, paceSeconds % 60)
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
        val earthRadius = 6371000.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon/2) * Math.sin(dLon/2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a))
        return (earthRadius * c).toFloat()
    }

    private fun goalReached(): Boolean {
        return when (goalType) {
            0 -> (distanceMeters / 1000) >= goalValue
            1 -> elapsedSeconds >= (goalValue * 60).toInt()
            2 -> (elapsedSeconds * 0.15f) >= goalValue // Approx calories
            else -> false
        }
    }

    private fun checkGoalReached() {
        if (goalReached()) {
            runOnUiThread {
                findViewById<android.widget.TextView>(R.id.tvRunStatus).text = "🎉 Goal reached!"
            }
        }
    }

    private fun awardXP() {
        val baseXP = if (goalReached()) 50L else 30L
        val distanceBonus = ((distanceMeters / 1000).toInt() * 5L)
        FirestoreRepository.incrementUserField(uid, "xp", baseXP + distanceBonus) {}
    }

    override fun onDestroy() {
        super.onDestroy()
        timer.cancel()
        if (fusedLocationClient != null && locationCallback != null && locationUpdatesStarted) {
            fusedLocationClient?.removeLocationUpdates(locationCallback!!)
        }
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 101
    }
}