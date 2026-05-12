package com.fida.app

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.fida.app.utils.PreferenceHelper
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class MainActivity : AppCompatActivity(), SensorEventListener {
    private val db = Firebase.firestore

    private val requestPermission =

        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                Log.i("DEBUG", "permission granted")
            } else {
                Log.i("DEBUG", "permission denied")
            }
        }

    private lateinit var toggle: ActionBarDrawerToggle

    private var sensorManager: SensorManager? = null

    private var running = false
    private var totalSteps = 0f
    private var previousTotalSteps = 0f
    private var maxStep = 0
    private var maxStepAchieved = false
    private var waterCounter = 0
    private var maxWater = 0
    private var sleepTime = 0f

    private var healthPoint = 100
    private var expPoint = 0
    private var maxExpPoint = 100
    private var level = 0
    private var coin = 0

    // Firestore doc ID — use uid from SharedPreferences, NOT display username
    private val safeUid: String by lazy {
        PreferenceHelper(this).getUid() ?: ""
    }

    // Display name for UI only
    private val safeUsername: String by lazy {
        intent.getStringExtra("username")
            ?: PreferenceHelper(this).getUsername()
            ?: "User"
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestPermission.launch(android.Manifest.permission.ACTIVITY_RECOGNITION)

        val drawerLayout = findViewById<DrawerLayout>(R.id.drawerLayout)
        toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.elevation = 0F

        val navView = findViewById<NavigationView>(R.id.navView)

        navView.setNavigationItemSelectedListener {
            when(it.itemId) {
                R.id.miItem1 -> Toast.makeText(applicationContext,
                    "Coming soon!", Toast.LENGTH_SHORT).show()
                R.id.miItem2 -> {
                    val intent = Intent(this@MainActivity, ShopActivity::class.java)
                    intent.putExtra("uid", safeUid)
                    startActivity(intent)
                }
                R.id.miItem3 -> Toast.makeText(applicationContext,
                    "Coming soon!", Toast.LENGTH_SHORT).show()
            }
            true
        }

        val header: View = navView.getHeaderView(0)
        val usernameNavHeader: TextView = header.findViewById(R.id.username_navHeader)
        usernameNavHeader.text = safeUsername

        val stepCardView = findViewById<com.google.android.material.card.MaterialCardView>(R.id.stepCardView)
        stepCardView.setOnClickListener {
            val intent = Intent(this@MainActivity, StepCounterActivity::class.java)
            intent.putExtra("uid", safeUid)
            startActivity(intent)
        }
        val waterCardView = findViewById<com.google.android.material.card.MaterialCardView>(R.id.waterCardView)
        waterCardView.setOnClickListener {
            val intent = Intent(this@MainActivity, WaterIntakeActivity::class.java)
            intent.putExtra("uid", safeUid)
            startActivity(intent)
        }
        val sleepCardView = findViewById<com.google.android.material.card.MaterialCardView>(R.id.sleepCardView)
        sleepCardView.setOnClickListener {
            val intent = Intent(this@MainActivity, RecordSleepActivity::class.java)
            intent.putExtra("uid", safeUid)
            startActivity(intent)
        }

        val sleepButton = findViewById<Button>(R.id.recordSleepButton)
        sleepButton.setOnClickListener {
            val intent = Intent(this@MainActivity, RecordSleepActivity::class.java)
            intent.putExtra("uid", safeUid)
            startActivity(intent)
        }

        loadData(safeUid)
        changeWaterCounter(safeUid)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
    }

    override fun onStart() {
        super.onStart()
        loadData(safeUid)
        updateToDate(safeUid)
    }

    override fun onStop() {
        super.onStop()
        updateToDate(safeUid)
        checkStepGoalAchieved()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        running = true

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val stepSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        if (stepSensor == null) {
            Toast.makeText(this, "No sensor detected on this device", Toast.LENGTH_SHORT).show()
        }
        else {
            sensorManager?.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        val stepsTaken : TextView = findViewById(R.id.stepsTaken)
        val stepProgressBar : com.google.android.material.progressindicator.LinearProgressIndicator = findViewById(R.id.stepProgressBar)

        if (running) {
            totalSteps = event!!.values[0]
            val currentSteps = totalSteps.toInt() - previousTotalSteps.toInt()

            stepsTaken.text = ("$currentSteps")

            stepProgressBar.setProgressCompat(currentSteps, true)
        }
    }

    private fun changeWaterCounter(uid: String) {
        val waterCounterTextView : TextView = findViewById(R.id.waterTaken)
        val addWaterButton : Button = findViewById(R.id.addWater)
        val removeWaterButton : Button = findViewById(R.id.removeWater)

        addWaterButton.setOnClickListener {
            waterCounter++
            waterCounterTextView.text = waterCounter.toString()

            incrementExp(20)
            updateWaterCounter(uid)
        }

        removeWaterButton.setOnClickListener {
            if (waterCounter > 0 && expPoint > 0) {
                waterCounter--
                waterCounterTextView.text = waterCounter.toString()

                incrementExp(-20)
                updateWaterCounter(uid)
            }
        }
    }

    private fun resetStepsForLongClick() {
        val stepsTaken : TextView = findViewById(R.id.stepsTaken)
        val stepProgressBar : com.google.android.material.progressindicator.LinearProgressIndicator = findViewById(R.id.stepProgressBar)

        stepProgressBar.setOnClickListener {
            Toast.makeText(this@MainActivity, "Long tap to reset step progress", Toast.LENGTH_SHORT).show()
        }

        stepProgressBar.setOnLongClickListener {
            previousTotalSteps = totalSteps
            stepsTaken.text = 0.toString()
            stepProgressBar.setProgressCompat(0, true)
            saveData()
            true
        }
    }
    private fun resetSteps() {
        val stepsTaken : TextView = findViewById(R.id.stepsTaken)

        previousTotalSteps = totalSteps
        stepsTaken.text = 0.toString()
        saveData()
    }

    private fun saveData() {
        val sharedPreferences = getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putFloat("previousTotalSteps", previousTotalSteps)
        editor.apply()
    }

    // Load All User Info — uses uid (Firebase document ID), NOT username
    private fun loadData(uid: String) {
        if (uid.isEmpty()) return

        val currentTime = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val sharedPreferences = getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        val savedNumber = sharedPreferences.getFloat("previousTotalSteps", 0f)
        Log.d("MainActivity", "$savedNumber")
        previousTotalSteps = savedNumber

        val userRef = db.collection("users").document(uid)

        val waterCounterTextView : TextView = findViewById(R.id.waterTaken)
        val currentHealthTextView : TextView = findViewById(R.id.currentHealth)
        val currentExpTextView : TextView = findViewById(R.id.currentExp)
        val maxExpTextView : TextView = findViewById(R.id.maxExp)
        val levelTextView : TextView = findViewById(R.id.level)
        val coinTextView : TextView = findViewById(R.id.coin)
        val maxStepTextView : TextView = findViewById(R.id.maxStep)
        val maxWaterTextView: TextView = findViewById(R.id.maxWater)
        val healthBar : com.google.android.material.progressindicator.LinearProgressIndicator = findViewById(R.id.healthBar)
        val expBar : com.google.android.material.progressindicator.LinearProgressIndicator = findViewById(R.id.expBar)
        val stepProgressBar : com.google.android.material.progressindicator.LinearProgressIndicator = findViewById(R.id.stepProgressBar)

        userRef.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val documentSnapshot = task.result
                if (documentSnapshot != null && documentSnapshot.exists()) {
                    val data = documentSnapshot.data
                    if (data != null) {
                        @Suppress("UNCHECKED_CAST")
                        val allDays = data["allDays"] as? Map<String, Any>
                        val todayData = allDays?.get(currentTime) as? Map<String, Any>

                        if (todayData != null) {
                            waterCounter = (todayData["waterCounter"] as? Long)?.toInt() ?: 0
                            waterCounterTextView.text = waterCounter.toString()

                            maxStepAchieved = todayData["maxStepAchieved"] as? Boolean ?: false
                            sleepTime = todayData["sleepTime"]?.toString()?.toFloatOrNull() ?: 0f
                        }

                        healthPoint = (data["health"] as? Long)?.toInt() ?: 100
                        currentHealthTextView.text = healthPoint.toString()
                        healthBar.setProgressCompat(healthPoint, true)

                        expPoint = (data["exp"] as? Long)?.toInt() ?: 0
                        currentExpTextView.text = expPoint.toString()
                        expBar.setProgressCompat(expPoint, true)

                        maxExpPoint = (data["maxExp"] as? Long)?.toInt() ?: 100
                        maxExpTextView.text = maxExpPoint.toString()
                        expBar.max = maxExpPoint

                        level = (data["level"] as? Long)?.toInt() ?: 1
                        levelTextView.text = level.toString()

                        coin = (data["coin"] as? Long)?.toInt() ?: 0
                        coinTextView.text = coin.toString()

                        maxStep = (data["maxStep"] as? Long)?.toInt() ?: 2500
                        maxStepTextView.text = maxStep.toString()
                        stepProgressBar.max = maxStep

                        maxWater = (data["maxWater"] as? Long)?.toInt() ?: 8
                        maxWaterTextView.text = maxWater.toString()
                    }
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    // updateToDate — uses uid for Firestore, not username
    private fun updateToDate(uid: String) {
        if (uid.isEmpty()) return

        val currentTime = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

        val userRef = db.collection("users").document(uid)
        userRef.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val documentSnapshot = task.result
                if (documentSnapshot != null && documentSnapshot.exists()) {
                    val data = documentSnapshot.data
                    if (data != null) {
                        @Suppress("UNCHECKED_CAST")
                        val allDays = data["allDays"] as? Map<String, Any>

                        if (allDays == null || !allDays.containsKey(currentTime)) {
                            resetSteps()
                        }
                    }
                }
            }
            userRef
                .update(
                    "coin", coin,
                    "exp", expPoint,
                    "health", healthPoint,
                    "level", level,
                    "maxExp", maxExpPoint,
                    "maxStep", maxStep,
                    "maxWater", maxWater,
                    "allDays.${currentTime}.stepCounter", totalSteps - previousTotalSteps,
                    "allDays.${currentTime}.waterCounter", waterCounter,
                    "allDays.${currentTime}.maxStepAchieved", maxStepAchieved,
                    "allDays.${currentTime}.sleepTime", sleepTime
                )
                .addOnSuccessListener {
                    Log.d("MainActivity", "Data saved for $currentTime")
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        this@MainActivity,
                        "Error while saving data",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.d(ContentValues.TAG, e.toString())
                }
        }
    }

    private fun incrementExp(expPointIncrement : Int) {
        val currentExpTextView : TextView = findViewById(R.id.currentExp)
        val expBar : com.google.android.material.progressindicator.LinearProgressIndicator = findViewById(R.id.expBar)

        expPoint += expPointIncrement
        if (expPoint >= maxExpPoint) {
            levelUp()
        }
        else {
            currentExpTextView.text = expPoint.toString()
            expBar.setProgressCompat(expPoint, true)
        }
    }
    private fun levelUp() {
        level++
        expPoint = 0
        maxExpPoint += 50 * (level - 1)
        coin += 50

        val levelTextView : TextView = findViewById(R.id.level)
        val currentExpTextView : TextView = findViewById(R.id.currentExp)
        val maxExpTextView : TextView = findViewById(R.id.maxExp)
        val coinTextView : TextView = findViewById(R.id.coin)
        val expBar : com.google.android.material.progressindicator.LinearProgressIndicator = findViewById(R.id.expBar)

        levelTextView.text = level.toString()
        currentExpTextView.text = expPoint.toString()
        maxExpTextView.text = maxExpPoint.toString()
        coinTextView.text = coin.toString()
        expBar.setProgressCompat(expPoint, true)
        expBar.max = maxExpPoint

        Toast.makeText(this@MainActivity, "Level Up!", Toast.LENGTH_SHORT).show()
        updateToDate(safeUid)
    }

    private fun checkStepGoalAchieved() {
        if ((totalSteps - previousTotalSteps) >= maxStep && !maxStepAchieved) {
            incrementExp(100)
            maxStepAchieved = true
            updateToDate(safeUid)
            Toast.makeText(this@MainActivity, "Step Goal Achieved!", Toast.LENGTH_SHORT).show()
        }
    }
    private fun updateWaterCounter(uid: String) {
        if (uid.isEmpty()) return

        val currentTime = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

        val userRef = db.collection("users").document(uid)
        userRef.get().addOnCompleteListener {
            userRef
                .update(
                    "allDays.${currentTime}.waterCounter", waterCounter,
                )
                .addOnFailureListener { e ->
                    Toast.makeText(
                        this@MainActivity,
                        "Error while updating water",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.d(ContentValues.TAG, e.toString())
                }
        }
    }
}