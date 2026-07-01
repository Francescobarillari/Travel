package com.travel.app.presentation.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.travel.app.domain.repository.ItineraryRepository
import it.unical.ea.dtos.itinerary.ItineraryDto
import kotlinx.coroutines.launch

class CompanyDashboardViewModel(
    private val itineraryRepository: ItineraryRepository
) : ViewModel() {

    var itineraries by mutableStateOf<List<ItineraryDto>>(emptyList())

    var isLoading by mutableStateOf(false)

    var errorMessage by mutableStateOf<String?>(null)

    fun fetchItineraries() {
        isLoading = true
        errorMessage = null
        viewModelScope.launch {
            try {
                val result = itineraryRepository.getItineraries()
                result.fold(
                    onSuccess = { list ->
                        itineraries = list
                    },
                    onFailure = { throwable ->
                        errorMessage = throwable.message ?: "Impossibile caricare gli itinerari"
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
