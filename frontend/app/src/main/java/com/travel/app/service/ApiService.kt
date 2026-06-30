package com.travel.app.service

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.PUT
import com.travel.app.data.dto.LoginRequest
import com.travel.app.data.dto.SignupRequest
import com.travel.app.data.dto.UserDTO
import retrofit2.http.DELETE
import retrofit2.http.Path
import it.unical.ea.dtos.activity.ActivityDto
import it.unical.ea.dtos.itinerary.ItineraryDto
import it.unical.ea.dtos.itinerary.CreateItineraryRequest

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

    // Chiamata per creare una nuova attività
    @POST("activity")
    suspend fun createActivity(@Body request: ActivityDto): ActivityDto

    // Chiamata per recuperare tutte le attività
    @GET("activity")
    suspend fun getActivities(): List<ActivityDto>

    // Chiamata per recuperare tutti gli itinerari
    @GET("itinerary")
    suspend fun getItineraries(): List<ItineraryDto>

    // Chiamata per creare un itinerario
    @POST("itinerary")
    suspend fun createItinerary(@Body request: CreateItineraryRequest): ItineraryDto

    // Chiamata per eliminare un itinerario
    @DELETE("itinerary/{id}")
    suspend fun deleteItinerary(@Path("id") id: String)
}
