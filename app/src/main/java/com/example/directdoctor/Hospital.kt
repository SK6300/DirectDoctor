package com.example.directdoctor
import java.io.Serializable

data class Hospital(
    var id: String = "",
    val name: String = "",
    val address: String = "",
    val locationLink: String = "",
    val emergencyContact: String = "",
    val websiteUrl: String = "",
    val timings: String = "",
    val facilities: String = "",
    val doctorsList: String = "",
    val reviewsLink: String = ""
) : Serializable