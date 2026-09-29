package com.example.directdoctor

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class HospitalDetailsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hospital_details)

        val hospital = intent.getSerializableExtra("HOSPITAL_DATA") as? Hospital ?: return

        val tvName = findViewById<TextView>(R.id.tvHospDetailName)
        val tvAddress = findViewById<TextView>(R.id.tvHospDetailAddress)
        val tvContact = findViewById<TextView>(R.id.tvHospDetailContact)
        val tvTimings = findViewById<TextView>(R.id.tvHospDetailTimings)
        val tvFacilities = findViewById<TextView>(R.id.tvHospDetailFacilities)
        val tvDoctors = findViewById<TextView>(R.id.tvHospDetailDoctors)

        val btnLocation = findViewById<Button>(R.id.btnViewLocation)
        val btnWebsite = findViewById<Button>(R.id.btnViewHospWebsite)
        val btnReviews = findViewById<Button>(R.id.btnViewReviews)

        tvName.text = hospital.name
        tvAddress.text = "📍 Address:\n${hospital.address}"

        tvContact.text = "📞 Emergency: ${hospital.emergencyContact}"
        tvContact.setOnClickListener {
            if (hospital.emergencyContact.isNotEmpty()) {
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${hospital.emergencyContact}"))
                startActivity(intent)
            }
        }

        if (hospital.timings.isNotEmpty()) {
            tvTimings.text = "⏰ Timings: ${hospital.timings}"
        } else {
            tvTimings.visibility = View.GONE
        }

        if (hospital.facilities.isNotEmpty()) {
            tvFacilities.text = "🏥 Facilities:\n${hospital.facilities}"
        } else {
            tvFacilities.visibility = View.GONE
        }

        if (hospital.doctorsList.isNotEmpty()) {
            tvDoctors.text = "👨‍⚕️ Available Doctors/Departments:\n${hospital.doctorsList}"
        } else {
            tvDoctors.visibility = View.GONE
        }

        fun formatUrl(url: String): String {
            return if (!url.startsWith("http://") && !url.startsWith("https://")) {
                "https://$url"
            } else url
        }

        if (hospital.locationLink.isNotEmpty()) {
            btnLocation.visibility = View.VISIBLE
            btnLocation.setOnClickListener {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(formatUrl(hospital.locationLink))))
            }
        }

        if (hospital.websiteUrl.isNotEmpty()) {
            btnWebsite.visibility = View.VISIBLE
            btnWebsite.setOnClickListener {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(formatUrl(hospital.websiteUrl))))
            }
        }

        if (hospital.reviewsLink.isNotEmpty()) {
            btnReviews.visibility = View.VISIBLE
            btnReviews.setOnClickListener {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(formatUrl(hospital.reviewsLink))))
            }
        }
    }
}