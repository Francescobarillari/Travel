package com.travel.app.presentation.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.travel.app.domain.model.User
import com.travel.app.domain.repository.UserRepository
import com.travel.app.data.repository.CaptchaRequiredException
import kotlinx.coroutines.launch

class LoginViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    var loginEmail by mutableStateOf("")
    var loginPassword by mutableStateOf("")
    var loginError by mutableStateOf<String?>(null)
    var isCaptchaRequiredState by mutableStateOf(false)
    var isLoading by mutableStateOf(false)

    fun login(captchaToken: String? = null, onSuccess: (User) -> Unit) {
        if (loginEmail.isBlank() || loginPassword.isBlank()) {
            loginError = "Compila tutti i campi"
            return
        }
        viewModelScope.launch {
            isLoading = true
            loginError = null
            val result = userRepository.login(loginEmail, loginPassword, captchaToken)
            isLoading = false
            result.fold(
                onSuccess = { user ->
                    loginEmail = ""
                    loginPassword = ""
                    isCaptchaRequiredState = false
                    onSuccess(user)
                },
                onFailure = { throwable ->
                    if (throwable is CaptchaRequiredException) {
                        isCaptchaRequiredState = true
                    }
                    loginError = throwable.message ?: "Errore di autenticazione"
                }
            )
        }
    }
}
