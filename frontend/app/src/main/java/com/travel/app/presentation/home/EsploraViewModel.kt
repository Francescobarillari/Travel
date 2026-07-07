package com.travel.app.presentation.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.travel.app.domain.repository.ActivityRepository
import com.travel.app.domain.repository.LocalitaRepository
import com.travel.app.domain.repository.UserRepository
import com.travel.app.domain.repository.ItineraryRepository
import com.travel.app.domain.model.User
import it.unical.ea.dtos.activity.ActivityDto
import it.unical.ea.dtos.location.LocationDto as LocalitaDto
import it.unical.ea.dtos.itinerary.ItineraryDto
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class EsploraTab {
    TUTTI,
    LOCALITA,
    UTENTI,
    ATTIVITA,
    ITINERARI
}

class EsploraViewModel(
    private val activityRepository: ActivityRepository,
    private val localitaRepository: LocalitaRepository,
    private val userRepository: UserRepository,
    private val itineraryRepository: ItineraryRepository
) : ViewModel() {

    var selectedTab by mutableStateOf(EsploraTab.TUTTI)

    var searchQuery by mutableStateOf("")
        private set
    
    fun onSearchQueryChanged(query: String) {
        searchQuery = query
        filterItinerariesLocally()
        debouncedSearch()
    }
    var minPrice by mutableStateOf<Double?>(null)
        private set

    var maxPrice by mutableStateOf<Double?>(null)
        private set

    fun onPriceRangeChanged(min: Double?, max: Double?) {
        minPrice = min
        maxPrice = max
        filterItinerariesLocally()
        performSearch()
    }
    
    var activities by mutableStateOf<List<ActivityDto>>(emptyList())
    var localitaList by mutableStateOf<List<LocalitaDto>>(emptyList())
    var userList by mutableStateOf<List<User>>(emptyList())
    var allItineraries by mutableStateOf<List<ItineraryDto>>(emptyList())
    var filteredItineraries by mutableStateOf<List<ItineraryDto>>(emptyList())

    var isLoading by mutableStateOf(false)
    var isLoadingMore by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    private var currentLocalitaPage = 0
    private var isLastLocalitaPage = false

    private var currentActivityPage = 0
    private var isLastActivityPage = false

    private val PAGE_SIZE = 10

    private var searchJob: Job? = null

    init {
        performSearch()
    }

    fun onTabSelected(tab: EsploraTab) {
        selectedTab = tab
    }

    private fun debouncedSearch() {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(500) // 500ms debounce
            performSearch()
        }
    }

    fun performSearch() {
        isLoading = true
        errorMessage = null
        currentLocalitaPage = 0
        currentActivityPage = 0
        isLastLocalitaPage = false
        isLastActivityPage = false
        
        viewModelScope.launch {
            try {
                val activityDeferred = viewModelScope.launch {
                    val result = activityRepository.searchActivities(searchQuery, minPrice, maxPrice, currentActivityPage, PAGE_SIZE)
                    result.fold(
                        onSuccess = { page -> 
                            activities = page.content ?: emptyList()
                            isLastActivityPage = (page.number ?: 0) >= (page.totalPages ?: 1) - 1
                        },
                        onFailure = { errorMessage = it.message }
                    )
                }
                
                val localitaDeferred = viewModelScope.launch {
                    val result = localitaRepository.searchLocalita(
                        query = searchQuery,
                        includeExternal = false,
                        page = currentLocalitaPage,
                        size = PAGE_SIZE
                    )
                    result.fold(
                        onSuccess = { page -> 
                            localitaList = page.content ?: emptyList()
                            isLastLocalitaPage = (page.number ?: 0) >= (page.totalPages ?: 1) - 1
                        },
                        onFailure = { errorMessage = it.message }
                    )
                }

                val usersDeferred = viewModelScope.launch {
                    val result = userRepository.getAllUsers()
                    result.fold(
                        onSuccess = { list -> 
                            userList = if (searchQuery.isBlank()) {
                                list
                            } else {
                                list.filter { 
                                    it.name?.contains(searchQuery, ignoreCase = true) == true || 
                                    it.email.contains(searchQuery, ignoreCase = true)
                                }
                            }
                        },
                        onFailure = { errorMessage = it.message }
                    )
                }

                val itineraryDeferred = viewModelScope.launch {
                    val result = itineraryRepository.getItineraries()
                    result.fold(
                        onSuccess = { list ->
                            allItineraries = list
                            filterItinerariesLocally()
                        },
                        onFailure = { errorMessage = it.message }
                    )
                }
                
                activityDeferred.join()
                localitaDeferred.join()
                usersDeferred.join()
                itineraryDeferred.join()
                
            } catch (e: Exception) {
                errorMessage = e.message ?: "Errore imprevisto durante la ricerca"
            } finally {
                isLoading = false
            }
        }
    }

    fun filterItinerariesLocally() {
        val query = searchQuery.trim()
        val minP = minPrice
        val maxP = maxPrice
        
        filteredItineraries = allItineraries.filter { itinerary ->
            val queryMatch = if (query.isEmpty()) {
                true
            } else {
                val titleMatch = itinerary.getTitle()?.contains(query, ignoreCase = true) == true
                val descMatch = itinerary.getDescription()?.contains(query, ignoreCase = true) == true
                val activityMatch = itinerary.getActivities()?.any { activity ->
                    activity.name?.contains(query, ignoreCase = true) == true ||
                    activity.location?.contains(query, ignoreCase = true) == true
                } == true
                titleMatch || descMatch || activityMatch
            }

            val totalPrice = itinerary.getActivities()?.sumOf { it.getPrice()?.toDouble() ?: 0.0 } ?: 0.0
            val minMatch = if (minP == null) true else totalPrice >= minP
            val maxMatch = if (maxP == null) true else totalPrice <= maxP

            queryMatch && minMatch && maxMatch
        }
    }

    fun loadMore() {
        if (isLoading || isLoadingMore) return
        
        if (selectedTab == EsploraTab.TUTTI || selectedTab == EsploraTab.LOCALITA) {
            if (!isLastLocalitaPage) loadMoreLocalita()
        }
        
        if (selectedTab == EsploraTab.TUTTI || selectedTab == EsploraTab.ATTIVITA) {
            if (!isLastActivityPage) loadMoreActivities()
        }
    }

    private fun loadMoreLocalita() {
        isLoadingMore = true
        currentLocalitaPage++
        viewModelScope.launch {
            val result = localitaRepository.searchLocalita(
                query = searchQuery,
                includeExternal = false,
                page = currentLocalitaPage,
                size = PAGE_SIZE
            )
            result.fold(
                onSuccess = { page -> 
                    localitaList = localitaList + (page.content ?: emptyList())
                    isLastLocalitaPage = (page.number ?: 0) >= (page.totalPages ?: 1) - 1
                },
                onFailure = { errorMessage = it.message }
            )
            isLoadingMore = false
        }
    }

    private fun loadMoreActivities() {
        isLoadingMore = true
        currentActivityPage++
        viewModelScope.launch {
            val result = activityRepository.searchActivities(searchQuery, minPrice, maxPrice, currentActivityPage, PAGE_SIZE)
            result.fold(
                onSuccess = { page -> 
                    activities = activities + (page.content ?: emptyList())
                    isLastActivityPage = (page.number ?: 0) >= (page.totalPages ?: 1) - 1
                },
                onFailure = { errorMessage = it.message }
            )
            isLoadingMore = false
        }
    }
}
