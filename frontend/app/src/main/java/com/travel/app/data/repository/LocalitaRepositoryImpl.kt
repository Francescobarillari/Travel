package com.travel.app.data.repository

import com.google.gson.Gson
import com.travel.app.data.dto.ErrorResponseDto
import com.travel.app.domain.repository.LocalitaRepository
import com.travel.app.service.ApiService
import it.unical.ea.dtos.location.LocationDto as LocalitaDto
import retrofit2.HttpException
import java.io.IOException

class LocalitaRepositoryImpl(
    private val apiService: ApiService
) : LocalitaRepository {

    override suspend fun getLocalitaById(id: String): Result<LocalitaDto> {
        return try {
            val result = apiService.getLocalitaById(id)
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(Exception(handleError(e)))
        }
    }

    override suspend fun searchLocalita(query: String, includeExternal: Boolean, page: Int, size: Int): Result<it.unical.ea.dtos.common.PageDto<LocalitaDto>> {
        return try {
            val result = apiService.searchLocalita(query, includeExternal, page, size)
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
