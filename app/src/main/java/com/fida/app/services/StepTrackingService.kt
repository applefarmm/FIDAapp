package com.fida.app.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.fida.app.HomeActivity
import com.fida.app.R
import com.fida.app.utils.FirestoreRepository
import com.fida.app.utils.PreferenceHelper
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class StepTrackingService : Service(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var stepCounterSensor: Sensor? = null

    // Step counter sensor gives total steps since device boot
    // We need to track the "baseline" - steps at the start of each day
    private var baselineSteps = 0L  // Steps at midnight / app start
    private var currentTotalSteps = 0L  // Current sensor reading
    private var todaySteps = 0L  // Steps today (currentTotalSteps - baselineSteps)

    private val prefs by lazy { PreferenceHelper(this) }
    private val db by lazy { com.google.firebase.firestore.FirebaseFirestore.getInstance() }

    companion object {
        const val CHANNEL_ID = "step_tracking_channel"
        const val NOTIFICATION_ID = 1001
        const val ACTION_START = "com.fida.app.services.START_STEP_TRACKING"
        const val ACTION_STOP = "com.fida.app.services.STOP_STEP_TRACKING"

        fun start(context: Context) {
            val intent = Intent(context, StepTrackingService::class.java).apply {
                action = ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, StepTrackingService::class.java).apply {
                action = ACTION_STOP
            }
            context.stopService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        createNotificationChannel()
        loadBaseline()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopSelf()
                return START_NOT_STICKY
            }
        }

        // Start foreground service with notification
        startForeground(NOTIFICATION_ID, createNotification(todaySteps.toInt()))

        // Register sensor listener
        if (stepCounterSensor != null) {
            sensorManager.registerListener(this, stepCounterSensor, SensorManager.SENSOR_DELAY_NORMAL)
            Log.d("StepTrackingService", "Step counter sensor registered")
        } else {
            Log.w("StepTrackingService", "No step counter sensor available on this device")
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_STEP_COUNTER) {
            currentTotalSteps = event.values[0].toLong()

            // Check if baseline needs reset (new day)
            checkAndResetBaseline()

            // Calculate steps for today
            todaySteps = currentTotalSteps - baselineSteps

            Log.d("StepTrackingService", "Total: $currentTotalSteps, Baseline: $baselineSteps, Today: $todaySteps")

            // Save to Firestore
            saveStepsToFirestore(todaySteps)

            // Update notification
            updateNotification(todaySteps.toInt())

            // Save baseline to SharedPreferences for persistence
            saveBaseline()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not needed for step counter
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
        Log.d("StepTrackingService", "Service stopped")
    }

    private fun checkAndResetBaseline() {
        val today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val savedDate = prefs.getBaselineDate()

        if (savedDate != today) {
            // New day - reset baseline
            baselineSteps = currentTotalSteps
            prefs.saveBaselineDate(today)
            prefs.saveBaselineSteps(baselineSteps)
            Log.d("StepTrackingService", "New day detected - baseline reset to $baselineSteps")
        }
    }

    private fun loadBaseline() {
        val today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val savedDate = prefs.getBaselineDate()

        if (savedDate == today) {
            // Same day - use saved baseline
            baselineSteps = prefs.getBaselineSteps()
            Log.d("StepTrackingService", "Loaded baseline for today: $baselineSteps")
        } else {
            // New day or first run - baseline will be set when sensor gives first reading
            baselineSteps = 0L
            Log.d("StepTrackingService", "New day - baseline will be set on first sensor reading")
        }
    }

    private fun saveBaseline() {
        prefs.saveBaselineSteps(baselineSteps)
    }

    private fun saveStepsToFirestore(steps: Long) {
        val uid = prefs.getUid()
        if (uid.isNullOrEmpty()) return

        FirestoreRepository.logDailyActivity(uid, "stepCounter", steps.toInt())
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Step Tracking",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Tracks your daily steps"
                setShowBadge(false)
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(steps: Int): Notification {
        val intent = Intent(this, HomeActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val maxSteps = prefs.getMaxSteps()

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Step Tracking Active")
            .setContentText("$steps / $maxSteps steps today")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }

    private fun updateNotification(steps: Int) {
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID, createNotification(steps))
    }
}