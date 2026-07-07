package com.travel.app.presentation.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.travel.app.domain.model.User
import com.travel.app.domain.repository.UserRepository
import kotlinx.coroutines.launch
import android.content.Context
import android.net.Uri
import java.io.File

class EditProfileViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    var initialUser by mutableStateOf<User?>(null)
    
    // Form fields state
    var name by mutableStateOf("")
    var email by mutableStateOf("")
    var vatNumber by mutableStateOf("")
    
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var successMessage by mutableStateOf<String?>(null)

    fun initialize(user: User) {
        if (initialUser == null) {
            initialUser = user
            name = user.name.orEmpty()
            email = user.email
            vatNumber = user.vatNumber.orEmpty()
        }
    }

    fun saveProfile(onSuccess: (User) -> Unit) {
        val currentUser = initialUser ?: return
        val isSocieta = currentUser.userType == "SOCIETA"
        val isMockUser = currentUser.email in listOf("test@travel.com", "societa@travel.com", "johnkinggraphics@gmail.com")

        val updatedUser = currentUser.copy(
            name = if (isSocieta) vatNumber else name,
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

    fun uploadAvatar(context: Context, uri: Uri, onSuccess: (User) -> Unit) {
        val currentUser = initialUser ?: return
        val isMockUser = currentUser.email in listOf("test@travel.com", "societa@travel.com", "johnkinggraphics@gmail.com")

        if (isMockUser) {
            errorMessage = "Impossibile caricare l'avatar per l'utente di test."
            return
        }

        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            successMessage = null

            try {
                // Leggi i byte dall'URI
                val inputStream = context.contentResolver.openInputStream(uri)
                val bytes = inputStream?.readBytes()
                inputStream?.close()

                if (bytes != null) {
                    val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
                    val result = userRepository.uploadAvatar(currentUser.id.orEmpty(), bytes, mimeType, "avatar.jpg")
                    
                    if (result.isSuccess) {
                        val updatedUser = result.getOrThrow()
                        initialUser = updatedUser
                        successMessage = "Foto profilo aggiornata con successo"
                        onSuccess(updatedUser)
                    } else {
                        errorMessage = result.exceptionOrNull()?.message ?: "Errore durante il caricamento della foto"
                    }
                } else {
                    errorMessage = "Impossibile leggere l'immagine"
                }
            } catch (e: Exception) {
                errorMessage = e.message ?: "Errore sconosciuto"
            } finally {
                isLoading = false
            }
        }
    }

    fun deactivateAccount(onSuccess: () -> Unit) {
        val currentUser = initialUser ?: return
        val isMockUser = currentUser.email in listOf("test@travel.com", "societa@travel.com", "johnkinggraphics@gmail.com")
        
        if (isMockUser) {
            errorMessage = "Impossibile disattivare l'account di test."
            return
        }

        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            
            val result = userRepository.deleteAccount(currentUser.id.orEmpty())
            isLoading = false
            
            result.fold(
                onSuccess = {
                    onSuccess()
                },
                onFailure = { throwable ->
                    errorMessage = throwable.message ?: "Errore durante la disattivazione"
                }
            )
        }
    }
}
