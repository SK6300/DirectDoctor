package com.example.directdoctor

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toColorInt
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

@SuppressLint("SetTextI18n")
class LoginSignupActivity : AppCompatActivity() {

    private lateinit var tvFormTitle: TextView
    private lateinit var btnToggleLogin: Button
    private lateinit var btnToggleSignup: Button
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var tvForgotPassword: TextView
    private lateinit var btnFormAction: Button
    private lateinit var btnFormLink: Button

    private lateinit var auth: FirebaseAuth
    private var isLoginForm = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_signup)

        auth = Firebase.auth

        tvFormTitle = findViewById(R.id.tvFormTitle)
        btnToggleLogin = findViewById(R.id.btnToggleLogin)
        btnToggleSignup = findViewById(R.id.btnToggleSignup)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        tvForgotPassword = findViewById(R.id.tvForgotPassword)
        btnFormAction = findViewById(R.id.btnFormAction)
        btnFormLink = findViewById(R.id.btnFormLink)

        btnToggleLogin.setOnClickListener { showLoginForm() }
        btnToggleSignup.setOnClickListener { showSignupForm() }
        btnFormLink.setOnClickListener { if (isLoginForm) showSignupForm() else showLoginForm() }
        btnFormAction.setOnClickListener { handleFormAction() }
    }

    private fun showLoginForm() {
        isLoginForm = true
        tvFormTitle.text = "Login Form"

        // Warning fixed using toColorInt()
        btnToggleLogin.backgroundTintList = ColorStateList.valueOf("#014DA1".toColorInt())
        btnToggleLogin.setTextColor(Color.WHITE)

        btnToggleSignup.backgroundTintList = ColorStateList.valueOf("#F0F0F0".toColorInt())
        btnToggleSignup.setTextColor(Color.BLACK)

        etConfirmPassword.visibility = View.GONE
        tvForgotPassword.visibility = View.VISIBLE
        btnFormAction.text = "Login"
        btnFormLink.text = "Not a member? Signup now"
    }

    private fun showSignupForm() {
        isLoginForm = false
        tvFormTitle.text = "Signup Form"

        btnToggleLogin.backgroundTintList = ColorStateList.valueOf("#F0F0F0".toColorInt())
        btnToggleLogin.setTextColor(Color.BLACK)

        btnToggleSignup.backgroundTintList = ColorStateList.valueOf("#014DA1".toColorInt())
        btnToggleSignup.setTextColor(Color.WHITE)

        etConfirmPassword.visibility = View.VISIBLE
        tvForgotPassword.visibility = View.GONE
        btnFormAction.text = "Signup"
        btnFormLink.text = "Already a member? Login now"
    }

    private fun handleFormAction() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Email and Password raayi Light", Toast.LENGTH_SHORT).show()
            return
        }

        if (isLoginForm) {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Login Success!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this, "Login Failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        } else {
            val confirmPassword = etConfirmPassword.text.toString().trim()
            if (password != confirmPassword) {
                Toast.makeText(this, "Passwords match avvatledu", Toast.LENGTH_SHORT).show()
                return
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Signup Success!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this, "Signup Failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }
}