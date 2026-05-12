package com.fida.app

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.fida.app.models.HealthProfile
import com.fida.app.utils.FirestoreRepository
import com.fida.app.utils.PreferenceHelper
import com.google.gson.Gson
import kotlin.math.pow

class HealthQuestionnaireActivity : AppCompatActivity() {

    private lateinit var prefs: PreferenceHelper
    private val uid: String by lazy {
        intent.getStringExtra("uid") ?: PreferenceHelper(this).getUid() ?: ""
    }

    // Screen 1: Basic Info
    private lateinit var etWeight: EditText
    private lateinit var etHeight: EditText
    private lateinit var etAge: EditText
    private lateinit var spinnerGender: Spinner

    // Screen 2: Health History
    private lateinit var etLastCheckup: EditText
    private lateinit var etChronicConditions: EditText
    private lateinit var etMedications: EditText
    private lateinit var etAllergies: EditText

    // Screen 3: Lifestyle
    private lateinit var etSleepHours: EditText
    private lateinit var seekBarStress: SeekBar
    private lateinit var tvStressLevel: TextView
    private lateinit var rgSmoking: RadioGroup
    private lateinit var rgAlcohol: RadioGroup
    private lateinit var rgShortness: RadioGroup

    // Screen 4: Fitness
    private lateinit var spinnerActivityLevel: Spinner
    private lateinit var etInjuries: EditText
    private lateinit var etFitnessGoals: EditText

