package com.travel.app.data.repository

import com.google.gson.Gson
import com.travel.app.data.dto.ErrorResponseDto
import com.travel.app.domain.repository.ItineraryRepository
import com.travel.app.service.ApiService
import it.unical.ea.dtos.itinerary.ItineraryDto
import it.unical.ea.dtos.itinerary.CreateItineraryRequest
import retrofit2.HttpException
import java.io.IOException

class ItineraryRepositoryImpl(
    private val apiService: ApiService
) : ItineraryRepository {

    override suspend fun getItineraries(): Result<List<ItineraryDto>> {
        return try {
            val result = apiService.getItineraries()
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(Exception(handleError(e)))
        }
    }

    override suspend fun getItinerariesByCreator(creatorId: String): Result<List<ItineraryDto>> {
        return try {
            val result = apiService.getItinerariesByCreator(creatorId)
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(Exception(handleError(e)))
        }
    }

    override suspend fun createItinerary(request: CreateItineraryRequest): Result<ItineraryDto> {
        return try {
            val result = apiService.createItinerary(request)
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(Exception(handleError(e)))
        }
    }

    override suspend fun updateItineraryVisibility(id: String, visibility: String): Result<ItineraryDto> {
        return try {
            val result = apiService.updateItineraryVisibility(id, visibility)
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(Exception(handleError(e)))
        }
    }

    override suspend fun deleteItinerary(id: String): Result<Unit> {
        return try {
            apiService.deleteItinerary(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception(handleError(e)))
        }
    }

    private fun handleError(e: Exception): String {
        return when (e) {
            is HttpException -> {
                val errorBody = e.response()?.errorBody()?.string()
                try {
                    val errorDto = Gson().fromJson(errorBody, ErrorResponseDto::class.java)
                    errorDto.error ?: "Errore del server (${e.code()})"
                } catch (parseException: Exception) {
                    "Errore del server: ${e.code()}"
                }
            }
            is IOException -> "Nessuna connessione internet"
            else -> e.message ?: "Errore imprevisto"
        }
    }
}
