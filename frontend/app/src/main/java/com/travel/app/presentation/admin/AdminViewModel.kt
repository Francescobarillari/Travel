package com.travel.app.presentation.admin

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.travel.app.data.AppContainer
import com.travel.app.domain.model.User
import com.travel.app.domain.repository.UserRepository
import com.travel.app.service.ApiService
import it.unical.ea.dtos.activity.ActivityDto
import it.unical.ea.dtos.user.UserDTO
import kotlinx.coroutines.launch

class AdminViewModel(
    private val apiService: ApiService = AppContainer.apiService,
    private val userRepository: UserRepository = AppContainer.userRepository
) : ViewModel() {

    var pendingCompanies by mutableStateOf<List<UserDTO>>(emptyList())
        private set

    var pendingActivities by mutableStateOf<List<ActivityDto>>(emptyList())
        private set

    var allCompanies by mutableStateOf<List<User>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)

    fun loadData() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                pendingCompanies = apiService.getPendingCompanies()
                pendingActivities = apiService.getPendingActivities()
                
                userRepository.getAllCompanies().fold(
                    onSuccess = { allCompanies = it },
                    onFailure = { errorMessage = it.message }
                )
            } catch (e: Exception) {
                errorMessage = e.localizedMessage ?: "Errore nel caricamento dei dati"
            } finally {
                isLoading = false
            }
        }
    }

    fun loadDataForPreview(
        companies: List<UserDTO>,
        activities: List<ActivityDto>,
        allComps: List<User>
    ) {
        pendingCompanies = companies
        pendingActivities = activities
        allCompanies = allComps
    }

    fun approveCompany(id: String) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                apiService.approveCompany(id)
                loadData()
            } catch (e: Exception) {
                errorMessage = e.localizedMessage ?: "Impossibile approvare la società"
                isLoading = false
            }
        }
    }

    fun rejectCompany(id: String) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                apiService.rejectCompany(id)
                loadData()
            } catch (e: Exception) {
                errorMessage = e.localizedMessage ?: "Impossibile rifiutare la società"
                isLoading = false
            }
        }
    }

    fun blockCompany(id: String) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            userRepository.blockCompany(id).fold(
                onSuccess = { loadData() },
                onFailure = {
                    errorMessage = it.localizedMessage ?: "Impossibile bloccare la società"
                    isLoading = false
                }
            )
        }
    }

    fun unblockCompany(id: String) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            userRepository.unblockCompany(id).fold(
                onSuccess = { loadData() },
                onFailure = {
                    errorMessage = it.localizedMessage ?: "Impossibile sbloccare la società"
                    isLoading = false
                }
            )
        }
    }

    fun approveActivity(id: String) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                apiService.approveActivity(id)
                loadData()
            } catch (e: Exception) {
                errorMessage = e.localizedMessage ?: "Impossibile approvare l'attività"
                isLoading = false
            }
        }
    }

    fun rejectActivity(id: String) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                apiService.rejectActivity(id)
                loadData()
            } catch (e: Exception) {
                errorMessage = e.localizedMessage ?: "Impossibile eliminare l'attività"
                isLoading = false
            }
        }
    }
}
