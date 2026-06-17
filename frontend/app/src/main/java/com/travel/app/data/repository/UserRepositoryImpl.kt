package com.travel.app.data.repository

import com.travel.app.data.dto.LoginRequestDto
import com.travel.app.data.dto.SignUpRequestDto
import com.travel.app.domain.model.User
import com.travel.app.domain.repository.UserRepository
import com.travel.app.service.ApiService

class UserRepositoryImpl(
    private val apiService: ApiService
) : UserRepository {
    override suspend fun login(email: String, password: String): Result<User> {
        return try {
            val response = apiService.login(LoginRequestDto(email, password))
            // Al momento LoginResponseDto restituisce solo il token.
            // Restituiamo un oggetto User con l'email fornita.
            // In futuro si potrebbe chiamare un endpoint /me per i dettagli completi.
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
            val response = apiService.register(
                SignUpRequestDto(
                    firstName = firstName,
                    lastName = lastName,
                    email = email,
                    password = password
                )
            )
            // UserDto restituito dal signup contiene firstName e lastName
            Result.success(
                User(
                    email = response.email,
                    username = "${response.firstName} ${response.lastName}"
                )
            )
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Errore durante la registrazione"))
        }
    }
}
