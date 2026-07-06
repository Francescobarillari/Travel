package com.travel.app.presentation.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.travel.app.domain.repository.ActivityRepository
import com.travel.app.domain.repository.UserRepository
import it.unical.ea.dtos.activity.ActivityDto
import kotlinx.coroutines.launch

class CompanyDashboardViewModel(
    private val activityRepository: ActivityRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    var activities by mutableStateOf<List<ActivityDto>>(emptyList())
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    val currentOrganizerName: String
        get() = try {
            userRepository.getSessionUser()?.name ?: ""
        } catch (e: Exception) {
            ""
        }

    fun fetchDashboardData() {
        isLoading = true
        errorMessage = null
        viewModelScope.launch {
            try {
                // Carica Attività e filtra per l'organizzatore corrente
                val actResult = activityRepository.getActivities()
                actResult.onSuccess { list ->
                    val orgName = currentOrganizerName
                    activities = list.filter { it.organizer == orgName }
                }
            } catch (e: Exception) {
                errorMessage = e.message ?: "Errore durante il caricamento dei dati"
            } finally {
                isLoading = false
            }
        }
    }
}
