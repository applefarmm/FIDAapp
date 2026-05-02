package com.fida.app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.fida.app.fragments.TutorialFragment

class TutorialActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tutorial)
        supportActionBar?.hide()

        // If savedInstanceState is null, load the first tutorial step
        if (savedInstanceState == null) {
            loadFragment(TutorialFragment())
        }

        val btnNext = findViewById<Button>(R.id.btnTutorialNext)
        btnNext.setOnClickListener {
            // Logic to advance tutorial steps or finish and go to HomeActivity
            // For now, just go to HomeActivity
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.tutorialFragmentContainer, fragment)
            .commit()
    }
}
