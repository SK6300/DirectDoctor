package com.example.directdoctor

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop

// 🚀 Kotha 'hasUnreadMessages' field add chesam
data class ConnectedDoctor(
    val doctorId: String = "",
    val doctorName: String = "",
    val specialisation: String = "",
    val profileImageUrl: String = "",
    val interactionType: String = "",
    var hasUnreadMessages: Boolean = false
)

class ConnectedDoctorsAdapter(private var doctorsList: List<ConnectedDoctor>) : RecyclerView.Adapter<ConnectedDoctorsAdapter.DocViewHolder>() {

    class DocViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivDocPic: ImageView = view.findViewById(R.id.ivDocPic)
        val tvDocName: TextView = view.findViewById(R.id.tvDocName)
        val tvDocSpec: TextView = view.findViewById(R.id.tvDocSpec)
        val tvInteractionType: TextView = view.findViewById(R.id.tvInteractionType)
        val btnChat: Button = view.findViewById(R.id.btnChatWithDoc)

        // 🚀 Item layout lo unna red dot badge ID
        val tvUnreadBadge: TextView = view.findViewById(R.id.tvUnreadBadge)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DocViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_connected_doctor, parent, false)
        return DocViewHolder(view)
    }

    override fun onBindViewHolder(holder: DocViewHolder, position: Int) {
        val doc = doctorsList[position]

        holder.tvDocName.text = doc.doctorName
        holder.tvDocSpec.text = doc.specialisation
        holder.tvInteractionType.text = "Interaction: ${doc.interactionType}"

        if (doc.profileImageUrl.isNotEmpty()) {
            Glide.with(holder.itemView.context).load(doc.profileImageUrl).placeholder(R.drawable.doctor_profile).transform(CircleCrop()).into(holder.ivDocPic)
        }

        // 🚀 Doctor ki unread messages unte red dot chupinchu, lekapothe hide cheyu
        if (doc.hasUnreadMessages) {
            holder.tvUnreadBadge.visibility = View.VISIBLE
        } else {
            holder.tvUnreadBadge.visibility = View.GONE
        }

        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, DoctorDetailsActivity::class.java)

            val doctorData = Doctor(
                id = doc.doctorId,
                fullName = doc.doctorName,
                specialisation = doc.specialisation,
                profileImageUrl = doc.profileImageUrl
            )

            intent.putExtra("DOCTOR_DATA", doctorData)
            intent.putExtra("CONNECTION_STATUS", doc.interactionType)
            context.startActivity(intent)
        }

        // Chat open avvadaniki kotha Intent logic
        holder.btnChat.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, ChatActivity::class.java)

            intent.putExtra("OTHER_USER_ID", doc.doctorId)
            intent.putExtra("OTHER_USER_NAME", doc.doctorName)
            intent.putExtra("IS_DOCTOR_APP", false)

            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = doctorsList.size

    // 🚀 Activity nundi data update cheyadaniki chinna function
    fun updateData(newList: List<ConnectedDoctor>) {
        doctorsList = newList
        notifyDataSetChanged()
    }
}