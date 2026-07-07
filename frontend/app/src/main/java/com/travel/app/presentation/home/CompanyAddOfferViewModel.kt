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
import com.travel.app.domain.repository.LocalitaRepository
import it.unical.ea.dtos.activity.ActivityDto
import it.unical.ea.dtos.location.LocationDto
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.Calendar

class CompanyAddOfferViewModel(
    private val activityRepository: ActivityRepository,
    private val userRepository: UserRepository,
    private val localitaRepository: LocalitaRepository
) : ViewModel() {

    val locationSuggestions = mutableStateListOf<LocationDto>()
    private var searchJob: Job? = null

    fun fetchLocationSuggestions(query: String) {
        searchJob?.cancel()
        if (query.trim().length < 2) {
            locationSuggestions.clear()
            return
        }
        searchJob = viewModelScope.launch {
            delay(500)
            localitaRepository.searchLocalita(query.trim(), includeExternal = true).fold(
                onSuccess = { pageDto ->
                    locationSuggestions.clear()
                    locationSuggestions.addAll(pageDto.content)
                },
                onFailure = {
                    locationSuggestions.clear()
                }
            )
        }
    }

    val defaultOrganizer: String by lazy {
        try {
            userRepository.getSessionUser()?.name ?: ""
        } catch (e: Exception) {
            ""
        }
    }

    var activityId by mutableStateOf<String?>(null)
    val isEditMode: Boolean
        get() = activityId != null

    // Form fields state
    var title by mutableStateOf("")
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
        activityId = null
        title = ""
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
        if (title.isBlank()) {
            errorMessage = "Il titolo dell'attività è obbligatorio"
            return
        }
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

                val activityDto = ActivityDto()
                activityDto.setName(title.trim())
                activityDto.setDescription(if (description.isNotBlank()) description else null)
                activityDto.setLocation(location)
                activityDto.setStartTime(startLdt)
                activityDto.setEndTime(endLdt)
                activityDto.setParticipants(participants)
                activityDto.setPrice(BigDecimal.valueOf(priceVal))
                activityDto.setOrganizer(defaultOrganizer)

                val result = if (isEditMode) {
                    activityRepository.updateActivity(activityId!!, activityDto)
                } else {
                    activityRepository.createActivity(activityDto)
                }
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

    fun loadActivity(id: String) {
        activityId = id
        isLoading = true
        errorMessage = null
        viewModelScope.launch {
            try {
                val result = activityRepository.getActivityById(id)
                result.onSuccess { activity ->
                    title = activity.name ?: ""
                    description = activity.description ?: ""
                    location = activity.location ?: ""
                    
                    // Parse dates
                    activity.startTime?.let { startTime ->
                        startYear = startTime.year
                        startMonth = startTime.monthValue
                        startDay = startTime.dayOfMonth
                        startHour = startTime.hour
                        startMinute = startTime.minute
                    }
                    activity.endTime?.let { endTime ->
                        endYear = endTime.year
                        endMonth = endTime.monthValue
                        endDay = endTime.dayOfMonth
                        endHour = endTime.hour
                        endMinute = endTime.minute
                    }
                    maxParticipantsText = activity.participants?.toString() ?: ""
                    priceText = activity.price?.toString() ?: ""
                }.onFailure { e ->
                    errorMessage = e.message ?: "Impossibile caricare i dati dell'attività"
                }
            } catch (e: Exception) {
                errorMessage = e.message ?: "Errore nel caricamento"
            } finally {
                isLoading = false
            }
        }
    }

    fun deleteActivity(onSuccess: () -> Unit) {
        val currentId = activityId ?: return
        isLoading = true
        errorMessage = null
        viewModelScope.launch {
            try {
                val result = activityRepository.deleteActivity(currentId)
                result.onSuccess {
                    resetForm()
                    onSuccess()
                }.onFailure { e ->
                    errorMessage = e.message ?: "Si è verificato un errore durante l'eliminazione"
                }
            } catch (e: Exception) {
                errorMessage = e.message ?: "Errore imprevisto"
            } finally {
                isLoading = false
            }
        }
    }
}
