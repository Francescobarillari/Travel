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
        captchaToken: String? = null,
        documentPhotos: List<String> = emptyList()
    ): Result<User>

    fun getSessionUser(): User?
    fun saveSession(user: User, token: String)
    fun saveSession(user: User, accessToken: String, refreshToken: String) {
        saveSession(user, accessToken)
    }
    fun logout()

    suspend fun getMe(): Result<User>
    suspend fun updateMe(user: User): Result<User>
    suspend fun deleteAccount(userId: String): Result<Unit>
    suspend fun uploadAvatar(userId: String, imageBytes: ByteArray, mimeType: String, fileName: String): Result<User>
    suspend fun uploadDocument(fileBytes: ByteArray, filename: String): Result<String>
    suspend fun getAllCompanies(): Result<List<User>>
    suspend fun blockCompany(id: String): Result<Unit>
    suspend fun unblockCompany(id: String): Result<Unit>
}
