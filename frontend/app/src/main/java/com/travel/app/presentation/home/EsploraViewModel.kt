package com.travel.app.presentation.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.travel.app.domain.repository.ActivityRepository
import it.unical.ea.dtos.activity.ActivityDto
import kotlinx.coroutines.launch

class EsploraViewModel(
    private val activityRepository: ActivityRepository
) : ViewModel() {

    var searchQuery by mutableStateOf("")
    
    var activities by mutableStateOf<List<ActivityDto>>(emptyList())

    var isLoading by mutableStateOf(false)

    var errorMessage by mutableStateOf<String?>(null)

    fun fetchActivities() {
        isLoading = true
        errorMessage = null
        viewModelScope.launch {
            try {
                val result = activityRepository.getActivities()
                result.fold(
                    onSuccess = { list ->
                        activities = list
                    },
                    onFailure = { throwable ->
                        errorMessage = throwable.message ?: "Impossibile caricare le attività"
                    }
                )
            } catch (e: Exception) {
                errorMessage = e.message ?: "Errore imprevisto"
            } finally {
                isLoading = false
            }
        }
    }

    val filteredActivities: List<ActivityDto>
        get() = activities.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
            (it.description?.contains(searchQuery, ignoreCase = true) == true) ||
            it.location.contains(searchQuery, ignoreCase = true) ||
            (it.organizer?.contains(searchQuery, ignoreCase = true) == true)
        }
}
