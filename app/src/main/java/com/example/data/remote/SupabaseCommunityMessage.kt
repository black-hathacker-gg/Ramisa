package com.example.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SupabaseCommunityMessage(
    @SerialName("id") val id: String? = null,
    @SerialName("user_id") val userId: String,
    @SerialName("user_name") val userName: String,
    @SerialName("message") val message: String,
    @SerialName("is_sos_alert") val isSosAlert: Boolean = false,
    @SerialName("created_at") val createdAt: String? = null
)
