package com.travel.app.presentation.detail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.travel.app.domain.repository.ActivityRepository
import com.travel.app.domain.repository.TripRepository
import it.unical.ea.dtos.activity.ActivityDto
import it.unical.ea.dtos.trip.TripDto
import kotlinx.coroutines.launch

class ItemDetailViewModel(
    private val tripRepository: TripRepository,
    private val activityRepository: ActivityRepository,
    private val itemId: String,
    private val isTrip: Boolean
) : ViewModel() {

    var trip by mutableStateOf<TripDto?>(null)
    var activity by mutableStateOf<ActivityDto?>(null)
    var isLoading by mutableStateOf(true)
    var errorMessage by mutableStateOf<String?>(null)

    // Simula lo stato dei preferiti (in una vera app, sarebbe nel DB legato all'utente)
    var isFavorite by mutableStateOf(false)

    init {
        loadDetail()
    }

    private fun loadDetail() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            if (isTrip) {
                tripRepository.getTripById(itemId).fold(
                    onSuccess = { trip = it },
                    onFailure = { errorMessage = it.message }
                )
            } else {
                activityRepository.getActivityById(itemId).fold(
                    onSuccess = { activity = it },
                    onFailure = { errorMessage = it.message }
                )
            }
            isLoading = false
        }
    }

    fun toggleFavorite() {
        isFavorite = !isFavorite
        // Qui si potrebbe chiamare un repository per salvare il preferito
    }

    fun bookItem() {
        // Implementazione mock per la prenotazione
    }
}
