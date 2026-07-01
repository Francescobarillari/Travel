package com.travel.app.presentation.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.travel.app.domain.model.User
import com.travel.app.domain.repository.UserRepository
import kotlinx.coroutines.launch

class SecurityViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    var initialUser by mutableStateOf<User?>(null)
    
    // Form fields state
    var oldPassword by mutableStateOf("")
    var newPassword by mutableStateOf("")
    
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var successMessage by mutableStateOf<String?>(null)

    fun initialize(user: User) {
        if (initialUser == null) {
            initialUser = user
            oldPassword = ""
            newPassword = ""
            errorMessage = null
            successMessage = null
        }
    }

    fun saveSecurity(onSuccess: (User) -> Unit) {
        val currentUser = initialUser ?: return

        val hasNewPassword = newPassword.isNotEmpty()
        
        if (hasNewPassword) {
            if (oldPassword.isEmpty()) {
                errorMessage = "Inserisci la vecchia password per procedere con il cambio"
                return
            }
            // Controllo robustezza password (min 8 caratteri, una maiuscola, una minuscola, un numero)
            if (newPassword.length < 8 || !newPassword.matches(Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$"))) {
                errorMessage = "La nuova password deve contenere almeno 8 caratteri, una maiuscola, una minuscola e un numero"
                return
            }
        } else {
            errorMessage = "Inserisci una nuova password per procedere"
            return
        }

        isLoading = true
        errorMessage = null
        successMessage = null

        val isMockUser = currentUser.email in listOf("test@travel.com", "societa@travel.com", "johnkinggraphics@gmail.com")

        viewModelScope.launch {
            // Se sta cambiando password, dobbiamo prima verificare la vecchia password
            if (isMockUser) {
                val expectedOld = currentUser.password ?: "travel"
                if (oldPassword != expectedOld && oldPassword != "travel") {
                    errorMessage = "La vecchia password non è corretta"
                    isLoading = false
                    return@launch
                }
            } else {
                val loginResult = userRepository.login(currentUser.email, oldPassword)
                if (loginResult.isFailure) {
                    errorMessage = "La vecchia password non è corretta"
                    isLoading = false
                    return@launch
                }
            }

            // Procediamo all'aggiornamento della password
            val updatedUser = currentUser.copy(
                password = newPassword
            )

            if (isMockUser) {
                isLoading = false
                initialUser = updatedUser
                oldPassword = ""
                newPassword = ""
                successMessage = "Password aggiornata con successo"
                onSuccess(updatedUser)
                return@launch
            }

            val result = userRepository.updateMe(updatedUser)
            isLoading = false
            result.fold(
                onSuccess = { savedUser ->
                    initialUser = savedUser.copy(password = newPassword)
                    oldPassword = ""
                    newPassword = ""
                    successMessage = "Password aggiornata con successo"
                    onSuccess(initialUser!!)
                },
                onFailure = { throwable ->
                    errorMessage = throwable.message ?: "Errore di salvataggio"
                }
            )
        }
    }
}
