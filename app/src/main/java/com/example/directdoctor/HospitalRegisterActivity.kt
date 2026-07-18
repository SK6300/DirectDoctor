package com.example.directdoctor

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class HospitalRegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hospital_register)

        val etName = findViewById<EditText>(R.id.etHospName)
        val etAddress = findViewById<EditText>(R.id.etHospAddress)
        val etLocation = findViewById<EditText>(R.id.etHospLocation)
        val etContact = findViewById<EditText>(R.id.etHospContact)
        val etWebsite = findViewById<EditText>(R.id.etHospWebsite)
        val etTimings = findViewById<EditText>(R.id.etHospTimings)
        val etFacilities = findViewById<EditText>(R.id.etHospFacilities)
        val etDoctorsList = findViewById<EditText>(R.id.etHospDoctorsList)
        val etReviews = findViewById<EditText>(R.id.etHospReviews)
        val btnSubmit = findViewById<Button>(R.id.btnSubmitHosp)

        btnSubmit.setOnClickListener {
            val name = etName.text.toString().trim()

            if (name.isNotEmpty()) {
                val db = FirebaseFirestore.getInstance()

                val hospData = hashMapOf(
                    "name" to name,
                    "address" to etAddress.text.toString().trim(),
                    "locationLink" to etLocation.text.toString().trim(),
                    "emergencyContact" to etContact.text.toString().trim(),
                    "websiteUrl" to etWebsite.text.toString().trim(),
                    "timings" to etTimings.text.toString().trim(),
                    "facilities" to etFacilities.text.toString().trim(),
                    "doctorsList" to etDoctorsList.text.toString().trim(),
                    "reviewsLink" to etReviews.text.toString().trim()
                )

                db.collection("Hospitals").add(hospData).addOnSuccessListener {
                    Toast.makeText(this, "Hospital Registered!", Toast.LENGTH_SHORT).show()
                    finish()
                }.addOnFailureListener {
                    Toast.makeText(this, "Failed to register hospital", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Hospital Name is required", Toast.LENGTH_SHORT).show()
            }
        }
    }
}