package com.travel.app.data.repository

import com.google.gson.Gson
import com.travel.app.data.dto.ErrorResponseDto
import com.travel.app.data.dto.LoginRequest
import com.travel.app.data.dto.SignupRequest
import com.travel.app.domain.model.User
import com.travel.app.domain.repository.UserRepository
import com.travel.app.service.ApiService
import retrofit2.HttpException
import java.io.IOException

class UserRepositoryImpl(
    private val apiService: ApiService
) : UserRepository {

    override suspend fun login(email: String, password: String): Result<User> {
        // Testing frontend
        if (email == "test@travel.com" && password == "travel") {
            return Result.success(User(email = email, username = "Test User"))
        }
        return try {
            val token = apiService.login(LoginRequest(email = email, password = password))
            // Il login ha avuto successo e abbiamo il token.
            // Per ora restituiamo l'utente con l'email usata.
            Result.success(User(email = email, username = email.split("@")[0]))
        } catch (e: Exception) {
            Result.failure(Exception(handleError(e)))
        }
    }

    override suspend fun registerViaggiatoreUser(
        email: String,
        firstName: String,
        lastName: String,
        password: String,
        phone: String?
    ): Result<User> {
        return try {
            apiService.register(
                SignupRequest(
                    email = email,
                    password = password,
                    userType = SignupRequest.UserType.vIAGGIATORE,
                    firstName = firstName,
                    lastName = lastName,
                    phone = phone
                )
            )
            Result.success(User(email = email, username = "$firstName $lastName"))
        } catch (e: Exception) {
            Result.failure(Exception(handleError(e)))
        }
    }

    override suspend fun registerSocietaUser(
        email: String,
        companyName: String,
        vatNumber: String,
        password: String,
        phone: String?
    ): Result<User> {
        return try {
            apiService.register(
                SignupRequest(
                    email = email,
                    password = password,
                    userType = SignupRequest.UserType.sOCIETA,
                    companyName = companyName,
                    vatNumber = vatNumber,
                    phone = phone
                )
            )
            Result.success(User(email = email, username = companyName))
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
                    when (e.code()) {
                        401 -> "Credenziali non valide"
                        409 -> "Utente già esistente"
                        else -> "Errore del server: ${e.code()}"
                    }
                }
            }
            is IOException -> "Nessuna connessione internet"
            else -> e.message ?: "Errore imprevisto"
        }
    }
}