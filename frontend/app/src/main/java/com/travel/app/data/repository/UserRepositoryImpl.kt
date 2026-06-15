package com.travel.app.data.repository

import com.travel.app.data.datasource.InMemoryDataSource
import com.travel.app.domain.model.User
import com.travel.app.domain.repository.UserRepository

class UserRepositoryImpl : UserRepository {
    override fun login(email: String, password: String): Result<User> {
        val existingPassword = InMemoryDataSource.registeredUsers[email]
        return if (existingPassword != null && existingPassword == password) {
            val username = InMemoryDataSource.userProfileNames[email] ?: "Utente"
            Result.success(User(email, username))
        } else {
            Result.failure(Exception("Email o password non corretti"))
        }
    }

    override fun register(email: String, username: String, password: String): Result<User> {
        return if (email.isBlank() || username.isBlank() || password.isBlank()) {
            Result.failure(Exception("Tutti i campi sono obbligatori"))
        } else if (InMemoryDataSource.registeredUsers.containsKey(email)) {
            Result.failure(Exception("Email già registrata"))
        } else {
            InMemoryDataSource.registeredUsers[email] = password
            InMemoryDataSource.userProfileNames[email] = username
            Result.success(User(email, username))
        }
    }
}
