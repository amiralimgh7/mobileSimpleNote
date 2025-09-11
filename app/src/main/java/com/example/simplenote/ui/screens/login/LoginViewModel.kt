package com.example.simplenote.ui.screens.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simplenote.AppGraph
import kotlinx.coroutines.launch

data class LoginState(
    val identity: String = "",
    val password: String = "",
    val loading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false
)

class LoginViewModel : ViewModel() {
    var state by mutableStateOf(LoginState())
        private set

    fun onIdentityChange(v: String) { state = state.copy(identity = v, error = null) }
    fun onPasswordChange(v: String) { state = state.copy(password = v, error = null) }

    fun login() {
        if (state.loading) return
        viewModelScope.launch {
            state = state.copy(loading = true, error = null)
            val res = AppGraph.authRepository.login(state.identity.trim(), state.password)
            state = if (res.isSuccess) {
                state.copy(loading = false, success = true)
            } else {
                state.copy(loading = false, error = res.exceptionOrNull()?.message ?: "Login failed")
            }
        }
    }
}
