package com.example.directdoctor

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class DoctorDashboardActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var etConsultationFee: EditText
    private lateinit var etClinicAddress: EditText
    private lateinit var etAvailabilityNotes: EditText
    private lateinit var btnUpdateDetails: Button
    private lateinit var switchStatus: Switch
    private lateinit var llPatientAppointments: LinearLayout

    // 🚀 Chat & Badge Variables
    private lateinit var ivChatRequests: ImageView
    private lateinit var tvUnreadBadge: TextView
    private val unreadCountsMap = mutableMapOf<String, Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_doctor_dashboard)

        // 🚀 Here is your Back Button Logic!
        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish() // This closes the page and takes you back exactly one step
        }

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        etConsultationFee = findViewById(R.id.etConsultationFee)
        etClinicAddress = findViewById(R.id.etClinicAddress)
        etAvailabilityNotes = findViewById(R.id.etAvailabilityNotes)
        btnUpdateDetails = findViewById(R.id.btnUpdateDetails)
        switchStatus = findViewById(R.id.switchStatus)
        llPatientAppointments = findViewById(R.id.llPatientAppointments)

        // 🚀 Initialize Chat Icon & Badge
        ivChatRequests = findViewById(R.id.ivChatRequests)
        tvUnreadBadge = findViewById(R.id.tvUnreadBadge)

        loadDashboardData()
        loadLiveAppointments()
        listenForUnreadMessages() // 🚀 Call Notification Logic

        // 🚀 Chat Requests List Open
        ivChatRequests.setOnClickListener {
            val intent = Intent(this, DoctorChatListActivity::class.java)
            startActivity(intent)
        }

        btnUpdateDetails.setOnClickListener {
            saveDashboardData()
        }

        switchStatus.setOnCheckedChangeListener { _, isChecked ->
            val statusText = if (isChecked) "Online" else "Offline"
            switchStatus.text = statusText
            updateDoctorStatus(statusText)
        }
    }

    private fun loadDashboardData() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("Doctors").document(userId).get().addOnSuccessListener { doc ->
            if (doc.exists()) {
                etConsultationFee.setText(doc.getString("consultationFee") ?: "")
                etClinicAddress.setText(doc.getString("clinicAddress") ?: "")
                etAvailabilityNotes.setText(doc.getString("availabilityNotes") ?: "")

                val currentStatus = doc.getString("status") ?: "Offline"
                switchStatus.isChecked = currentStatus == "Online"
                switchStatus.text = currentStatus
            }
        }
    }

    private fun loadLiveAppointments() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("Appointments").whereEqualTo("doctorId", userId).get().addOnSuccessListener { docs ->
            llPatientAppointments.removeAllViews()
            if (docs.isEmpty) {
                llPatientAppointments.addView(TextView(this).apply { text = "No live bookings yet." })
                return@addOnSuccessListener
            }

            for (doc in docs) {
                val status = doc.getString("status") ?: "Booked"
                val patientName = doc.getString("patientName") ?: "Unknown Patient"
                val time = doc.getString("dateTime")

                val infoText = "👤 Patient: $patientName\n📅 $time\n📌 Status: $status"
                val tvPatient = TextView(this).apply {
                    text = infoText
                    textSize = 16f
                    setPadding(0, 10, 0, 10)
                }
                llPatientAppointments.addView(tvPatient)

                if (status == "Cancel Requested") {
                    val btnApprove = android.widget.Button(this).apply {
                        text = "Approve Cancellation"
                        setBackgroundColor(android.graphics.Color.parseColor("#FF9800"))
                        setTextColor(android.graphics.Color.WHITE)
                    }
                    btnApprove.setOnClickListener {
                        db.collection("Appointments").document(doc.id).update("status", "Cancelled")
                        Toast.makeText(this@DoctorDashboardActivity, "Cancellation Approved", Toast.LENGTH_SHORT).show()
                        tvPatient.text = infoText.replace("Status: Cancel Requested", "Status: Cancelled")
                        it.visibility = View.GONE
                    }
                    llPatientAppointments.addView(btnApprove)
                }

                val divider = View(this).apply {
                    layoutParams = android.widget.LinearLayout.LayoutParams(
                        android.widget.LinearLayout.LayoutParams.MATCH_PARENT, 2
                    ).apply { setMargins(0, 15, 0, 15) }
                    setBackgroundColor(android.graphics.Color.parseColor("#CCCCCC"))
                }
                llPatientAppointments.addView(divider)
            }
        }
    }

    private fun saveDashboardData() {
        val userId = auth.currentUser?.uid ?: return

        val updates = hashMapOf<String, Any>(
            "consultationFee" to etConsultationFee.text.toString().trim(),
            "clinicAddress" to etClinicAddress.text.toString().trim(),
            "availabilityNotes" to etAvailabilityNotes.text.toString().trim()
        )

        db.collection("Doctors").document(userId).update(updates)
            .addOnSuccessListener {
                Toast.makeText(this, "Details Updated Successfully!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                db.collection("Doctors").document(userId).set(updates, com.google.firebase.firestore.SetOptions.merge())
                    .addOnSuccessListener {
                        Toast.makeText(this, "Details Saved Successfully!", Toast.LENGTH_SHORT).show()
                    }
            }
    }

    private fun updateDoctorStatus(status: String) {
        val userId = auth.currentUser?.uid ?: return
        db.collection("Doctors").document(userId).update("status", status)
    }

    // 🚀 Notification Badge Logic Updated for simple dot visibility
    private fun listenForUnreadMessages() {
        val currentUserId = auth.currentUser?.uid ?: return

        db.collection("Appointments").whereEqualTo("doctorId", currentUserId).get()
            .addOnSuccessListener { docs ->
                docs.forEach { doc ->
                    val patientId = doc.getString("userId") ?: return@forEach
                    val roomId = if (currentUserId < patientId) "${currentUserId}_${patientId}" else "${patientId}_${currentUserId}"

                    db.collection("Chats").document(roomId).collection("Messages")
                        .whereEqualTo("isRead", false)
                        .addSnapshotListener { snapshot, _ ->
                            if (snapshot != null) {
                                // Only count unread messages sent by the patient
                                val count = snapshot.documents.count { it.getString("senderId") != currentUserId }
                                unreadCountsMap[roomId] = count

                                val totalUnread = unreadCountsMap.values.sum()
                                if (totalUnread > 0) {
                                    tvUnreadBadge.visibility = View.VISIBLE // 🚀 Just show the dot
                                } else {
                                    tvUnreadBadge.visibility = View.GONE // 🚀 Hide the dot
                                }
                            }
                        }
                }
            }
    }
}