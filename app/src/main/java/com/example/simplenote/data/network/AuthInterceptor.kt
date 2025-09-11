package com.example.simplenote.data.network

import com.example.simplenote.data.auth.TokenStore
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Authorization فقط روی مسیرهایی که لازم دارن اضافه می‌شود.
 * روی مسیرهای auth عمداً حذف می‌شود تا خطای token_not_valid نگیریم.
 */
class AuthInterceptor(
    private val tokenStore: TokenStore
) : Interceptor {

    private val authExcludedPaths = setOf(
        "/api/auth/register/",
        "/api/auth/token/",
        "/api/auth/token/refresh/"
    )

    override fun intercept(chain: Interceptor.Chain): Response {
        val req = chain.request()
        val path = req.url.encodedPath

        val builder = req.newBuilder()
            .header("Accept", "application/json")

        val shouldAttachAuth = !authExcludedPaths.contains(path)
        if (shouldAttachAuth) {
            val access = tokenStore.getAccessBlocking()
            if (!access.isNullOrBlank()) {
                builder.header("Authorization", "Bearer $access")
            }
        }

        return chain.proceed(builder.build())
    }
}
