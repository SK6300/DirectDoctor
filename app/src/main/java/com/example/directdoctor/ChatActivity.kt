package com.example.directdoctor

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView // 🚀 Added this import for the back button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ChatActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: ChatAdapter
    private val messagesList = mutableListOf<ChatMessage>()
    private var roomId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        // 🚀 Here is your Back Button Logic!
        // It is perfectly placed right below setContentView.
        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish() // This closes the chat and takes you back exactly one step
        }

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val currentUserId = auth.currentUser?.uid ?: return

        // Intent nundi data thechukovadam
        val otherUserId = intent.getStringExtra("OTHER_USER_ID") ?: return
        val otherUserName = intent.getStringExtra("OTHER_USER_NAME") ?: "User"
        val isDoctorApp = intent.getBooleanExtra("IS_DOCTOR_APP", false)

        val tvChatDocName = findViewById<TextView>(R.id.tvChatDocName)
        val etMessageInput = findViewById<EditText>(R.id.etMessageInput)
        val btnSendMessage = findViewById<Button>(R.id.btnSendMessage)
        val rvChatMessages = findViewById<RecyclerView>(R.id.rvChatMessages)

        // 🚀 Dynamic Header Logic
        if (isDoctorApp) {
            tvChatDocName.text = "Direct chat with $otherUserName"
        } else {
            tvChatDocName.text = "Direct chat with Dr. $otherUserName"
        }

        // Room ID Creation (Iddari IDs kalipi unique room create chestham)
        roomId = if (currentUserId < otherUserId) {
            "${currentUserId}_${otherUserId}"
        } else {
            "${otherUserId}_${currentUserId}"
        }

        adapter = ChatAdapter(messagesList)
        rvChatMessages.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true // Latest message kinda nundi start avvali
        }
        rvChatMessages.adapter = adapter

        // 🚀 Keyboard open ainappudu chat auto-scroll avvadaniki
        rvChatMessages.addOnLayoutChangeListener { _, _, _, _, bottom, _, _, _, oldBottom ->
            if (bottom < oldBottom) {
                rvChatMessages.post {
                    if (messagesList.isNotEmpty()) {
                        rvChatMessages.smoothScrollToPosition(messagesList.size - 1)
                    }
                }
            }
        }

        // 🚀 Real-time Live Messages Fetch & Mark as Read
        db.collection("Chats").document(roomId).collection("Messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener

                messagesList.clear()
                snapshot?.documents?.forEach { doc ->
                    val msg = doc.toObject(ChatMessage::class.java)
                    if (msg != null) {
                        messagesList.add(msg)

                        // 🚀 If the message is from the OTHER person and is unread, mark it as read
                        if (msg.senderId != currentUserId && !msg.isRead) {
                            doc.reference.update("isRead", true)
                        }
                    }
                }
                adapter.notifyDataSetChanged()
                if (messagesList.isNotEmpty()) {
                    rvChatMessages.scrollToPosition(messagesList.size - 1)
                }
            }

        // 🚀 Send Message Logic (Saves isRead as false)
        btnSendMessage.setOnClickListener {
            val msgText = etMessageInput.text.toString().trim()
            if (msgText.isNotEmpty()) {
                val newMsg = ChatMessage(currentUserId, msgText, System.currentTimeMillis(), false)

                db.collection("Chats").document(roomId).collection("Messages")
                    .add(newMsg)
                    .addOnSuccessListener { etMessageInput.text.clear() }
            }
        }
    }
}