package com.example.simplenote.ui.screens.register

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simplenote.AppGraph
import kotlinx.coroutines.launch

data class RegisterState(
    val firstName: String = "",
    val lastName: String = "",
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val retype: String = "",
    val loading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false
)

class RegisterViewModel : ViewModel() {
    var state by mutableStateOf(RegisterState())
        private set

    fun onFirstNameChange(v: String) { state = state.copy(firstName = v) }
    fun onLastNameChange(v: String)  { state = state.copy(lastName = v) }
    fun onUsernameChange(v: String)  { state = state.copy(username = v) }
    fun onEmailChange(v: String)     { state = state.copy(email = v) }
    fun onPasswordChange(v: String)  { state = state.copy(password = v) }
    fun onRetypeChange(v: String)    { state = state.copy(retype = v) }

    /** هیچ ولیدیشن UI — مستقیم درخواست می‌زنیم */
    fun submit() {
        if (state.loading) return
        viewModelScope.launch {
            state = state.copy(loading = true, error = null)
            val s = state
            val res = AppGraph.authRepository.register(
                username = s.username,
                password = s.password,
                email = s.email,
                first = s.firstName,
                last  = s.lastName
            )
            state = if (res.isSuccess) {
                state.copy(loading = false, success = true)
            } else {
                state.copy(loading = false, error = res.exceptionOrNull()?.message ?: "Registration failed")
            }
        }
    }
}
