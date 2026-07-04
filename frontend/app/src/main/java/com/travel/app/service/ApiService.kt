package com.travel.app.service

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.PUT
import it.unical.ea.dtos.authDto.LoginRequest
import it.unical.ea.dtos.authDto.SignupRequest
import it.unical.ea.dtos.authDto.JwtResponse
import it.unical.ea.dtos.user.UserDTO
import retrofit2.http.DELETE
import retrofit2.http.Path
import it.unical.ea.dtos.activity.ActivityDto
import it.unical.ea.dtos.itinerary.ItineraryDto
import it.unical.ea.dtos.itinerary.CreateItineraryRequest

interface ApiService {

    // Chiamata per il Login - Restituisce i token di autenticazione
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): JwtResponse

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

    @PUT("activity/{id}")
    suspend fun updateActivity(@Path("id") id: String, @Body request: ActivityDto): ActivityDto

    @GET("activity/{id}/bookings")
    suspend fun getBookedUsers(@Path("id") id: String): List<UserDTO>

    // Chiamata per recuperare tutte le attività
    @GET("activity")
    suspend fun getActivities(): List<ActivityDto>

    @GET("activity/search")
    suspend fun searchActivities(
        @retrofit2.http.Query("query") query: String,
        @retrofit2.http.Query("minPrice") minPrice: Double? = null,
        @retrofit2.http.Query("maxPrice") maxPrice: Double? = null,
        @retrofit2.http.Query("page") page: Int = 0,
        @retrofit2.http.Query("size") size: Int = 10
    ): it.unical.ea.dtos.common.PageDto<ActivityDto> = throw NotImplementedError()

    @GET("api/trips/search")
    suspend fun searchTrips(
        @retrofit2.http.Query("query") query: String,
        @retrofit2.http.Query("minPrice") minPrice: Double? = null,
        @retrofit2.http.Query("maxPrice") maxPrice: Double? = null,
        @retrofit2.http.Query("page") page: Int = 0,
        @retrofit2.http.Query("size") size: Int = 10
    ): it.unical.ea.dtos.common.PageDto<it.unical.ea.dtos.trip.TripDto> = throw NotImplementedError()

    @GET("api/trips/{id}")
    suspend fun getTripById(@Path("id") id: String): it.unical.ea.dtos.trip.TripDto = throw NotImplementedError()

    @GET("activity/{id}")
    suspend fun getActivityById(@Path("id") id: String): ActivityDto = throw NotImplementedError()

    @GET("api/feed/personalized")
    suspend fun getPersonalizedFeed(): List<it.unical.ea.dtos.trip.TripDto> = throw NotImplementedError()

    // Chiamata per recuperare tutti gli itinerari
    @GET("itinerary")
    suspend fun getItineraries(): List<ItineraryDto>

    // Chiamata per creare un itinerario
    @POST("itinerary")
    suspend fun createItinerary(@Body request: CreateItineraryRequest): ItineraryDto

    // Chiamata per eliminare un itinerario
    @DELETE("itinerary/{id}")
    suspend fun deleteItinerary(@Path("id") id: String)

    // Caricamento documenti
    @retrofit2.http.Multipart
    @POST("api/auth/upload-document")
    suspend fun uploadDocument(
        @retrofit2.http.Part file: okhttp3.MultipartBody.Part
    ): String

    // Endpoint Admin
    @GET("api/admin/companies/pending")
    suspend fun getPendingCompanies(): List<UserDTO>

    @POST("api/admin/companies/{id}/approve")
    suspend fun approveCompany(@Path("id") id: String)

    @POST("api/admin/companies/{id}/reject")
    suspend fun rejectCompany(@Path("id") id: String)

    @GET("api/admin/activities/pending")
    suspend fun getPendingActivities(): List<ActivityDto>

    @POST("api/admin/activities/{id}/approve")
    suspend fun approveActivity(@Path("id") id: String)

    @DELETE("api/admin/activities/{id}")
    suspend fun rejectActivity(@Path("id") id: String)

    @GET("api/admin/companies")
    suspend fun getAllCompanies(): List<UserDTO>

    @POST("api/admin/companies/{id}/block")
    suspend fun blockCompany(@Path("id") id: String)

    @POST("api/admin/companies/{id}/unblock")
    suspend fun unblockCompany(@Path("id") id: String)
}
