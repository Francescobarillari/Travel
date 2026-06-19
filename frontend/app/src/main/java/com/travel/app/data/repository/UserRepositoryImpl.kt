package com.travel.app.data.repository

import com.google.gson.Gson
import com.travel.app.data.dto.ErrorResponseDto
import com.travel.app.data.dto.LoginRequestDto
import com.travel.app.data.dto.SignUpRequestDto
import com.travel.app.domain.model.User
import com.travel.app.domain.repository.UserRepository
import com.travel.app.service.ApiService
import retrofit2.HttpException
import java.io.IOException

class UserRepositoryImpl(
    private val apiService: ApiService
) : UserRepository {

    override suspend fun login(email: String, password: String): Result<User> {
        return try {
            apiService.login(LoginRequestDto(email, password))
            Result.success(User(email = email, username = email.split("@")[0]))
        } catch (e: Exception) {
            Result.failure(Exception(handleError(e)))
        }
    }

    override suspend fun register(
        email: String,
        firstName: String,
        lastName: String,
        password: String
    ): Result<User> {
        return try {
            apiService.register(
                SignUpRequestDto(
                    firstName = firstName,
                    lastName = lastName,
                    email = email,
                    password = password
                )
            )
            Result.success(User(email = email, username = "$firstName $lastName"))
        } catch (e: Exception) {
            Result.failure(Exception(handleError(e)))
        }
    }

    private fun handleError(e: Exception): String {
        return when (e) {
            is HttpException -> {
                val errorBody = e.response()?.errorBody()?.string()
                try {
                    // Prova a leggere il messaggio dal campo "error" del JSON
                    val errorDto = Gson().fromJson(errorBody, ErrorResponseDto::class.java)
                    errorDto.error ?: "Errore del server (${e.code()})"
                } catch (parseException: Exception) {
                    // Fallback se il JSON non è nel formato atteso
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
