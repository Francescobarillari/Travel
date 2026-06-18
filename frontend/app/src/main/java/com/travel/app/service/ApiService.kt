package com.travel.app.service

import retrofit2.http.Body
import retrofit2.http.POST
import com.travel.app.data.dto.LoginRequest
import com.travel.app.data.dto.SignupRequest

interface ApiService {

    // Chiamata per il Login - Restituisce il Token come Stringa
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): String

    // Chiamata per la Registrazione - Restituisce un messaggio o token come Stringa
    @POST("api/auth/signup")
    suspend fun register(@Body request: SignupRequest): String

}
