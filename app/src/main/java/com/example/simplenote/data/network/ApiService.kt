package com.example.simplenote.data.network

import com.example.simplenote.data.model.NoteCreateRequest
import com.example.simplenote.data.model.NoteDto
import com.example.simplenote.data.model.NotePatchRequest
import com.example.simplenote.data.model.RegisterRequest
import com.example.simplenote.data.model.TokenObtainPairRequest
import com.example.simplenote.data.model.TokenObtainPairResponse
import com.example.simplenote.data.model.TokenRefreshRequest
import com.example.simplenote.data.model.TokenRefreshResponse
import com.example.simplenote.data.model.UserInfoResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {

    // ---------- AUTH ----------
    @POST("api/auth/register/")
    suspend fun register(@Body body: RegisterRequest): Response<Unit> // 201 بدون body هم درست هندل می‌شود

    @POST("api/auth/token/")
    suspend fun token(@Body body: TokenObtainPairRequest): Response<TokenObtainPairResponse>

    @POST("api/auth/token/refresh/")
    suspend fun refresh(@Body body: TokenRefreshRequest): Response<TokenRefreshResponse>

    @GET("api/auth/userinfo/")
    suspend fun userInfo(): Response<UserInfoResponse>

    // ---------- NOTES ----------
    @GET("api/notes/")
    suspend fun notesList(): Response<List<NoteDto>>

    @POST("api/notes/")
    suspend fun noteCreate(@Body body: NoteCreateRequest): Response<NoteDto>

    @GET("api/notes/{id}/")
    suspend fun noteDetail(@Path("id") id: Int): Response<NoteDto>

    @PUT("api/notes/{id}/")
    suspend fun noteUpdate(@Path("id") id: Int, @Body body: NoteCreateRequest): Response<NoteDto>

    @PATCH("api/notes/{id}/")
    suspend fun notePatch(@Path("id") id: Int, @Body body: NotePatchRequest): Response<NoteDto>

    @DELETE("api/notes/{id}/")
    suspend fun noteDelete(@Path("id") id: Int): Response<Unit>

    @POST("api/notes/bulk")
    suspend fun noteBulk(@Body items: List<NoteCreateRequest>): Response<List<NoteDto>>

    @GET("api/notes/filter")
    suspend fun notesFilter(): Response<List<NoteDto>>
}
