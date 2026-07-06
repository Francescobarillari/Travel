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

    private var currentBookingId: String? = null

    fun bookItinerary(itineraryId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val response = apiService.bookItinerary(itineraryId)
                if (response.clientSecret != null) {
                    currentBookingId = response.bookingId
                    _paymentClientSecret.value = response.clientSecret
                } else {
                    // It was a free itinerary, mock payment or something
                    _error.value = "Prenotazione confermata!"
                }
            } catch (e: Exception) {
                _error.value = "Errore durante la prenotazione: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun confirmPaymentSuccess() {
        val bookingId = currentBookingId ?: return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                apiService.confirmItineraryBooking(bookingId)
                _error.value = "Prenotazione confermata!"
            } catch (e: Exception) {
                _error.value = "Errore durante la conferma: ${e.message}"
            } finally {
                _isLoading.value = false
                currentBookingId = null
                _paymentClientSecret.value = null
            }
        }
    }

    fun clearClientSecret() {
        _paymentClientSecret.value = null
        currentBookingId = null
    }
}
