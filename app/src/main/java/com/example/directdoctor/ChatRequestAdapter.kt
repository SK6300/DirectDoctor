package com.example.directdoctor

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

// Simple model for holding patient details
data class ChatRequest(val patientId: String, val patientName: String)

class ChatRequestAdapter(private val requests: List<ChatRequest>) : RecyclerView.Adapter<ChatRequestAdapter.RequestViewHolder>() {

    class RequestViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvPatientName: TextView = view.findViewById(R.id.tvPatientName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_request, parent, false)
        return RequestViewHolder(view)
    }

    override fun onBindViewHolder(holder: RequestViewHolder, position: Int) {
        val request = requests[position]
        holder.tvPatientName.text = request.patientName

        // 🚀 Doctor patient meeda click chesinappudu
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, ChatActivity::class.java)

            intent.putExtra("OTHER_USER_ID", request.patientId)
            intent.putExtra("OTHER_USER_NAME", request.patientName)
            intent.putExtra("IS_DOCTOR_APP", true) // Doctor app nundi velthunnam

            context.startActivity(intent)
        }
    }

    override fun getItemCount() = requests.size
}