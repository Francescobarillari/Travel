package com.travel.app.service

import retrofit2.http.Body
import retrofit2.http.POST
import com.travel.app.data.dto.LoginRequestDto
import com.travel.app.data.dto.LoginResponseDto
import com.travel.app.data.dto.SignUpRequestDto
import com.travel.app.data.dto.UserDto

interface ApiService {

    // Chiamata per il Login
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequestDto): LoginResponseDto

    // Chiamata per la Registrazione
    @POST("api/auth/signup")
    suspend fun register(@Body request: SignUpRequestDto): UserDto

}
