package com.travel.app.data.remote

import com.travel.app.domain.model.review.CreateReviewDto
import com.travel.app.domain.model.review.ReviewDto
import retrofit2.Response
import retrofit2.http.*

interface ReviewApiService {
    
    @POST("api/v1/reviews")
    suspend fun createReview(@Body createReviewDto: CreateReviewDto): Response<ReviewDto>

    @GET("api/v1/reviews/activity/{activityId}")
    suspend fun getReviewsForActivity(@Path("activityId") activityId: String): Response<List<ReviewDto>>

    @GET("api/v1/reviews/itinerary/{itineraryId}")
    suspend fun getReviewsForItinerary(@Path("itineraryId") itineraryId: String): Response<List<ReviewDto>>

    @PUT("api/v1/reviews/{id}")
    suspend fun updateReview(@Path("id") id: String, @Body review: CreateReviewDto): Response<ReviewDto>

    @DELETE("api/v1/reviews/{id}")
    suspend fun deleteReview(@Path("id") id: String): Response<Void>

    @GET("api/v1/reviews/user/{userId}")
    suspend fun getReviewsByUser(@Path("userId") userId: String): Response<List<ReviewDto>>
}
