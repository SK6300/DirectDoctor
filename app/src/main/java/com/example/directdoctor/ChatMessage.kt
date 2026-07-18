package com.example.directdoctor

data class ChatMessage(
    val senderId: String = "",
    val message: String = "", // 🚀 ఇక్కడ 'message' అని పేరు పెట్టాం
    val timestamp: Long = 0L,
    val isRead: Boolean = false
)