package com.example.directdoctor

import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class DoctorChatListActivity : AppCompatActivity() {

    private lateinit var rvPatientChats: RecyclerView
    private val db = FirebaseFirestore.getInstance()
    private val currentDoctorId = FirebaseAuth.getInstance().currentUser?.uid

    private val requestList = mutableListOf<ChatRequest>()
    private lateinit var adapter: ChatRequestAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_doctor_chat_list)

        // It is perfectly placed right below setContentView.
        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish() // This closes the chat and takes you back exactly one step
        }

        rvPatientChats = findViewById(R.id.rvPatientChats)
        rvPatientChats.layoutManager = LinearLayoutManager(this)

        adapter = ChatRequestAdapter(requestList)
        rvPatientChats.adapter = adapter

        if (currentDoctorId != null) {
            fetchChatRequests()
        }
    }

    private fun fetchChatRequests() {
        // Appointments data nundi patients ni get chesthunnam
        db.collection("Appointments")
            .whereEqualTo("doctorId", currentDoctorId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                requestList.clear()
                val uniquePatients = mutableSetOf<String>()

                for (doc in querySnapshot.documents) {
                    val patientId = doc.getString("userId") ?: continue

                    // Patient name database lo ela save ayyindo check chey (userName / patientName)
                    val patientName = doc.getString("patientName") ?: doc.getString("userName") ?: "Unknown Patient"

                    // Duplicate patients rakunda logic
                    if (!uniquePatients.contains(patientId)) {
                        uniquePatients.add(patientId)
                        requestList.add(ChatRequest(patientId, patientName))
                    }
                }

                if (requestList.isEmpty()) {
                    Toast.makeText(this, "No chat requests yet!", Toast.LENGTH_SHORT).show()
                } else {
                    adapter.notifyDataSetChanged()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load chats", Toast.LENGTH_SHORT).show()
            }
    }
}