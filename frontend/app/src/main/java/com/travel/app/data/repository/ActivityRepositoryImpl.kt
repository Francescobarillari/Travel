package com.travel.app.data.repository

import com.google.gson.Gson
import com.travel.app.data.dto.ErrorResponseDto
import com.travel.app.domain.repository.ActivityRepository
import com.travel.app.service.ApiService
import it.unical.ea.dtos.activity.ActivityDto
import it.unical.ea.dtos.activity.ActivityTemplateDto
import it.unical.ea.dtos.activity.CreateActivityRequestDto
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.io.IOException

class ActivityRepositoryImpl(
    private val apiService: ApiService
) : ActivityRepository {

    override suspend fun createActivity(activity: CreateActivityRequestDto): Result<ActivityTemplateDto> {
        return try {
            val result = apiService.createActivity(activity)
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(Exception(handleError(e)))
        }
    }

    override suspend fun getActivities(): Result<List<ActivityDto>> {
        return try {
            val result = apiService.getActivities()
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(Exception(handleError(e)))
        }
    }

    override suspend fun getActivityById(id: String): Result<ActivityDto> {
        return try {
            val result = apiService.getActivityById(id)
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(Exception(handleError(e)))
        }
    }

    override suspend fun searchActivities(query: String, minStartTime: String?, page: Int, size: Int): Result<it.unical.ea.dtos.common.PageDto<ActivityTemplateDto>> {
        return try {
            val result = apiService.searchActivities(query, minStartTime, page, size)
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(Exception(handleError(e)))
        }
    }

    override suspend fun updateActivity(id: String, activity: ActivityDto): Result<ActivityDto> {
        return try {
            val result = apiService.updateActivity(id, activity)
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(Exception(handleError(e)))
        }
    }

    override suspend fun uploadActivityImages(id: String, imageParts: List<Triple<ByteArray, String, String>>): Result<ActivityDto> {
        return try {
            val parts = imageParts.map { (bytes, mimeType, fileName) ->
                val requestBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
                MultipartBody.Part.createFormData("files", fileName, requestBody)
            }
            val result = apiService.uploadActivityImages(id, parts)
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(Exception(handleError(e)))
        }
    }

    override suspend fun getBookedUsers(id: String): Result<List<it.unical.ea.dtos.user.UserDTO>> {
        return try {
            val result = apiService.getBookedUsers(id)
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(Exception(handleError(e)))
        }
    }

    override suspend fun deleteActivity(id: String): Result<Unit> {
        return try {
            apiService.deleteActivity(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception(handleError(e)))
        }
    }

    override suspend fun isActivityBooked(id: String): Result<Boolean> {
        return try {
            val result = apiService.isActivityBooked(id)
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(Exception(handleError(e)))
        }
    }

    override suspend fun bookActivity(id: String): Result<it.unical.ea.dtos.payment.PaymentIntentResponseDto> {
        return try {
            val result = apiService.bookActivity(id)
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(Exception(handleError(e)))
        }
    }

    override suspend fun confirmActivityBooking(bookingId: String): Result<Unit> {
        return try {
            apiService.confirmActivityBooking(bookingId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception(handleError(e)))
        }
    }

    override suspend fun cancelActivityBooking(id: String): Result<Unit> {
        return try {
            apiService.cancelActivityBooking(id)
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
