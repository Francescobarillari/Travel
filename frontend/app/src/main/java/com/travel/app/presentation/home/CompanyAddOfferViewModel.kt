package com.travel.app.presentation.home

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.travel.app.domain.repository.ActivityRepository
import com.travel.app.domain.repository.UserRepository
import it.unical.ea.dtos.activity.ActivityDto
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.Calendar

class CompanyAddOfferViewModel(
    private val activityRepository: ActivityRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    val defaultOrganizer: String by lazy {
        try {
            userRepository.getSessionUser()?.name ?: ""
        } catch (e: Exception) {
            ""
        }
    }

    // Form fields state
    var description by mutableStateOf("")
    var location by mutableStateOf("")
    
    var startYear by mutableStateOf(0)
    var startMonth by mutableStateOf(0)
    var startDay by mutableStateOf(0)
    var startHour by mutableStateOf(0)
    var startMinute by mutableStateOf(0)

    var endYear by mutableStateOf(0)
    var endMonth by mutableStateOf(0)
    var endDay by mutableStateOf(0)
    var endHour by mutableStateOf(0)
    var endMinute by mutableStateOf(0)

    var maxParticipantsText by mutableStateOf("")
    var priceText by mutableStateOf("")

    // Selected images list
    val selectedImages = mutableStateListOf<Uri>()

    // UI Status state
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var showSuccessDialog by mutableStateOf(false)

    fun resetForm() {
        description = ""
        location = ""
        startYear = 0
        startMonth = 0
        startDay = 0
        startHour = 0
        startMinute = 0
        endYear = 0
        endMonth = 0
        endDay = 0
        endHour = 0
        endMinute = 0
        maxParticipantsText = ""
        priceText = ""
        selectedImages.clear()
        errorMessage = null
    }

    fun submitActivity() {
        // Validation
        if (location.isBlank()) {
            errorMessage = "La posizione dell'attività è obbligatoria"
            return
        }
        if (startYear == 0) {
            errorMessage = "La data di inizio è obbligatoria"
            return
        }
        if (endYear == 0) {
            errorMessage = "La data di fine è obbligatoria"
            return
        }

        // Compare dates
        val startCal = Calendar.getInstance().apply {
            set(startYear, startMonth - 1, startDay, startHour, startMinute)
        }
        val endCal = Calendar.getInstance().apply {
            set(endYear, endMonth - 1, endDay, endHour, endMinute)
        }
        if (startCal.after(endCal)) {
            errorMessage = "La data di inizio deve essere precedente alla data di fine"
            return
        }

        val participants = maxParticipantsText.toIntOrNull()
        if (participants == null || participants < 1) {
            errorMessage = "Il numero massimo di partecipanti deve essere almeno 1"
            return
        }

        val priceVal = priceText.toDoubleOrNull()
        if (priceVal == null || priceVal < 0.0) {
            errorMessage = "Il prezzo non può essere negativo o vuoto"
            return
        }

        isLoading = true
        errorMessage = null

        viewModelScope.launch {
            try {
                // Create LocalDateTime objects for DTO
                val startLdt = LocalDateTime.of(startYear, startMonth, startDay, startHour, startMinute)
                val endLdt = LocalDateTime.of(endYear, endMonth, endDay, endHour, endMinute)

                val activityName = if (defaultOrganizer.isNotBlank()) defaultOrganizer else "Attività"

                val activityDto = ActivityDto()
                activityDto.setName(activityName)
                activityDto.setDescription(if (description.isNotBlank()) description else null)
                activityDto.setLocation(location)
                activityDto.setStartTime(startLdt)
                activityDto.setEndTime(endLdt)
                activityDto.setParticipants(participants)
                activityDto.setPrice(BigDecimal.valueOf(priceVal))
                activityDto.setOrganizer(defaultOrganizer)

                val result = activityRepository.createActivity(activityDto)
                result.onSuccess {
                    showSuccessDialog = true
                    resetForm()
                }.onFailure { e ->
                    errorMessage = e.message ?: "Si è verificato un errore durante la pubblicazione"
                }
            } catch (e: Exception) {
                errorMessage = e.message ?: "Errore imprevisto"
            } finally {
                isLoading = false
            }
        }
    }
}
