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
import it.unical.ea.dtos.activity.ActivityTemplateDto
import it.unical.ea.dtos.activity.CreateActivityRequestDto
import it.unical.ea.dtos.activity.TimeSlotDto
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

    // Recurring activity states
    val selectedDaysOfWeek = mutableStateListOf<String>()
    val timeSlots = mutableStateListOf<TimeSlotDto>()

    fun toggleDayOfWeek(day: String) {
        if (selectedDaysOfWeek.contains(day)) {
            selectedDaysOfWeek.remove(day)
        } else {
            selectedDaysOfWeek.add(day)
        }
    }

    val selectedTags = mutableStateListOf<it.unical.ea.enums.TravelTag>()

    fun toggleTag(tag: it.unical.ea.enums.TravelTag) {
        if (selectedTags.contains(tag)) {
            selectedTags.remove(tag)
        } else {
            selectedTags.add(tag)
        }
    }

    fun addTimeSlot(startH: Int, startM: Int, endH: Int, endM: Int) {
        val start = java.time.LocalTime.of(startH, startM)
        val end = java.time.LocalTime.of(endH, endM)
        timeSlots.add(TimeSlotDto(start, end))
    }

    fun removeTimeSlot(index: Int) {
        if (index in timeSlots.indices) {
            timeSlots.removeAt(index)
        }
    }

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
        selectedDaysOfWeek.clear()
        selectedTags.clear()
        timeSlots.clear()
        // default time slot
        timeSlots.add(TimeSlotDto(java.time.LocalTime.of(14, 0), java.time.LocalTime.of(16, 0)))
        errorMessage = null
    }

    fun submitActivity(context: android.content.Context) {
        val participants = maxParticipantsText.toIntOrNull()
        val priceVal = priceText.toDoubleOrNull()

        val startLocalDate = if (startYear > 0) java.time.LocalDate.of(startYear, startMonth, startDay) else null
        val endLocalDate = if (endYear > 0) java.time.LocalDate.of(endYear, endMonth, endDay) else null

        val startLdt = if (isEditMode && startYear > 0) java.time.LocalDateTime.of(startYear, startMonth, startDay, startHour, startMinute) else null
        val endLdt = if (isEditMode && endYear > 0) java.time.LocalDateTime.of(endYear, endMonth, endDay, endHour, endMinute) else null

        val validationError = ActivityFormValidator.validateActivityForm(
            title = title,
            location = location,
            isEditMode = isEditMode,
            startLdt = startLdt,
            endLdt = endLdt,
            startDate = startLocalDate,
            endDate = endLocalDate,
            selectedDays = selectedDaysOfWeek.toSet(),
            timeSlots = timeSlots.toList(),
            maxParticipants = participants,
            price = priceVal
        )

        if (validationError != null) {
            errorMessage = validationError
            return
        }

        isLoading = true
        errorMessage = null

        viewModelScope.launch {
            try {
                val result = if (isEditMode) {
                    val activityDto = ActivityDto()
                    activityDto.setName(title.trim())
                    activityDto.setDescription(if (description.isNotBlank()) description else null)
                    activityDto.setLocation(location)
                    activityDto.setStartTime(startLdt!!)
                    activityDto.setEndTime(endLdt!!)
                    activityDto.setParticipants(participants!!)
                    activityDto.setPrice(BigDecimal.valueOf(priceVal!!))
                    activityDto.setOrganizer(defaultOrganizer)
                    activityDto.setTags(selectedTags.toSet())
                    activityRepository.updateActivity(activityId!!, activityDto)
                } else {
                    val createRequest = CreateActivityRequestDto()
                    createRequest.setName(title.trim())
                    createRequest.setDescription(if (description.isNotBlank()) description else null)
                    createRequest.setLocation(location)
                    createRequest.setStartDate(startLocalDate!!)
                    createRequest.setEndDate(endLocalDate!!)
                    createRequest.setDaysOfWeek(selectedDaysOfWeek.toSet())
                    createRequest.setTimeSlots(timeSlots.toList())
                    createRequest.setParticipants(participants!!)
                    createRequest.setPrice(BigDecimal.valueOf(priceVal!!))
                    createRequest.setOrganizer(defaultOrganizer)
                    createRequest.setTags(selectedTags.toSet())
                    activityRepository.createActivity(createRequest)
                }
                
                result.onSuccess { createdOrUpdated ->
                    if (selectedImages.isNotEmpty()) {
                        val imageParts = selectedImages.mapNotNull { uri ->
                            try {
                                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                                    val bytes = inputStream.readBytes()
                                    val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
                                    val fileName = "image_${System.currentTimeMillis()}.jpg"
                                    Triple(bytes, mimeType, fileName)
                                }
                            } catch (e: Exception) {
                                null
                            }
                        }
                        
                        if (imageParts.isNotEmpty()) {
                            val uploadId = when (createdOrUpdated) {
                                is ActivityTemplateDto -> createdOrUpdated.id
                                is ActivityDto -> createdOrUpdated.id
                                else -> null
                            }
                            val uploadResult = activityRepository.uploadActivityImages(uploadId.toString(), imageParts)
                            uploadResult.onFailure {
                                errorMessage = "L'attività è stata salvata, ma c'è stato un errore nel caricare le immagini."
                            }
                        }
                    }
                    
                    if (errorMessage == null) {
                        showSuccessDialog = true
                        resetForm()
                    }
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
                    activity.tags?.let { tags ->
                        selectedTags.clear()
                        selectedTags.addAll(tags)
                    }
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
