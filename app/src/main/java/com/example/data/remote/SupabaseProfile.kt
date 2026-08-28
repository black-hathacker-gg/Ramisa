package com.example.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SupabaseProfile(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String,
    @SerialName("email") val email: String = "",
    @SerialName("phone") val phone: String,
    @SerialName("emergency_pin") val emergencyPin: String,
    @SerialName("user_type") val userType: String,
    @SerialName("age_category") val ageCategory: String,
    @SerialName("blood_group") val bloodGroup: String,
    @SerialName("emergency_note") val emergencyNote: String
)
