package com.example.simplenote.data.model

// ---------- AUTH ----------
data class RegisterRequest(
    val username: String,
    val password: String,
    val email: String,
    val first_name: String? = null,
    val last_name: String? = null
)

data class TokenObtainPairRequest(
    val username: String,
    val password: String
)

data class TokenObtainPairResponse(
    val access: String,
    val refresh: String
)

data class TokenRefreshRequest(
    val refresh: String
)

data class TokenRefreshResponse(
    val access: String
)

data class UserInfoResponse(
    val id: Int,
    val username: String,
    val email: String?,
    val first_name: String?,
    val last_name: String?
)

// ---------- NOTES ----------
data class NoteDto(
    val id: Int,
    val title: String,
    val description: String,
    val created_at: String,
    val updated_at: String,
    val creator_name: String,
    val creator_username: String
)

data class NoteCreateRequest(
    val title: String,
    val description: String
)

data class NotePatchRequest(
    val title: String? = null,
    val description: String? = null
)
