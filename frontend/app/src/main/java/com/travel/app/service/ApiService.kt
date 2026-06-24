package com.travel.app.service

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.PUT
import com.travel.app.data.dto.LoginRequest
import com.travel.app.data.dto.SignupRequest
import com.travel.app.data.dto.UserDTO

interface ApiService {

    // Chiamata per il Login - Restituisce il Token come Stringa
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): String

    // Chiamata per la Registrazione - Restituisce un messaggio o token come Stringa
    @POST("api/auth/signup")
    suspend fun register(@Body request: SignupRequest): String

    // Chiamata per recuperare il profilo dell'utente autenticato
    @GET("user/me")
    suspend fun getMe(): UserDTO

    // Chiamata per aggiornare il profilo dell'utente autenticato
    @PUT("user/me")
    suspend fun updateMe(@Body request: UserDTO): UserDTO
}
