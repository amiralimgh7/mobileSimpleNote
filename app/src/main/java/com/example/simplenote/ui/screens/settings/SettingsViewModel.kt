package com.example.simplenote.ui.screens.settings

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.simplenote.AppGraph
import com.example.simplenote.data.network.RetrofitProvider
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.Response
import retrofit2.http.GET

private const val TAG = "SettingsVM"

data class SettingsState(
    val name: String = "",
    val email: String = "",
    val error: String? = null
)

private interface AuthApi {
    @GET("/api/auth/userinfo/")
    suspend fun userInfo(): Response<UserInfoDto>
}

data class UserInfoDto(
    val id: Int? = null,
    val username: String? = null,
    val email: String? = null,
    @SerializedName("first_name") val firstName: String? = null,
    @SerializedName("last_name")  val lastName: String? = null
)

class SettingsViewModel(
    private val appContext: Context
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state

    private val api: AuthApi by lazy {
        RetrofitProvider.get(appContext).create(AuthApi::class.java)
    }

    init { loadUser() }

    private fun loadUser() = viewModelScope.launch {
        try {
            val r = api.userInfo()
            Log.d(TAG, "userinfo url=${r.raw().request.url}")
            if (r.isSuccessful) {
                val u = r.body()
                val fullName = listOfNotNull(u?.firstName, u?.lastName)
                    .filter { !it.isNullOrBlank() }
                    .joinToString(" ")
                    .ifBlank { u?.username.orEmpty() }

                _state.value = SettingsState(
                    name = fullName,
                    email = u?.email.orEmpty()
                )
            } else {
                val msg = r.errorBody()?.string().orEmpty().ifBlank { "Failed to load profile" }
                Log.e(TAG, "userinfo error code=${r.code()} msg=$msg")
                _state.value = _state.value.copy(error = msg)
            }
        } catch (t: Throwable) {
            Log.e(TAG, "userinfo exception", t)
            _state.value = _state.value.copy(error = t.message ?: "Network error")
        }
    }

    /** لاگ‌اوت: پاک کردن توکن‌ها + دیتابیس لوکال + توقف سینک Outbox */
    fun logout() {
        viewModelScope.launch {
            Log.d(TAG, "logout() requested")
            runCatching { AppGraph.logoutAndWipe() }
                .onFailure { Log.e(TAG, "logoutAndWipe failed", it) }
        }
    }

    companion object {
        fun factory(ctx: Context): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    @Suppress("UNCHECKED_CAST")
                    return SettingsViewModel(ctx.applicationContext) as T
                }
            }
    }
}
