package com.travel.app.service

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Query
import it.unical.ea.dtos.authDto.LoginRequest
import it.unical.ea.dtos.authDto.SignupRequest
import it.unical.ea.dtos.authDto.JwtResponse
import it.unical.ea.dtos.authDto.ForgotPasswordRequest
import it.unical.ea.dtos.authDto.ResetPasswordRequest
import it.unical.ea.dtos.user.UserDTO
import retrofit2.http.DELETE
import retrofit2.http.Path
import it.unical.ea.dtos.activity.ActivityDto
import it.unical.ea.dtos.itinerary.ItineraryDto
import it.unical.ea.dtos.itinerary.CreateItineraryRequest
import it.unical.ea.dtos.payment.PaymentIntentResponseDto
import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.Part

interface ApiService {

    // Chiamata per il Login - Restituisce i token di autenticazione
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): JwtResponse

    // Chiamata per la Registrazione - Restituisce un messaggio o token come Stringa
    @POST("api/auth/signup")
    suspend fun register(@Body request: SignupRequest): String

    @POST("api/auth/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): String

    @POST("api/auth/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): String

    // Chiamata per recuperare il profilo dell'utente autenticato
    @GET("user/me")
    suspend fun getMe(): UserDTO

    // Chiamata per aggiornare il profilo dell'utente autenticato
    @PUT("user/me")
    suspend fun updateMe(@Body request: UserDTO): UserDTO

    @GET("user")
    suspend fun getUsers(): List<UserDTO>

    @GET("user/{id}")
    suspend fun getUser(@Path("id") id: String): UserDTO

    // Chiamata per eliminare l'account
    @DELETE("user/{id}")
    suspend fun deleteUser(@Path("id") id: String)

    // Chiamata per caricare l'avatar
    @Multipart
    @POST("user/{id}/avatar")
    suspend fun uploadAvatar(
        @Path("id") id: String,
        @Part file: MultipartBody.Part
    ): UserDTO

    // Chiamata per creare una nuova attività
    @POST("activity")
    suspend fun createActivity(@Body request: ActivityDto): ActivityDto

    @PUT("activity/{id}")
    suspend fun updateActivity(@Path("id") id: String, @Body request: ActivityDto): ActivityDto

    @POST("activity/{id}/book")
    suspend fun bookActivity(@Path("id") id: String): PaymentIntentResponseDto

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
        @retrofit2.http.Query("minStartTime") minStartTime: String? = null,
        @retrofit2.http.Query("page") page: Int = 0,
        @retrofit2.http.Query("size") size: Int = 10
    ): it.unical.ea.dtos.common.PageDto<ActivityDto>

    @GET("api/location/search")
    suspend fun searchLocalita(
        @retrofit2.http.Query("query") query: String,
        @retrofit2.http.Query("includeExternal") includeExternal: Boolean,
        @retrofit2.http.Query("page") page: Int = 0,
        @retrofit2.http.Query("size") size: Int = 10
    ): it.unical.ea.dtos.common.PageDto<it.unical.ea.dtos.location.LocationDto>

    @GET("api/location/{id}")
    suspend fun getLocalitaById(@Path("id") id: String): it.unical.ea.dtos.location.LocationDto

    @GET("activity/{id}")
    suspend fun getActivityById(@Path("id") id: String): ActivityDto

    @GET("api/feed/personalized")
    suspend fun getPersonalizedFeed(): List<it.unical.ea.dtos.location.LocationDto>

    // Chiamata per recuperare tutti gli itinerari
    @GET("itinerary")
    suspend fun getItineraries(): List<ItineraryDto>

    // Chiamata per recuperare gli itinerari creati da uno specifico utente
    @GET("itinerary/creator/{creatorId}")
    suspend fun getItinerariesByCreator(@Path("creatorId") creatorId: String): List<ItineraryDto>

    @GET("itinerary/{id}")
    suspend fun getItineraryById(@Path("id") id: String): ItineraryDto

    // Chiamata per creare un itinerario
    @POST("itinerary")
    suspend fun createItinerary(@Body request: CreateItineraryRequest): ItineraryDto

    // Chiamata per eliminare un itinerario
    @DELETE("itinerary/{id}")
    suspend fun deleteItinerary(@Path("id") id: String)

    @POST("itinerary/{id}/book")
    suspend fun bookItinerary(@Path("id") id: String): PaymentIntentResponseDto

    @GET("itinerary/{id}/isBooked")
    suspend fun isItineraryBooked(@Path("id") id: String): Boolean

    @POST("itinerary/booking/{bookingId}/confirm")
    suspend fun confirmItineraryBooking(@Path("bookingId") bookingId: String)

    // Chiamata per aggiornare la visibilità di un itinerario
    @PUT("itinerary/{id}/visibility")
    suspend fun updateItineraryVisibility(
        @Path("id") id: String, 
        @Query("visibility") visibility: String
    ): ItineraryDto

    @DELETE("activity/{id}")
    suspend fun deleteActivity(@Path("id") id: String) { throw NotImplementedError() }

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

open class MockApiService : ApiService {
    override suspend fun login(request: LoginRequest): JwtResponse = throw NotImplementedError()
    override suspend fun register(request: SignupRequest): String = throw NotImplementedError()
    override suspend fun forgotPassword(request: ForgotPasswordRequest): String = throw NotImplementedError()
    override suspend fun resetPassword(request: ResetPasswordRequest): String = throw NotImplementedError()
    override suspend fun getMe(): UserDTO = throw NotImplementedError()
    override suspend fun updateMe(request: UserDTO): UserDTO = throw NotImplementedError()
    override suspend fun deleteUser(id: String) {}
    override suspend fun uploadAvatar(id: String, file: MultipartBody.Part): UserDTO = throw NotImplementedError()
    override suspend fun createActivity(request: ActivityDto): ActivityDto = throw NotImplementedError()
    override suspend fun updateActivity(id: String, request: ActivityDto): ActivityDto = throw NotImplementedError()
    override suspend fun getBookedUsers(id: String): List<UserDTO> = throw NotImplementedError()
    override suspend fun getActivities(): List<ActivityDto> = throw NotImplementedError()
    override suspend fun searchActivities(
        query: String, minPrice: Double?, maxPrice: Double?, page: Int, size: Int
    ): it.unical.ea.dtos.common.PageDto<ActivityDto> = throw NotImplementedError()
    override suspend fun searchLocalita(
        query: String, includeExternal: Boolean, page: Int, size: Int
    ): it.unical.ea.dtos.common.PageDto<it.unical.ea.dtos.location.LocationDto> = throw NotImplementedError()
    override suspend fun getLocalitaById(id: String): it.unical.ea.dtos.location.LocationDto = throw NotImplementedError()
    override suspend fun getActivityById(id: String): ActivityDto = throw NotImplementedError()
    override suspend fun getPersonalizedFeed(): List<it.unical.ea.dtos.location.LocationDto> = throw NotImplementedError()
    override suspend fun getItineraries(): List<ItineraryDto> = throw NotImplementedError()
    override suspend fun getItinerariesByCreator(creatorId: String): List<ItineraryDto> = throw NotImplementedError()
    override suspend fun createItinerary(request: CreateItineraryRequest): ItineraryDto = throw NotImplementedError()
    override suspend fun updateItineraryVisibility(id: String, visibility: String): ItineraryDto = throw NotImplementedError()
    override suspend fun deleteItinerary(id: String) {}
    override suspend fun deleteActivity(id: String) {}
    override suspend fun uploadDocument(file: okhttp3.MultipartBody.Part): String = throw NotImplementedError()
    override suspend fun getPendingCompanies(): List<UserDTO> = throw NotImplementedError()
    override suspend fun approveCompany(id: String) {}
    override suspend fun rejectCompany(id: String) {}
    override suspend fun getPendingActivities(): List<ActivityDto> = throw NotImplementedError()
    override suspend fun approveActivity(id: String) {}
    override suspend fun rejectActivity(id: String) {}
    override suspend fun getAllCompanies(): List<UserDTO> = throw NotImplementedError()
    override suspend fun blockCompany(id: String) {}
    override suspend fun unblockCompany(id: String) {}
    override suspend fun getUsers(): List<UserDTO> = throw NotImplementedError()
    override suspend fun getUser(id: String): UserDTO = throw NotImplementedError()
    override suspend fun getItineraryById(id: String): ItineraryDto = throw NotImplementedError()
    
    override suspend fun bookItinerary(id: String): PaymentIntentResponseDto = throw NotImplementedError()
    override suspend fun isItineraryBooked(id: String): Boolean = throw NotImplementedError()
    override suspend fun confirmItineraryBooking(bookingId: String) {}
    override suspend fun bookActivity(id: String): PaymentIntentResponseDto = throw NotImplementedError()
}
