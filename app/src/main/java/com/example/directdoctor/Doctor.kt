package com.example.directdoctor
import java.io.Serializable

data class Doctor(
    var id: String = "",
    val fullName: String = "",
    val specialisation: String = "",
    val profileImageUrl: String = "",
    val mobileNumber: String = "",
    val age: String = "",
    val gender: String = "",
    val portfolioUrl: String = "",
    val websiteUrl: String = "",
    val aboutUs: String = "",
    val consultationFee: String = "",
    val clinicAddress: String = "",
    val availabilityNotes: String = "",
    val bookedAppointments: String = "",
    var portfolio: String = "",
    var website: String = ""
) : Serializable