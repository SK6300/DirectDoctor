package com.example.directdoctor

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UserDataActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    // 🚀 Badge Variables
    private lateinit var tvProfileUnreadBadge: TextView
    private val unreadCountsMap = mutableMapOf<String, Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_data)

        // 🚀 Here is your Back Button Logic!
        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish() // This closes the page and takes you back exactly one step
        }

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val cardAppointments = findViewById<CardView>(R.id.cardAppointments)
        val llAppointmentsContainer = findViewById<android.widget.LinearLayout>(R.id.llAppointmentsContainer)
        val cardConnectedDoctors = findViewById<CardView>(R.id.cardConnectedDoctors)

        // 🚀 Initialize the Badge ID (Make sure this ID exists in your activity_user_data.xml)
        tvProfileUnreadBadge = findViewById(R.id.tvProfileUnreadBadge)

        // 🚀 Call the Notification Logic
        listenForUnreadMessages()

        // 🚀 Connected Doctors Card Click Logic
        cardConnectedDoctors.setOnClickListener {
            val intent = Intent(this, ConnectedDoctorsActivity::class.java)
            startActivity(intent)
        }

        // 🚀 Appointments Logic
        cardAppointments.setOnClickListener {
            if (llAppointmentsContainer.visibility == View.VISIBLE) {
                llAppointmentsContainer.visibility = View.GONE
            } else {
                llAppointmentsContainer.visibility = View.VISIBLE
                llAppointmentsContainer.removeAllViews() // Clear old data

                val userId = auth.currentUser?.uid ?: return@setOnClickListener
                db.collection("Appointments").whereEqualTo("userId", userId).get()
                    .addOnSuccessListener { docs ->
                        if (docs.isEmpty) {
                            val tvEmpty = TextView(this).apply { text = "No appointments booked yet." }
                            llAppointmentsContainer.addView(tvEmpty)
                        }

                        for (doc in docs) {
                            val status = doc.getString("status") ?: "Booked"
                            val docName = doc.getString("doctorName")
                            val docMobile = doc.getString("doctorMobile") ?: "N/A"
                            val time = doc.getString("dateTime")

                            val apptText = "🩺 Dr. $docName\n📞 Contact: $docMobile\n📅 $time\n📌 Status: $status"

                            val tvAppt = TextView(this).apply {
                                text = apptText
                                textSize = 16f
                                setPadding(0, 10, 0, 10)
                                setTextColor(android.graphics.Color.parseColor("#333333"))
                            }
                            llAppointmentsContainer.addView(tvAppt)

                            // Show Cancel Button only if it's currently "Booked"
                            if (status == "Booked") {
                                val btnCancel = android.widget.Button(this).apply {
                                    text = "Request Cancellation"
                                    setBackgroundColor(android.graphics.Color.parseColor("#D32F2F"))
                                    setTextColor(android.graphics.Color.WHITE)
                                }
                                btnCancel.setOnClickListener {
                                    db.collection("Appointments").document(doc.id).update("status", "Cancel Requested")
                                    Toast.makeText(this@UserDataActivity, "Cancellation Requested!", Toast.LENGTH_SHORT).show()
                                    tvAppt.text = apptText.replace("Status: Booked", "Status: Cancel Requested")
                                    it.visibility = View.GONE // Hide button after clicking
                                }
                                llAppointmentsContainer.addView(btnCancel)
                            }

                            // Add a divider line
                            val divider = View(this).apply {
                                layoutParams = android.widget.LinearLayout.LayoutParams(
                                    android.widget.LinearLayout.LayoutParams.MATCH_PARENT, 2
                                ).apply { setMargins(0, 15, 0, 15) }
                                setBackgroundColor(android.graphics.Color.parseColor("#CCCCCC"))
                            }
                            llAppointmentsContainer.addView(divider)
                        }
                    }
            }
        }
    }

    // 🚀 User Notification Badge Logic
    private fun listenForUnreadMessages() {
        val currentUserId = auth.currentUser?.uid ?: return

        // Fetch all appointments for THIS user
        db.collection("Appointments").whereEqualTo("userId", currentUserId).get()
            .addOnSuccessListener { docs ->
                docs.forEach { doc ->
                    val doctorId = doc.getString("doctorId") ?: return@forEach
                    // Create roomId exactly as we do in ChatActivity
                    val roomId = if (currentUserId < doctorId) "${currentUserId}_${doctorId}" else "${doctorId}_${currentUserId}"

                    // Listen for messages in this room
                    db.collection("Chats").document(roomId).collection("Messages")
                        .whereEqualTo("isRead", false)
                        .addSnapshotListener { snapshot, _ ->
                            if (snapshot != null) {
                                // Count messages sent by the DOCTOR that are still unread
                                val count = snapshot.documents.count { it.getString("senderId") != currentUserId }
                                unreadCountsMap[roomId] = count

                                // Sum up unread messages across all connected doctors
                                val totalUnread = unreadCountsMap.values.sum()
                                if (totalUnread > 0) {
                                    tvProfileUnreadBadge.visibility = View.VISIBLE // Show the red dot
                                } else {
                                    tvProfileUnreadBadge.visibility = View.GONE // Hide the red dot
                                }
                            }
                        }
                }
            }
    }
}