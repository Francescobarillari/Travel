package com.travel.app.presentation.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.travel.app.domain.repository.ActivityRepository
import it.unical.ea.dtos.user.UserDTO
import kotlinx.coroutines.launch

class CompanyActivityBookingsViewModel(
    private val activityRepository: ActivityRepository,
    private val activityId: String
) : ViewModel() {

    var bookedUsers by mutableStateOf<List<UserDTO>>(emptyList())
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    init {
        fetchBookings()
    }

    fun fetchBookings() {
        isLoading = true
        errorMessage = null
        viewModelScope.launch {
            try {
                val result = activityRepository.getBookedUsers(activityId)
                result.fold(
                    onSuccess = { list ->
                        bookedUsers = list
                    },
                    onFailure = { throwable ->
                        errorMessage = throwable.message ?: "Impossibile caricare l'elenco degli iscritti"
                    }
                )
            } catch (e: Exception) {
                errorMessage = e.message ?: "Errore imprevisto"
            } finally {
                isLoading = false
            }
        }
    }
}
