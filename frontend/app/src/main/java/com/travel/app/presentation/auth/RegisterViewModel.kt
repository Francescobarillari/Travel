package com.travel.app.presentation.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.travel.app.domain.model.User
import com.travel.app.domain.repository.UserRepository
import kotlinx.coroutines.launch

enum class UserType { VIAGGIATORE, SOCIETA }

class RegisterViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    // Stato del form di Registrazione - Comune
    var registerUserType by mutableStateOf(UserType.VIAGGIATORE)
    var registerEmail by mutableStateOf("")
    var registerPassword by mutableStateOf("")
    var registerConfirmPassword by mutableStateOf("")
    var registerPhone by mutableStateOf("")
    var registerError by mutableStateOf<String?>(null)

    // Stato del form di Registrazione - Solo VIAGGIATORE
    var registerFirstName by mutableStateOf("")
    var registerLastName by mutableStateOf("")

    // Stato del form di Registrazione - Solo SOCIETA
    var registerCompanyName by mutableStateOf("")
    var registerVatNumber by mutableStateOf("")
    var registerDocumentPhotos by mutableStateOf<List<String>>(emptyList())
    var isUploadingDocument by mutableStateOf(false)

    var isLoading by mutableStateOf(false)

    fun uploadDocumentFile(fileBytes: ByteArray, filename: String) {
        viewModelScope.launch {
            isUploadingDocument = true
            registerError = null
            userRepository.uploadDocument(fileBytes, filename).fold(
                onSuccess = { path ->
                    registerDocumentPhotos = registerDocumentPhotos + path
                    isUploadingDocument = false
                },
                onFailure = { throwable ->
                    registerError = throwable.message ?: "Errore durante il caricamento del documento"
                    isUploadingDocument = false
                }
            )
        }
    }

    fun register(captchaToken: String, onSuccess: (User) -> Unit) {
        if (registerEmail.isBlank() || registerPassword.isBlank()) {
            registerError = "Email e password sono obbligatorie"
            return
        }
        if (registerPassword != registerConfirmPassword) {
            registerError = "Le password non coincidono"
            return
        }

        if (registerUserType == UserType.VIAGGIATORE) {
            if (registerFirstName.isBlank() || registerLastName.isBlank()) {
                registerError = "Nome e cognome sono obbligatori"
                return
            }
        } else {
            if (registerCompanyName.isBlank() || registerVatNumber.isBlank()) {
                registerError = "Ragione sociale e Partita IVA sono obbligatorie"
                return
            }
        }

        viewModelScope.launch {
            isLoading = true
            registerError = null
            val result = if (registerUserType == UserType.VIAGGIATORE) {
                userRepository.registerViaggiatoreUser(
                    email = registerEmail,
                    firstName = registerFirstName,
                    lastName = registerLastName,
                    password = registerPassword,
                    phone = registerPhone.takeIf { it.isNotBlank() },
                    captchaToken = captchaToken
                )
            } else {
                userRepository.registerSocietaUser(
                    email = registerEmail,
                    companyName = registerCompanyName,
                    vatNumber = registerVatNumber,
                    password = registerPassword,
                    phone = registerPhone.takeIf { it.isNotBlank() },
                    captchaToken = captchaToken,
                    documentPhotos = registerDocumentPhotos
                )
            }
            isLoading = false
            result.fold(
                onSuccess = { user ->
                    registerEmail = ""
                    registerFirstName = ""
                    registerLastName = ""
                    registerCompanyName = ""
                    registerVatNumber = ""
                    registerPassword = ""
                    registerConfirmPassword = ""
                    registerPhone = ""
                    registerDocumentPhotos = emptyList()
                    onSuccess(user)
                },
                onFailure = { throwable ->
                    registerError = throwable.message ?: "Errore di registrazione"
                }
            )
        }
    }
}
