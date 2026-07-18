package com.example.directdoctor

import android.graphics.Color
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth

class ChatAdapter(private val messages: List<ChatMessage>) : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    class ChatViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val llMessageRow: LinearLayout = view.findViewById(R.id.llMessageRow)
        val tvMessageBody: TextView = view.findViewById(R.id.tvMessageBody)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_message, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chatMsg = messages[position]

        // 🚀 ఇక్కడ chatMsg.message అని వాడాం, కాబట్టి Unresolved reference ఎర్రర్ రాదు!
        holder.tvMessageBody.text = chatMsg.message

        if (chatMsg.senderId == currentUserId) {
            // Mana message (Right side, Blue bubble)
            holder.llMessageRow.gravity = Gravity.END
            holder.tvMessageBody.setBackgroundResource(R.drawable.bg_chat_sender)
            holder.tvMessageBody.setTextColor(Color.WHITE)
        } else {
            // Doctor message (Left side, White bubble)
            holder.llMessageRow.gravity = Gravity.START
            holder.tvMessageBody.setBackgroundResource(R.drawable.bg_chat_receiver)
            holder.tvMessageBody.setTextColor(Color.parseColor("#333333"))
        }
    }

    override fun getItemCount(): Int = messages.size
}