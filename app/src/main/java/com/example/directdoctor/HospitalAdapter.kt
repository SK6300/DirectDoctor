package com.example.directdoctor

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class HospitalAdapter(private val hospitalList: List<Hospital>) : RecyclerView.Adapter<HospitalAdapter.HospitalViewHolder>() {

    class HospitalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvHospName)
        val tvAddress: TextView = itemView.findViewById(R.id.tvHospAddress)
        val tvContact: TextView = itemView.findViewById(R.id.tvHospContact)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HospitalViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_blank, parent, false)
        return HospitalViewHolder(view)
    }

    override fun onBindViewHolder(holder: HospitalViewHolder, position: Int) {
        val hosp = hospitalList[position]
        holder.tvName.text = hosp.name
        holder.tvAddress.text = hosp.address
        holder.tvContact.text = "Emergency: ${hosp.emergencyContact}"

        // Click Action added here
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, HospitalDetailsActivity::class.java)
            intent.putExtra("HOSPITAL_DATA", hosp)
            context.startActivity(intent)
        }
    }

    override fun getItemCount() = hospitalList.size
}