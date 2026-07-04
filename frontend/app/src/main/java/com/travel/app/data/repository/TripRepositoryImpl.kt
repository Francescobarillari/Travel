package com.travel.app.data.repository

import com.google.gson.Gson
import com.travel.app.data.dto.ErrorResponseDto
import com.travel.app.domain.repository.TripRepository
import com.travel.app.service.ApiService
import it.unical.ea.dtos.trip.TripDto
import retrofit2.HttpException
import java.io.IOException

class TripRepositoryImpl(
    private val apiService: ApiService
) : TripRepository {

    override suspend fun getTripById(id: String): Result<TripDto> {
        return try {
            val result = apiService.getTripById(id)
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(Exception(handleError(e)))
        }
    }

    override suspend fun searchTrips(query: String, minPrice: Double?, maxPrice: Double?, page: Int, size: Int): Result<it.unical.ea.dtos.common.PageDto<TripDto>> {
        return try {
            val result = apiService.searchTrips(query, minPrice, maxPrice, page, size)
            Result.success(result)
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
