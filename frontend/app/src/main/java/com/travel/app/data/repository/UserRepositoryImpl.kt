package com.travel.app.data.repository

import com.travel.app.data.dto.LoginRequest
import com.travel.app.data.dto.SignupRequest
import com.travel.app.domain.model.User
import com.travel.app.domain.repository.UserRepository
import com.travel.app.service.ApiService

class UserRepositoryImpl(
    private val apiService: ApiService
) : UserRepository {
    override suspend fun login(email: String, password: String): Result<User> {
        return try {
            val token = apiService.login(LoginRequest(email = email, password = password))
            // Il login ha avuto successo e abbiamo il token.
            // Per ora restituiamo l'utente con l'email usata.
            Result.success(User(email = email, username = email.split("@")[0]))
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Errore durante il login"))
        }
    }

    override suspend fun register(
        email: String,
        firstName: String,
        lastName: String,
        password: String
    ): Result<User> {
        return try {
            val resultMessage = apiService.register(
                SignupRequest(
                    firstName = firstName,
                    lastName = lastName,
                    email = email,
                    password = password
                )
            )
            // Se la registrazione ha successo, restituiamo l'utente creato.
            Result.success(
                User(
                    email = email,
                    username = "$firstName $lastName"
                )
            )
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Errore durante la registrazione"))
        }
    }
}
