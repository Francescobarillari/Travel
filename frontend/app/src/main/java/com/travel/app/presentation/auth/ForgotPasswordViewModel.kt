package com.travel.app.presentation.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.travel.app.domain.repository.UserRepository
import kotlinx.coroutines.launch

class ForgotPasswordViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    var email by mutableStateOf("")
    var otp by mutableStateOf("")
    var newPassword by mutableStateOf("")
    var confirmPassword by mutableStateOf("")

    var isLoading by mutableStateOf(false)
    var error by mutableStateOf<String?>(null)
    var successMessage by mutableStateOf<String?>(null)
    var otpSent by mutableStateOf(false)

    fun sendOtp(onSuccess: () -> Unit = {}) {
        if (email.isBlank()) {
            error = "Inserisci un indirizzo email"
            return
        }
        viewModelScope.launch {
            isLoading = true
            error = null
            successMessage = null
            userRepository.forgotPassword(email).fold(
                onSuccess = { msg ->
                    isLoading = false
                    otpSent = true
                    successMessage = msg
                    onSuccess()
                },
                onFailure = { throwable ->
                    isLoading = false
                    error = throwable.message ?: "Errore nell'invio dell'OTP"
                }
            )
        }
    }

    fun resetPassword(onSuccess: () -> Unit) {
        if (otp.length != 6) {
            error = "Il codice OTP deve essere di 6 cifre"
            return
        }
        if (newPassword.isBlank()) {
            error = "Inserisci la nuova password"
            return
        }
        if (newPassword.length < 8 || !newPassword.matches(Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$"))) {
            error = "La password deve contenere almeno 8 caratteri, una maiuscola, una minuscola e un numero"
            return
        }
        if (newPassword != confirmPassword) {
            error = "Le password non coincidono"
            return
        }

        viewModelScope.launch {
            isLoading = true
            error = null
            successMessage = null
            userRepository.resetPassword(email, otp, newPassword).fold(
                onSuccess = { msg ->
                    isLoading = false
                    successMessage = msg
                    // Reset dei campi
                    email = ""
                    otp = ""
                    newPassword = ""
                    confirmPassword = ""
                    otpSent = false
                    onSuccess()
                },
                onFailure = { throwable ->
                    isLoading = false
                    error = throwable.message ?: "Errore durante il ripristino della password"
                }
            )
        }
    }

    fun clearState() {
        email = ""
        otp = ""
        newPassword = ""
        confirmPassword = ""
        isLoading = false
        error = null
        successMessage = null
        otpSent = false
    }
}
