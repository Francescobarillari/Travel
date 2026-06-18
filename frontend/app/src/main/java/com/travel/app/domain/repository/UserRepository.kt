package com.travel.app.domain.repository

import com.travel.app.domain.model.User

interface UserRepository {
    suspend fun login(email: String, password: String): Result<User>
    suspend fun register(email: String, firstName: String, lastName: String, password: String): Result<User>
}
