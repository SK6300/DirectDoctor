package com.example.directdoctor

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class DoctorRegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_doctor_register)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Views linking
        val etFullName = findViewById<EditText>(R.id.etFullName)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etRegNumber = findViewById<EditText>(R.id.etRegNumber)
        val etMobile = findViewById<EditText>(R.id.etMobile)
        val spinnerSpecialisation = findViewById<Spinner>(R.id.spinnerSpecialisation)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnRegisterDoctor = findViewById<Button>(R.id.btnRegisterDoctor)

        // Setup Spinner Options
        val specialisations = arrayOf("Select Specialisation", "Cardiology", "Neurology", "Orthopedics", "Pediatrics", "Dermatology", "General Physician", "Ophthalmology", "Psychiatry", "Gastroenterology", "Endocrinology", "Rheumatology", "Oncology", "Radiology", "Anesthesiology", "Pulmonology", "Gynecology", "Nephrology", "Hematology", "Family Medicine", "Urology", "Obstetrics and Gynecology", "Pathology", "Allergy & Immunology", "Addiction Medicine", "Emergency Medicine", "Otolaryngology")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, specialisations)
        spinnerSpecialisation.adapter = adapter

        // Register Button Logic
        btnRegisterDoctor.setOnClickListener {
            val name = etFullName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val regNum = etRegNumber.text.toString().trim()
            val mobile = etMobile.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val specialisation = spinnerSpecialisation.selectedItem.toString()

            // Validations
            if (name.isEmpty() || email.isEmpty() || regNum.isEmpty() || mobile.isEmpty() || password.isEmpty() || specialisation == "Select Specialisation") {
                Toast.makeText(this, "Please fill all details correctly", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Create User in Firebase Auth
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val userId = auth.currentUser?.uid ?: ""

                        // Save extra data to Firestore
                        val doctorData = hashMapOf(
                            "fullName" to name,
                            "email" to email,
                            "registerNumber" to regNum,
                            "mobileNumber" to mobile,
                            "specialisation" to specialisation,
                            "role" to "Doctor"
                        )

                        db.collection("Doctors").document(userId)
                            .set(doctorData)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Doctor Registered Successfully!", Toast.LENGTH_LONG).show()
                                startActivity(Intent(this, MainActivity::class.java))
                                finish()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Database Error: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                    } else {
                        Toast.makeText(this, "Registration Failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }
}