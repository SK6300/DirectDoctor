package com.example.directdoctor

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop

class DoctorAdapter(private val doctorList: List<Doctor>) : RecyclerView.Adapter<DoctorAdapter.DoctorViewHolder>() {

    class DoctorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivDocPic: ImageView = itemView.findViewById(R.id.ivDocPic)
        val tvDocName: TextView = itemView.findViewById(R.id.tvDocName)
        val tvDocSpec: TextView = itemView.findViewById(R.id.tvDocSpec)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DoctorViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_doctor_suggestion, parent, false)
        return DoctorViewHolder(view)
    }

    override fun onBindViewHolder(holder: DoctorViewHolder, position: Int) {
        val doctor = doctorList[position]

        val nameDisplay = if (doctor.fullName.startsWith("Dr.", true)) doctor.fullName else "Dr. ${doctor.fullName}"
        holder.tvDocName.text = nameDisplay
        holder.tvDocSpec.text = doctor.specialisation.ifEmpty { "General Physician" }

        if (doctor.profileImageUrl.isNotEmpty()) {
            Glide.with(holder.itemView.context).load(doctor.profileImageUrl).placeholder(R.drawable.doctor_profile).transform(CircleCrop()).into(holder.ivDocPic)
        } else {
            holder.ivDocPic.setImageResource(R.drawable.doctor_profile)
        }

        // Click chesthe kotha page ki velthundi
        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, DoctorDetailsActivity::class.java)
            intent.putExtra("DOCTOR_DATA", doctor)
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount() = doctorList.size
}