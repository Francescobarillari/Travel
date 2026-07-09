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

    // Conteggio dei template di attività approvati (getActivities filtra già template approved=true)
    var approvedActivitiesCount by mutableStateOf(0)
        private set

    // Spinner a schermo intero: solo al primo caricamento
    var isLoading by mutableStateOf(false)
        private set

    // Refresh successivi (pull-to-refresh / icona aggiorna): il contenuto resta visibile
    var isRefreshing by mutableStateOf(false)
        private set

    // ID delle card con un'azione approva/rifiuta/blocca in corso
    var actingIds by mutableStateOf<Set<String>>(emptySet())
        private set

    var errorMessage by mutableStateOf<String?>(null)

    private var hasLoadedOnce = false

    fun loadData() {
        viewModelScope.launch {
            if (hasLoadedOnce) isRefreshing = true else isLoading = true
            errorMessage = null
            try {
                fetchAll()
            } catch (e: Exception) {
                errorMessage = e.localizedMessage ?: "Errore nel caricamento dei dati"
            } finally {
                hasLoadedOnce = true
                isLoading = false
                isRefreshing = false
            }
        }
    }

    private suspend fun fetchAll() {
        pendingCompanies = apiService.getPendingCompanies()
        pendingActivities = apiService.getPendingActivities()
        approvedActivitiesCount = apiService.getActivities().size

        userRepository.getAllCompanies().fold(
            onSuccess = { allCompanies = it },
            onFailure = { errorMessage = it.message }
        )
    }

    // Riallinea contatori e liste dopo un'azione, senza mostrare alcuno spinner
    private suspend fun reloadStatsSilently() {
        try {
            fetchAll()
        } catch (_: Exception) {
            // Le liste sono già state aggiornate in modo ottimistico: un errore
            // nel refresh silenzioso non deve sovrascrivere l'esito dell'azione.
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
        hasLoadedOnce = true
    }

    private fun runCardAction(
        id: String,
        fallbackError: String,
        action: suspend () -> Unit
    ) {
        viewModelScope.launch {
            actingIds = actingIds + id
            errorMessage = null
            try {
                action()
            } catch (e: Exception) {
                errorMessage = e.localizedMessage ?: fallbackError
            } finally {
                actingIds = actingIds - id
            }
        }
    }

    fun approveCompany(id: String) = runCardAction(id, "Impossibile approvare l'agenzia") {
        apiService.approveCompany(id)
        pendingCompanies = pendingCompanies.filterNot { it.id.toString() == id }
        reloadStatsSilently()
    }

    fun rejectCompany(id: String) = runCardAction(id, "Impossibile rifiutare l'agenzia") {
        apiService.rejectCompany(id)
        pendingCompanies = pendingCompanies.filterNot { it.id.toString() == id }
        reloadStatsSilently()
    }

    fun blockCompany(id: String) = runCardAction(id, "Impossibile bloccare l'agenzia") {
        userRepository.blockCompany(id).getOrThrow()
        reloadStatsSilently()
    }

    fun unblockCompany(id: String) = runCardAction(id, "Impossibile sbloccare l'agenzia") {
        userRepository.unblockCompany(id).getOrThrow()
        reloadStatsSilently()
    }

    fun approveActivity(id: String) = runCardAction(id, "Impossibile approvare l'attività") {
        apiService.approveActivity(id)
        pendingActivities = pendingActivities.filterNot { it.id.toString() == id }
        reloadStatsSilently()
    }

    fun rejectActivity(id: String) = runCardAction(id, "Impossibile eliminare l'attività") {
        apiService.rejectActivity(id)
        pendingActivities = pendingActivities.filterNot { it.id.toString() == id }
        reloadStatsSilently()
    }
}
