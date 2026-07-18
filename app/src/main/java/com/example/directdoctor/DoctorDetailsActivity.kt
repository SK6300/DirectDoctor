package com.example.directdoctor

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class DoctorDetailsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_doctor_details)

        // 🚀 Here is your Back Button Logic!
        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish() // This closes the page and takes you back exactly one step
        }

        val doctor = intent.getSerializableExtra("DOCTOR_DATA") as? Doctor ?: return

        val auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()

        val ivPic = findViewById<ImageView>(R.id.ivDetailPic)
        val tvName = findViewById<TextView>(R.id.tvDetailName)
        val tvSpec = findViewById<TextView>(R.id.tvDetailSpec)

        val tvMobile = findViewById<TextView>(R.id.tvDetailMobile)
        val tvFee = findViewById<TextView>(R.id.tvDetailFee)
        val tvClinic = findViewById<TextView>(R.id.tvDetailClinic)
        val tvAge = findViewById<TextView>(R.id.tvDetailAge)
        val tvGender = findViewById<TextView>(R.id.tvDetailGender)
        val tvAvailability = findViewById<TextView>(R.id.tvDetailAvailability)
        val tvBooked = findViewById<TextView>(R.id.tvDetailBooked)
        val tvAbout = findViewById<TextView>(R.id.tvDetailAbout)

        val llDynamicDetails = findViewById<LinearLayout>(R.id.llDynamicDetails)
        val btnPortfolio = findViewById<Button>(R.id.btnViewPortfolio)
        val btnWebsite = findViewById<Button>(R.id.btnViewWebsite)

        // Basic Info fast ga load cheyadaniki
        val nameDisplay = if (doctor.fullName.startsWith("Dr.", true)) doctor.fullName else "Dr. ${doctor.fullName}"
        tvName.text = nameDisplay
        tvSpec.text = "Specialization: ${doctor.specialisation.ifEmpty { "General Physician" }}"
        val tvConnectionHighlight = findViewById<TextView>(R.id.tvConnectionHighlight)
        val connectionStatus = intent.getStringExtra("CONNECTION_STATUS")

        if (!connectionStatus.isNullOrEmpty()) {
            tvConnectionHighlight.visibility = View.VISIBLE
            tvConnectionHighlight.text = "🤝 Connected: $connectionStatus"
        }
        tvMobile.text = "📞 Tap to Call: ${doctor.mobileNumber}"
        tvAge.text = "Age: ${doctor.age}"
        tvGender.text = "Gender: ${doctor.gender}"

        tvMobile.setOnClickListener {
            if (doctor.mobileNumber.isNotEmpty()) {
                startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${doctor.mobileNumber}")))
            }
        }

        fun formatUrl(url: String): String {
            val trimmed = url.trim()
            return if (!trimmed.startsWith("http://") && !trimmed.startsWith("https://")) {
                "https://$trimmed"
            } else trimmed
        }

        // 🚀 REAL-TIME FIRESTORE UPDATE LOGIC 🚀
        // Ikkada direct ga database nundi update thechukuntam
        db.collection("Doctors").document(doctor.id).get().addOnSuccessListener { doc ->
            if (doc.exists()) {
                val data = doc.data ?: return@addOnSuccessListener

                // 1. Profile Picture Update
                val imageUrl = data["profileImageUrl"]?.toString() ?: doctor.profileImageUrl
                if (imageUrl.isNotEmpty()) {
                    Glide.with(this).load(imageUrl).placeholder(R.drawable.doctor_profile).transform(CircleCrop()).into(ivPic)
                }

                // 2. Details visibility updates
                val fee = data["consultationFee"]?.toString() ?: doctor.consultationFee
                if (fee.isNotEmpty()) {
                    tvFee.visibility = View.VISIBLE
                    tvFee.text = "Consultation Fee: ₹$fee"
                }

                val clinic = data["clinicAddress"]?.toString() ?: doctor.clinicAddress
                if (clinic.isNotEmpty()) {
                    tvClinic.visibility = View.VISIBLE
                    tvClinic.text = "📍 Clinic Address:\n$clinic"
                }

                val availability = data["availabilityNotes"]?.toString() ?: doctor.availabilityNotes
                if (availability.isNotEmpty()) {
                    tvAvailability.visibility = View.VISIBLE
                    tvAvailability.text = "✅ Available Times:\n$availability"
                }

                val booked = data["bookedAppointments"]?.toString() ?: doctor.bookedAppointments
                if (booked.isNotEmpty()) {
                    tvBooked.visibility = View.VISIBLE
                    tvBooked.text = "❌ Booked Slots:\n$booked"
                }

                val about = data["aboutUs"]?.toString() ?: data["about"]?.toString() ?: doctor.aboutUs
                if (about.isNotEmpty()) {
                    tvAbout.visibility = View.VISIBLE
                    tvAbout.text = "About Us:\n$about"
                }

                // 🚀 3. PORTFOLIO & WEBSITE LINKS LOGIC
                // Firebase lo "portfolio" leda "portfolioUrl" yela unna thechukuntundi
                val freshPortfolio = data["portfolio"]?.toString() ?: data["portfolioUrl"]?.toString() ?: ""
                Toast.makeText(this@DoctorDetailsActivity, "Port: $freshPortfolio", Toast.LENGTH_LONG).show()
                if (freshPortfolio.isNotEmpty()) {
                    btnPortfolio.visibility = View.VISIBLE
                    btnPortfolio.setOnClickListener {
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(formatUrl(freshPortfolio))))
                    }
                }

                val freshWebsite = data["website"]?.toString() ?: data["websiteUrl"]?.toString() ?: ""
                if (freshWebsite.isNotEmpty()) {
                    btnWebsite.visibility = View.VISIBLE
                    btnWebsite.setOnClickListener {
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(formatUrl(freshWebsite))))
                    }
                }

                // 4. Dynamic fields logic (kotha fields emaina unte add cheyadaniki)
                val knownKeys = listOf("fullName", "specialisation", "mobileNumber", "age", "gender", "consultationFee", "clinicAddress", "availabilityNotes", "bookedAppointments", "aboutUs", "about", "profileImageUrl", "portfolio", "portfolioUrl", "website", "websiteUrl", "id", "password")

                llDynamicDetails.removeAllViews()
                for ((key, value) in data) {
                    val valueStr = value.toString().trim()
                    if (key !in knownKeys && valueStr.isNotEmpty()) {
                        val formattedKey = key.replace(Regex("([a-z])([A-Z]+)"), "$1 $2").capitalize()
                        val dynamicTextView = TextView(this@DoctorDetailsActivity).apply {
                            text = "$formattedKey: $valueStr"
                            textSize = 16f
                            setTextColor(android.graphics.Color.parseColor("#333333"))
                            setPadding(0, dpToPx(5), 0, dpToPx(5))
                        }
                        llDynamicDetails.addView(dynamicTextView)
                    }
                }
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to fetch latest details", Toast.LENGTH_SHORT).show()
        }

        val btnBookAppointment = findViewById<Button>(R.id.btnBookAppointment)

        btnBookAppointment.setOnClickListener {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                Toast.makeText(this, "Please Login to Book", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, LoginSignupActivity::class.java))
                return@setOnClickListener
            }

            val calendar = java.util.Calendar.getInstance()
            android.app.DatePickerDialog(this, { _, year, month, day ->
                val dateStr = "$day/${month + 1}/$year"

                android.app.TimePickerDialog(this, { _, hour, minute ->
                    val timeStr = String.format("%02d:%02d", hour, minute)

                    db.collection("Users").document(currentUser.uid).get().addOnSuccessListener { userDoc ->
                        val patientName = userDoc.getString("fullName") ?: "Patient"

                        val apptData = hashMapOf(
                            "doctorId" to doctor.id,
                            "doctorName" to doctor.fullName,
                            "doctorMobile" to doctor.mobileNumber,
                            "userId" to currentUser.uid,
                            "patientName" to patientName,
                            "dateTime" to "$dateStr at $timeStr",
                            "status" to "Booked"
                        )

                        db.collection("Appointments").add(apptData).addOnSuccessListener {
                            Toast.makeText(this, "Appointment Booked!", Toast.LENGTH_SHORT).show()
                        }
                    }
                }, calendar.get(java.util.Calendar.HOUR_OF_DAY), calendar.get(java.util.Calendar.MINUTE), false).show()

            }, calendar.get(java.util.Calendar.YEAR), calendar.get(java.util.Calendar.MONTH), calendar.get(java.util.Calendar.DAY_OF_MONTH)).show()
        }
    }

    private fun dpToPx(dp: Int): Int {
        val density = resources.displayMetrics.density
        return (dp * density).toInt()
    }
}