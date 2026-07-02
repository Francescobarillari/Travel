package com.travel.app.domain.repository

import com.travel.app.domain.model.User

interface UserRepository {
    suspend fun login(email: String, password: String, captchaToken: String? = null): Result<User>
    suspend fun registerViaggiatoreUser(
        email: String,
        firstName: String,
        lastName: String,
        password: String,
        phone: String? = null,
        captchaToken: String? = null
    ): Result<User>
    suspend fun registerSocietaUser(
        email: String,
        companyName: String,
        vatNumber: String,
        password: String,
        phone: String? = null,
        captchaToken: String? = null
    ): Result<User>

    fun getSessionUser(): User?
    fun saveSession(user: User, token: String)
    fun logout()

    suspend fun getMe(): Result<User>
    suspend fun updateMe(user: User): Result<User>
}
