package com.travel.app.domain.repository

import com.travel.app.data.remote.ReviewApiService
import com.travel.app.domain.model.review.CreateReviewDto
import com.travel.app.domain.model.review.ReviewDto

class ReviewRepository(
    private val apiService: ReviewApiService
) {
    suspend fun createReview(dto: CreateReviewDto): Result<ReviewDto> {
        return try {
            val response = apiService.createReview(dto)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to create review: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getReviewsForActivity(activityId: String): Result<List<ReviewDto>> {
        return try {
            val response = apiService.getReviewsForActivity(activityId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to fetch reviews: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getReviewsForItinerary(itineraryId: String): Result<List<ReviewDto>> {
        return try {
            val response = apiService.getReviewsForItinerary(itineraryId)
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateReview(id: String, review: CreateReviewDto): Result<ReviewDto> {
        return try {
            val response = apiService.updateReview(id, review)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteReview(id: String): Result<Unit> {
        return try {
            val response = apiService.deleteReview(id)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
