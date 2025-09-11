package com.example.simplenote.data.remote.dto

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("email") val email: String,      // اگر بک‌اند username می‌خواهد این را عوض کن
    @SerializedName("password") val password: String
)

data class AuthTokens(
    @SerializedName("access") val access: String,
    @SerializedName("refresh") val refresh: String
)
