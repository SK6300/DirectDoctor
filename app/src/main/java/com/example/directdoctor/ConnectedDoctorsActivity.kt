package com.example.directdoctor

import android.os.Bundle
import android.widget.ImageView // 🚀 Added this for the back button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ConnectedDoctorsActivity : AppCompatActivity() {

    private lateinit var rvConnectedDoctors: RecyclerView
    private lateinit var adapter: ConnectedDoctorsAdapter
    private val connectedList = mutableListOf<ConnectedDoctor>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_connected_doctors) // Linking the new layout

        // 🚀 Here is your Back Button Logic!
        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish() // This closes the page and takes you back exactly one step
        }

        rvConnectedDoctors = findViewById(R.id.rvConnectedDoctors)
        rvConnectedDoctors.layoutManager = LinearLayoutManager(this)

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Toast.makeText(this, "Please Login First", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        fetchConnectedDoctors(currentUser.uid)
    }

    private fun fetchConnectedDoctors(userId: String) {
        val db = FirebaseFirestore.getInstance()

        // Mundu Appointments check chesthunnam, future lo separate "Connections" table vaadochu
        db.collection("Appointments")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val uniqueDoctorIds = mutableSetOf<String>()

                for (doc in querySnapshot) {
                    val doctorId = doc.getString("doctorId") ?: continue
                    val status = doc.getString("status") ?: "Appointment Booked"

                    if (!uniqueDoctorIds.contains(doctorId)) {
                        uniqueDoctorIds.add(doctorId)

                        // Doctor full info kosam inko call
                        db.collection("Doctors").document(doctorId).get().addOnSuccessListener { docSnapshot ->
                            if (docSnapshot.exists()) {
                                val name = docSnapshot.getString("fullName") ?: "Unknown"
                                val spec = docSnapshot.getString("specialisation") ?: ""
                                val img = docSnapshot.getString("profileImageUrl") ?: ""

                                connectedList.add(ConnectedDoctor(doctorId, name, spec, img, status))

                                if (::adapter.isInitialized) {
                                    adapter.notifyDataSetChanged()
                                } else {
                                    adapter = ConnectedDoctorsAdapter(connectedList)
                                    rvConnectedDoctors.adapter = adapter
                                }
                            }
                        }
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load connections", Toast.LENGTH_SHORT).show()
            }
    }
}