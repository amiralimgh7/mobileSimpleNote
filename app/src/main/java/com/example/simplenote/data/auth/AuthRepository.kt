package com.example.simplenote.data.auth

import com.example.simplenote.data.remote.AuthApi
import com.example.simplenote.data.remote.LoginRequest
import com.example.simplenote.data.remote.RefreshRequest
import com.example.simplenote.data.remote.RegisterRequest
import com.example.simplenote.data.remote.TokenResponse
import com.example.simplenote.data.remote.UserInfoResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import retrofit2.Response

class AuthRepository(
    private val api: AuthApi,
    private val tokenStore: TokenStore
) {
    /** Login: save tokens if ok */
    suspend fun login(identity: String, password: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val res = api.login(LoginRequest(username = identity, password = password))
            handleTokenResponse(res)
        } catch (t: Throwable) {
            Result.failure(t)
        }
    }

    /** Register: هیچ ولیدیشن فرانت—فقط پاس‌دادن به API، اگر 2xx بود موفق */
    suspend fun register(
        username: String,
        password: String,
        email: String,
        first: String?,
        last: String?
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val res = api.register(
                RegisterRequest(
                    username = username,
                    password = password,
                    email = email,
                    first_name = first,
                    last_name = last
                )
            )
            if (res.isSuccessful) {
                // خیلی از سرورها body برنمی‌گردونن (۲۰۱ بدون بدنه) → موفقیت حساب کن
                Result.success(Unit)
            } else {
                Result.failure(Exception(res.errorBody()?.string().toReadableMessage()))
            }
        } catch (t: Throwable) {
            Result.failure(t)
        }
    }

    /** Refresh access token */
    suspend fun refresh(): Result<Unit> = withContext(Dispatchers.IO) {
        val refresh = tokenStore.getRefresh()
            ?: return@withContext Result.failure(IllegalStateException("No refresh token"))
        try {
            val res = api.refresh(RefreshRequest(refresh))
            if (res.isSuccessful) {
                val body = res.body()
                if (body != null && body.access.isNotBlank()) {
                    tokenStore.saveTokens(access = body.access, refresh = refresh)
                    Result.success(Unit)
                } else Result.failure(IllegalStateException("Empty refresh response"))
            } else {
                Result.failure(Exception(res.errorBody()?.string().toReadableMessage()))
            }
        } catch (t: Throwable) {
            Result.failure(t)
        }
    }

    /** User info with current token */
    suspend fun userInfo(): Result<UserInfoResponse> = withContext(Dispatchers.IO) {
        val access = tokenStore.getAccess()
            ?: return@withContext Result.failure(IllegalStateException("Not authenticated"))
        try {
            val res = api.userInfo("Bearer $access")
            if (res.isSuccessful) {
                val body = res.body()
                if (body != null) Result.success(body)
                else Result.failure(IllegalStateException("Empty response"))
            } else {
                Result.failure(Exception(res.errorBody()?.string().toReadableMessage()))
            }
        } catch (t: Throwable) {
            Result.failure(t)
        }
    }

    private suspend fun handleTokenResponse(res: Response<TokenResponse>): Result<Unit> {
        return if (res.isSuccessful) {
            val body = res.body()
            if (body != null && body.access.isNotBlank() && body.refresh.isNotBlank()) {
                tokenStore.saveTokens(access = body.access, refresh = body.refresh)
                Result.success(Unit)
            } else Result.failure(IllegalStateException("Empty token response"))
        } else {
            Result.failure(Exception(res.errorBody()?.string().toReadableMessage()))
        }
    }
}

private fun String?.toReadableMessage(): String {
    val raw = this.orEmpty()
    return try {
        val obj = JSONObject(raw)
        when {
            obj.has("detail") -> obj.getString("detail")
            else -> obj.keys().asSequence().joinToString(" | ") { k -> "$k: ${obj.opt(k)}" }
        }
    } catch (_: Throwable) {
        raw.ifBlank { "Request failed" }
    }
}
