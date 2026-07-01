package com.travel.app.presentation.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.travel.app.domain.model.User
import com.travel.app.domain.repository.UserRepository
import kotlinx.coroutines.launch

class EditProfileViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    var initialUser by mutableStateOf<User?>(null)
    
    // Form fields state
    var name by mutableStateOf("")
    var email by mutableStateOf("")
    var username by mutableStateOf("")
    var vatNumber by mutableStateOf("")
    
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var successMessage by mutableStateOf<String?>(null)

    fun initialize(user: User) {
        if (initialUser == null) {
            initialUser = user
            name = user.name.orEmpty()
            email = user.email
            username = user.username
            vatNumber = user.vatNumber.orEmpty()
        }
    }

    fun saveProfile(onSuccess: (User) -> Unit) {
        val currentUser = initialUser ?: return
        val isSocieta = currentUser.userType == "SOCIETA"
        val isMockUser = currentUser.email in listOf("test@travel.com", "societa@travel.com", "johnkinggraphics@gmail.com")

        val updatedUser = currentUser.copy(
            name = name,
            username = username,
            vatNumber = if (isSocieta) vatNumber else null
        )

        isLoading = true
        errorMessage = null
        successMessage = null

        if (isMockUser) {
            isLoading = false
            initialUser = updatedUser
            successMessage = "Profilo salvato con successo"
            onSuccess(updatedUser)
            return
        }

        viewModelScope.launch {
            val result = userRepository.updateMe(updatedUser)
            isLoading = false
            result.fold(
                onSuccess = { savedUser ->
                    initialUser = savedUser
                    successMessage = "Profilo salvato con successo"
                    onSuccess(savedUser)
                },
                onFailure = { throwable ->
                    errorMessage = throwable.message ?: "Errore di salvataggio"
                }
            )
        }
    }
}
