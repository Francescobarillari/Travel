package com.travel.app.presentation.components.itinerary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.travel.app.data.AppContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ItineraryDetailViewModel : ViewModel() {
    
    private val apiService = AppContainer.apiService

    private val _paymentClientSecret = MutableStateFlow<String?>(null)
    val paymentClientSecret: StateFlow<String?> = _paymentClientSecret

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun bookItinerary(itineraryId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val response = apiService.bookItinerary(itineraryId)
                if (response.clientSecret != null) {
                    _paymentClientSecret.value = response.clientSecret
                } else {
                    // It was a free itinerary or something
                    _error.value = "Prenotazione confermata (Gratuita)"
                }
            } catch (e: Exception) {
                _error.value = "Errore durante la prenotazione: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearClientSecret() {
        _paymentClientSecret.value = null
    }
}
