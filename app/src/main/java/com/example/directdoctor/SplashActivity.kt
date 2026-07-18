package com.example.directdoctor

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AlphaAnimation
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val logo = findViewById<ImageView>(R.id.fullLogo)

        // Manam paatha file lo vadina exact Fade-In effect
        val fadeIn = AlphaAnimation(0f, 1f)
        fadeIn.duration = 2000
        logo.startAnimation(fadeIn)

        // SplashActivity.kt lo delay ni 3000 nunchi 2000 ki thaggisthe load fast ga avtundi
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, 2000)
    }
}