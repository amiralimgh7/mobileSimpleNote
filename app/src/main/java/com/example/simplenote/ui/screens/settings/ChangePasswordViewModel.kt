package com.example.simplenote.ui.screens.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.simplenote.data.remote.AuthApi
import com.example.simplenote.data.remote.ChangePasswordRequest
import com.example.simplenote.data.remote.ChangePasswordResponse
import com.example.simplenote.data.network.RetrofitProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Response

data class ChangePasswordState(
    val current: String = "",
    val newPass: String = "",
    val retype: String = "",
    val isSubmitting: Boolean = false,
    val message: String? = null
)

class ChangePasswordViewModel(
    private val appContext: Context
) : ViewModel() {

    private val api: AuthApi by lazy {
        RetrofitProvider.get(appContext).create(AuthApi::class.java)
    }

    private val _state = MutableStateFlow(ChangePasswordState())
    val state: StateFlow<ChangePasswordState> = _state

    fun onCurrentChange(v: String) = _state.update { it.copy(current = v) }
    fun onNewChange(v: String) = _state.update { it.copy(newPass = v) }
    fun onRetypeChange(v: String) = _state.update { it.copy(retype = v) }
    fun clearMessage() = _state.update { it.copy(message = null) }

    fun submit() = viewModelScope.launch {
        val s = state.value
        if (s.current.isBlank() || s.newPass.isBlank() || s.retype.isBlank()) {
            _state.update { it.copy(message = "Please fill in all fields.") }
            return@launch
        }
        if (s.newPass.length < 6) {
            _state.update { it.copy(message = "New password must be at least 6 characters.") }
            return@launch
        }
        if (s.newPass != s.retype) {
            _state.update { it.copy(message = "New password and retype do not match.") }
            return@launch
        }

        _state.update { it.copy(isSubmitting = true, message = null) }
        runCatching {
            api.changePassword(ChangePasswordRequest(old_password = s.current, new_password = s.newPass))
        }.onSuccess { r: Response<ChangePasswordResponse> ->
            if (r.isSuccessful) {
                val msg = r.body()?.detail ?: "Password changed successfully."
                _state.update {
                    it.copy(
                        current = "",
                        newPass = "",
                        retype = "",
                        isSubmitting = false,
                        message = msg
                    )
                }
            } else {
                val raw = r.errorBody()?.string().orEmpty()
                val msg = raw.parseErrorMessage()
                _state.update { it.copy(isSubmitting = false, message = msg) }
            }
        }.onFailure { e ->
            _state.update { it.copy(isSubmitting = false, message = e.message ?: "Network error.") }
        }
    }

    companion object {
        fun factory(ctx: Context): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    @Suppress("UNCHECKED_CAST")
                    return ChangePasswordViewModel(ctx.applicationContext) as T
                }
            }
    }
}

/* --- Helper برای پیام خطا --- */
private fun String.parseErrorMessage(): String {
    val raw = this
    return try {
        val obj = JSONObject(raw)
        when {
            obj.has("detail") -> obj.getString("detail")
            obj.has("errors") -> obj.getJSONArray("errors")
                .takeIf { it.length() > 0 }
                ?.getJSONObject(0)
                ?.optString("message", raw)
                ?: raw
            else -> raw
        }
    } catch (_: Throwable) {
        raw.ifBlank { "Password change failed." }
    }
}
