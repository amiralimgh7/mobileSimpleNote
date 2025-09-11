package com.example.simplenote.data.network

import android.content.Context
import android.util.Log
import com.example.simplenote.BuildConfig
import com.example.simplenote.data.auth.TokenStore
import com.example.simplenote.data.remote.AuthApi
import com.example.simplenote.data.remote.RefreshRequest
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.Route
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitProvider {

    @Volatile private var retrofit: Retrofit? = null

    // مسیرهای عمومی (بدون Authorization)
    private val publicPaths = listOf(
        "/api/auth/token/",
        "/api/auth/register/",
        "/api/auth/token/refresh/"
    )

    fun get(context: Context): Retrofit {
        return retrofit ?: synchronized(this) {
            retrofit ?: buildRetrofit(context.applicationContext).also { retrofit = it }
        }
    }

    private fun buildRetrofit(appContext: Context): Retrofit {
        val tokenStore = TokenStore(appContext)

        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        // Interceptor → اضافه کردن Authorization
        val authInterceptor = Interceptor { chain ->
            val original = chain.request()
            val path = original.url.encodedPath
            val isPublic = publicPaths.any { path.endsWith(it) || path.contains(it) }

            val req = if (!isPublic) {
                val access = try {
                    tokenStore.getAccessBlocking()
                } catch (_: Exception) { null }
                if (!access.isNullOrBlank()) {
                    original.newBuilder()
                        .addHeader("Authorization", "Bearer $access")
                        .build()
                } else original
            } else original

            chain.proceed(req)
        }

        // Authenticator → در صورت 401، توکن رفرش کن
        val authenticator = Authenticator { _: Route?, response: Response ->
            if (response.code == 401) {
                val refresh = runBlocking { tokenStore.getRefresh() }
                if (refresh.isNullOrBlank()) return@Authenticator null

                try {
                    val api = Retrofit.Builder()
                        .baseUrl(BuildConfig.API_BASE_URL)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build()
                        .create(AuthApi::class.java)

                    val refreshRes = runBlocking { api.refresh(RefreshRequest(refresh)) }

                    if (refreshRes.isSuccessful) {
                        val newAccess = refreshRes.body()?.access
                        if (!newAccess.isNullOrBlank()) {
                            runBlocking { tokenStore.saveTokens(newAccess, refresh) }

                            return@Authenticator response.request.newBuilder()
                                .header("Authorization", "Bearer $newAccess")
                                .build()
                        }
                    } else {
                        runBlocking { tokenStore.clear() }
                    }
                } catch (t: Throwable) {
                    Log.e("RetrofitProvider", "Token refresh failed", t)
                }
            }
            null
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(logging)
            .authenticator(authenticator)   // 👈 اضافه شد
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .callTimeout(20, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}
