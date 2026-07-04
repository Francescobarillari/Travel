package com.travel.app.data.repository

import com.google.gson.Gson
import com.travel.app.data.dto.ErrorResponseDto
import it.unical.ea.dtos.authDto.LoginRequest
import it.unical.ea.dtos.authDto.SignupRequest
import it.unical.ea.enums.UserType
import com.travel.app.data.session.SessionManager
import com.travel.app.domain.model.User
import com.travel.app.domain.model.toDomain
import com.travel.app.domain.model.toDto
import com.travel.app.domain.repository.UserRepository
import com.travel.app.service.ApiService
import retrofit2.HttpException
import java.io.IOException
import okhttp3.MediaType.Companion.toMediaTypeOrNull

class UserRepositoryImpl(
    private val apiService: ApiService,
    private val sessionManagerProvider: () -> SessionManager
) : UserRepository {

    override suspend fun login(email: String, password: String, captchaToken: String?): Result<User> {
        // Credenziali hardcoded per test offline/sviluppo locale
        if (email == "test@travel.com" && password == "travel") {
            val user = User(
                email = email,
                userType = "VIAGGIATORE",
                phone = "6895312",
                name = "Charlotte king",
                password = password
            )
            saveSession(user, "mock_test_token")
            return Result.success(user)
        }
        if (email == "societa@travel.com" && password == "travel") {
            val user = User(
                email = email,
                userType = "SOCIETA",
                phone = "0984123456",
                name = "Travel Agency S.r.l.",
                password = password
            )
            saveSession(user, "mock_societa_token")
            return Result.success(user)
        }

        return try {
            val jwtResponse = apiService.login(LoginRequest().apply {
                this.email = email
                this.password = password
                this.captchaToken = captchaToken
            })
            
            val token = jwtResponse.accessToken
            val refreshToken = jwtResponse.refreshToken
            
            // 1. Salva i token in sessione per abilitare l'AuthInterceptor per le chiamate successive
            sessionManagerProvider().saveTokens(token, refreshToken)

            // 2. Chiama l'endpoint me per caricare i dettagli completi del profilo dal DB
            val userDto = apiService.getMe()
            val user = userDto.toDomain().copy(password = password)
            
            // 3. Salva la sessione aggiornata con i dati definitivi
            saveSession(user, token, refreshToken)
            
            Result.success(user)
        } catch (e: Exception) {
            if (e is HttpException && e.code() == 403) {
                Result.failure(CaptchaRequiredException(handleError(e)))
            } else {
                Result.failure(Exception(handleError(e)))
            }
        }
    }

    override suspend fun registerViaggiatoreUser(
        email: String,
        firstName: String,
        lastName: String,
        password: String,
        phone: String?,
        captchaToken: String?
    ): Result<User> {
        return try {
            apiService.register(
                SignupRequest().apply {
                    this.email = email
                    this.password = password
                    this.userType = UserType.VIAGGIATORE
                    this.firstName = firstName
                    this.lastName = lastName
                    this.phone = phone
                    this.captchaToken = captchaToken
                }
            )
            Result.success(
                User(
                    email = email,
                    userType = "VIAGGIATORE",
                    phone = phone,
                    name = "$firstName $lastName",
                    password = password
                )
            )
        } catch (e: Exception) {
            Result.failure(Exception(handleError(e)))
        }
    }

    override suspend fun registerSocietaUser(
        email: String,
        companyName: String,
        vatNumber: String,
        password: String,
        phone: String?,
        captchaToken: String?,
        documentPhotos: List<String>
    ): Result<User> {
        return try {
            apiService.register(
                SignupRequest().apply {
                    this.email = email
                    this.password = password
                    this.userType = UserType.SOCIETA
                    this.companyName = companyName
                    this.vatNumber = vatNumber
                    this.phone = phone
                    this.captchaToken = captchaToken
                    this.documentPhotos = ArrayList(documentPhotos)
                }
            )
            Result.success(
                User(
                    email = email,
                    userType = "SOCIETA",
                    phone = phone,
                    name = companyName,
                    password = password,
                    vatNumber = vatNumber
                )
            )
        } catch (e: Exception) {
            Result.failure(Exception(handleError(e)))
        }
    }

    override fun getSessionUser(): User? {
        return sessionManagerProvider().getSessionUser()
    }

    override fun saveSession(user: User, token: String) {
        sessionManagerProvider().saveSession(user, token)
    }

    override fun saveSession(user: User, accessToken: String, refreshToken: String) {
        sessionManagerProvider().saveSession(user, accessToken, refreshToken)
    }

    override fun logout() {
        sessionManagerProvider().clearSession()
    }

    override suspend fun getMe(): Result<User> {
        return try {
            val userDto = apiService.getMe()
            val user = userDto.toDomain()
            val token = sessionManagerProvider().getSessionToken().orEmpty()
            saveSession(user, token)
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(Exception(handleError(e)))
        }
    }

    override suspend fun updateMe(user: User): Result<User> {
        return try {
            val dtoToSend = user.toDto().apply { password = user.password }
            val returnedDto = apiService.updateMe(dtoToSend)
            val updatedUser = returnedDto.toDomain().copy(password = user.password)
            val token = sessionManagerProvider().getSessionToken().orEmpty()
            saveSession(updatedUser, token)
            Result.success(updatedUser)
        } catch (e: Exception) {
            Result.failure(Exception(handleError(e)))
        }
    }

    override suspend fun uploadDocument(fileBytes: ByteArray, filename: String): Result<String> {
        return try {
            val extension = filename.substringAfterLast('.', "").lowercase()
            val mimeType = when (extension) {
                "png" -> "image/png"
                "webp" -> "image/webp"
                else -> "image/jpeg"
            }
            val mediaType = mimeType.toMediaTypeOrNull()
            val requestBody = okhttp3.RequestBody.Companion.create(mediaType, fileBytes)
            val part = okhttp3.MultipartBody.Part.createFormData("file", filename, requestBody)
            val path = apiService.uploadDocument(part)
            Result.success(path)
        } catch (e: Exception) {
            Result.failure(Exception(handleError(e)))
        }
    }

    override suspend fun getAllCompanies(): Result<List<User>> {
        return try {
            val list = apiService.getAllCompanies().map { it.toDomain() }
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(Exception(handleError(e)))
        }
    }

    override suspend fun blockCompany(id: String): Result<Unit> {
        return try {
            apiService.blockCompany(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception(handleError(e)))
        }
    }

    override suspend fun unblockCompany(id: String): Result<Unit> {
        return try {
            apiService.unblockCompany(id)
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

class CaptchaRequiredException(message: String) : Exception(message)