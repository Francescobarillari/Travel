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

    private val _showCheckoutSummary = MutableStateFlow(false)
    val showCheckoutSummary: StateFlow<Boolean> = _showCheckoutSummary

    private val _bookingId = MutableStateFlow<String?>(null)
    val bookingId: StateFlow<String?> = _bookingId

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _isBooked = MutableStateFlow(false)
    val isBooked: StateFlow<Boolean> = _isBooked

    private val _showSummaryDialog = MutableStateFlow(false)
    val showSummaryDialog: StateFlow<Boolean> = _showSummaryDialog

    private var currentBookingId: String? = null

    fun checkIsBooked(itineraryId: String) {
        viewModelScope.launch {
            try {
                val status = apiService.isItineraryBooked(itineraryId)
                if (status) {
                    _isBooked.value = true
                }
            } catch (e: Exception) {
                // Ignore failure to fetch status, defaults to false
            }
        }
    }

    fun bookItinerary(itineraryId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val response = apiService.bookItinerary(itineraryId)
                currentBookingId = response.bookingId
                _bookingId.value = response.bookingId
                _paymentClientSecret.value = response.clientSecret
                // Mostra sempre il checkout summary (sia gratuito che a pagamento)
                _showCheckoutSummary.value = true
            } catch (e: Exception) {
                _error.value = "Errore durante la prenotazione: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun confirmBooking() {
        val bookingId = currentBookingId ?: return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                apiService.confirmItineraryBooking(bookingId)
                _isBooked.value = true
                _showSummaryDialog.value = true
                _error.value = "Prenotazione confermata!"
                _showCheckoutSummary.value = false
            } catch (e: Exception) {
                _error.value = "Errore durante la conferma: ${e.message}"
            } finally {
                _isLoading.value = false
                currentBookingId = null
                _bookingId.value = null
                _paymentClientSecret.value = null
            }
        }
    }

    fun cancelBooking(itineraryId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                apiService.cancelItineraryBooking(itineraryId)
            } catch (_: Exception) {
                // Ignora errori, chiudi comunque la schermata
            } finally {
                _showCheckoutSummary.value = false
                currentBookingId = null
                _bookingId.value = null
                _paymentClientSecret.value = null
                _isLoading.value = false
            }
        }
    }

    fun confirmPaymentSuccess() {
        confirmBooking()
    }

    fun clearClientSecret() {
        _paymentClientSecret.value = null
        currentBookingId = null
        _bookingId.value = null
    }

    fun onSummaryDialogDismissed() {
        _showSummaryDialog.value = false
    }
}
