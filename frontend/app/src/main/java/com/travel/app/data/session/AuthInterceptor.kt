package com.travel.app.data.session

import com.travel.app.BuildConfig
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val sessionManagerProvider: () -> SessionManager) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val requestUrl = originalRequest.url.toString()
        val backendUrl = BuildConfig.BACKEND_URL

        // Sicurezza extra: allega il token di autorizzazione solo ed esclusivamente
        // se la richiesta HTTP è diretta verso il nostro backend per prevenire token leakage
        val isRequestToBackend = requestUrl.startsWith(backendUrl)

        val token = sessionManagerProvider().getSessionToken()

        val request = if (isRequestToBackend && !token.isNullOrBlank()) {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            originalRequest
        }

        return chain.proceed(request)
    }
}