    // Navigation
    private lateinit var btnNext: Button
    private lateinit var btnPrev: Button
    private lateinit var btnSubmit: Button
    private var currentScreen = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_health_questionnaire)
        supportActionBar?.hide()

        prefs = PreferenceHelper(this)
        initializeViews()
        showScreen(1)
    }

    private fun initializeViews() {
        // Screen 1
        etWeight = findViewById(R.id.etWeight)
        etHeight = findViewById(R.id.etHeight)
        etAge = findViewById(R.id.etAge)
        spinnerGender = findViewById(R.id.spinnerGender)

        // Screen 2
        etLastCheckup = findViewById(R.id.etLastCheckup)
        etChronicConditions = findViewById(R.id.etChronicConditions)
        etMedications = findViewById(R.id.etMedications)
        etAllergies = findViewById(R.id.etAllergies)

        // Screen 3
        etSleepHours = findViewById(R.id.etSleepHours)
        seekBarStress = findViewById(R.id.seekBarStress)
        tvStressLevel = findViewById(R.id.tvStressLevel)
        rgSmoking = findViewById(R.id.rgSmoking)
        rgAlcohol = findViewById(R.id.rgAlcohol)
        rgShortness = findViewById(R.id.rgShortness)

        // Screen 4
        spinnerActivityLevel = findViewById(R.id.spinnerActivityLevel)
        etInjuries = findViewById(R.id.etInjuries)
        etFitnessGoals = findViewById(R.id.etFitnessGoals)

        // Navigation
        btnNext = findViewById(R.id.btnNext)
        btnPrev = findViewById(R.id.btnPrev)
        btnSubmit = findViewById(R.id.btnSubmit)

        btnNext.setOnClickListener { nextScreen() }
        btnPrev.setOnClickListener { prevScreen() }
        btnSubmit.setOnClickListener { submitQuestionnaire() }

        seekBarStress.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                tvStressLevel.text = "Stress Level: $progress/10"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun showScreen(screen: Int) {
        currentScreen = screen
        val screen1 = findViewById<android.view.ViewGroup>(R.id.screen1)
        val screen2 = findViewById<android.view.ViewGroup>(R.id.screen2)
        val screen3 = findViewById<android.view.ViewGroup>(R.id.screen3)
        val screen4 = findViewById<android.view.ViewGroup>(R.id.screen4)

        screen1.visibility = if (screen == 1) android.view.View.VISIBLE else android.view.View.GONE
        screen2.visibility = if (screen == 2) android.view.View.VISIBLE else android.view.View.GONE
        screen3.visibility = if (screen == 3) android.view.View.VISIBLE else android.view.View.GONE
        screen4.visibility = if (screen == 4) android.view.View.VISIBLE else android.view.View.GONE

        btnPrev.isEnabled = screen > 1
        btnNext.visibility = if (screen < 4) android.view.View.VISIBLE else android.view.View.GONE
        btnSubmit.visibility = if (screen == 4) android.view.View.VISIBLE else android.view.View.GONE
    }

    private fun nextScreen() {
        if (validateCurrentScreen()) {
            showScreen(currentScreen + 1)
        }
    }

    private fun prevScreen() {
        showScreen(currentScreen - 1)
    }

    private fun validateCurrentScreen(): Boolean {
        return when (currentScreen) {
            1 -> {
                val weight = etWeight.text.toString().trim()
                val height = etHeight.text.toString().trim()
                val age = etAge.text.toString().trim()

                if (weight.isEmpty() || height.isEmpty() || age.isEmpty()) {
                    Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                    return false
                }
                true
            }

            2 -> true // Optional fields
            3 -> {
                val sleep = etSleepHours.text.toString().trim()
                if (sleep.isEmpty()) {
                    Toast.makeText(this, "Please enter sleep hours", Toast.LENGTH_SHORT).show()
                    return false
                }
                true
            }

            4 -> true // Optional fields
            else -> false
        }
    }

    private fun submitQuestionnaire() {
        if (!validateCurrentScreen()) return

        val weight = etWeight.text.toString().toFloatOrNull() ?: 0f
        val height = etHeight.text.toString().toFloatOrNull() ?: 0f
        val age = etAge.text.toString().toIntOrNull() ?: 0
        val gender = spinnerGender.selectedItem.toString()

        val bmi = calculateBMI(weight, height)

        val profile = HealthProfile(
            uid = uid,
            lastUpdated = System.currentTimeMillis(),
            weight = weight,
            height = height,
            age = age,
            gender = gender,
            bmi = bmi,
            lastCheckupDate = etLastCheckup.text.toString(),
            chronicConditions = etChronicConditions.text.toString().split(",").map { it.trim() },
            medications = etMedications.text.toString().split(",").map { it.trim() },
            allergies = etAllergies.text.toString().split(",").map { it.trim() },
            sleepHours = etSleepHours.text.toString().toIntOrNull() ?: 7,
            stressLevel = seekBarStress.progress,
            smokingStatus = getSmoking(),
            alcoholFrequency = getAlcohol(),
            shortnessOfBreath = rgShortness.checkedRadioButtonId == R.id.rbShortYes,
            activityLevel = spinnerActivityLevel.selectedItem.toString(),
            injuries = etInjuries.text.toString().split(",").map { it.trim() },
            fitnessGoals = etFitnessGoals.text.toString().split(",").map { it.trim() }
        )

        saveProfile(profile)
    }

    private fun calculateBMI(weight: Float, height: Float): Float {
        if (height == 0f) return 0f
        val heightM = height / 100
        return weight / (heightM.pow(2))
    }

    private fun getSmoking(): String {
        return when (rgSmoking.checkedRadioButtonId) {
            R.id.rbSmokingNever -> "never"
            R.id.rbSmokingFormer -> "former"
            R.id.rbSmokingCurrent -> "current"
            else -> "unknown"
        }
    }

    private fun getAlcohol(): String {
        return when (rgAlcohol.checkedRadioButtonId) {
            R.id.rbAlcoholNever -> "never"
            R.id.rbAlcoholRarely -> "rarely"
            R.id.rbAlcoholOften -> "often"
            else -> "unknown"
        }
    }

    private fun saveProfile(profile: HealthProfile) {
        val gson = Gson()
        val profileJson = gson.toJson(profile)

        // Save to SharedPreferences
        prefs.saveHealthProfile(uid, profileJson)
        prefs.saveHealthProfileUpdateTime(uid)

        // Save to Firestore
        FirestoreRepository.saveHealthProfile(uid, profile) { success ->
            if (success) {
                Toast.makeText(this, "Health profile saved!", Toast.LENGTH_SHORT).show()
                setResult(RESULT_OK)
                finish()
            } else {
                Toast.makeText(this, "Error saving profile", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
