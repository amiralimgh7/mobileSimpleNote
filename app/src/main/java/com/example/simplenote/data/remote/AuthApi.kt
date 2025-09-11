package com.example.simplenote.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

// ==== Requests ====
data class LoginRequest(
    val username: String,
    val password: String
)

data class RefreshRequest(
    val refresh: String
)

data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String,
    val first_name: String? = null,
    val last_name: String? = null
)

data class ChangePasswordRequest(
    val old_password: String,
    val new_password: String
)

// ==== Responses ====
data class TokenResponse(
    val access: String,
    val refresh: String
)

data class RefreshResponse(
    val access: String
)

data class UserInfoResponse(
    val id: Int,
    val username: String,
    val email: String,
    val first_name: String? = null,
    val last_name: String? = null
)

data class ChangePasswordResponse(
    val detail: String
)

interface AuthApi {
    // Login (JWT)
    @POST("api/auth/token/")
    suspend fun login(@Body body: LoginRequest): Response<TokenResponse>

    // Refresh
    @POST("api/auth/token/refresh/")
    suspend fun refresh(@Body body: RefreshRequest): Response<RefreshResponse>

    // Register
    @POST("api/auth/register/")
    suspend fun register(@Body body: RegisterRequest): Response<UserInfoResponse>

    // User info (Bearer access)
    @GET("api/auth/userinfo/")
    suspend fun userInfo(
        @Header("Authorization") bearer: String
    ): Response<UserInfoResponse>

    // Change password (Bearer access)
    @POST("api/auth/change-password/")
    suspend fun changePassword(
        @Body body: ChangePasswordRequest
    ): Response<ChangePasswordResponse>
}
