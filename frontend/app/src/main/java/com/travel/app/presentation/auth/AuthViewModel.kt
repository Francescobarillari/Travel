package com.travel.app.presentation.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.travel.app.domain.model.User
import com.travel.app.domain.repository.UserRepository
import kotlinx.coroutines.launch

class AuthViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    // Stato del form di Login
    var loginEmail by mutableStateOf("")
    var loginPassword by mutableStateOf("")
    var loginError by mutableStateOf<String?>(null)

    // Stato del form di Registrazione
    var registerEmail by mutableStateOf("")
    var registerFirstName by mutableStateOf("")
    var registerLastName by mutableStateOf("")
    var registerPassword by mutableStateOf("")
    var registerConfirmPassword by mutableStateOf("")
    var registerError by mutableStateOf<String?>(null)

    var isLoading by mutableStateOf(false)

    fun login(onSuccess: (User) -> Unit) {
        if (loginEmail.isBlank() || loginPassword.isBlank()) {
            loginError = "Compila tutti i campi"
            return
        }
        viewModelScope.launch {
            isLoading = true
            loginError = null
            val result = userRepository.login(loginEmail, loginPassword)
            isLoading = false
            result.fold(
                onSuccess = { user ->
                    loginEmail = ""
                    loginPassword = ""
                    onSuccess(user)
                },
                onFailure = { throwable ->
                    loginError = throwable.message ?: "Errore di autenticazione"
                }
            )
        }
    }

    fun register(onSuccess: (User) -> Unit) {
        if (registerEmail.isBlank() || registerFirstName.isBlank() || 
            registerLastName.isBlank() || registerPassword.isBlank()) {
            registerError = "Compila tutti i campi"
            return
        }
        if (registerPassword != registerConfirmPassword) {
            registerError = "Le password non coincidono"
            return
        }
        
        viewModelScope.launch {
            isLoading = true
            registerError = null
            val result = userRepository.register(
                registerEmail, 
                registerFirstName, 
                registerLastName, 
                registerPassword
            )
            isLoading = false
            result.fold(
                onSuccess = { user ->
                    registerEmail = ""
                    registerFirstName = ""
                    registerLastName = ""
                    registerPassword = ""
                    registerConfirmPassword = ""
                    onSuccess(user)
                },
                onFailure = { throwable ->
                    registerError = throwable.message ?: "Errore di registrazione"
                }
            )
        }
    }
}
