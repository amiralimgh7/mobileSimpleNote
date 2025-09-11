package com.example.simplenote.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

data class NoteDto(
    val id: Int,
    val title: String,
    val description: String,
    val color: String? = null,
    val created_at: String? = null,
    val updated_at: String? = null
)
data class PageResponse<T>(
    val count: Int,
    val next: String?,
    val previous: String?,
    val results: List<T>
)

data class NoteCreateRequest(val title: String, val description: String)
data class NoteUpdateRequest(val title: String? = null, val description: String? = null)

interface NotesApi {
    @GET("api/notes/")
    suspend fun listNotes(
        @Query("page") page: Int? = null,
        @Query("page_size") pageSize: Int? = null
    ): Response<PageResponse<NoteDto>>

    @GET("api/notes/filter")
    suspend fun filterNotes(
        @Query("title") title: String? = null,
        @Query("description") description: String? = null,
        @Query("page") page: Int? = null,
        @Query("page_size") pageSize: Int? = null
    ): Response<PageResponse<NoteDto>>

    @GET("api/notes/{id}/")
    suspend fun getNote(@Path("id") id: Int): Response<NoteDto>

    @POST("api/notes/")
    suspend fun create(@Body body: NoteCreateRequest): Response<NoteDto>

    @PATCH("api/notes/{id}/")
    suspend fun update(@Path("id") id: Int, @Body body: NoteUpdateRequest): Response<NoteDto>

    @DELETE("api/notes/{id}/")
    suspend fun delete(@Path("id") id: Int): Response<Unit>
}
