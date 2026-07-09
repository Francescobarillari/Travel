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
import it.unical.ea.dtos.activity.ActivityTemplateDto
import it.unical.ea.dtos.location.LocationDto as LocalitaDto
import it.unical.ea.dtos.itinerary.ItineraryDto
import it.unical.ea.enums.TravelTag
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class CercaTab {
    TUTTI,
    LOCALITA,
    UTENTI,
    ATTIVITA,
    ITINERARI
}

enum class CercaSortOption {
    NONE,
    PRICE_ASC,
    PRICE_DESC,
    RATING_DESC,
    DATE_ASC,
    DATE_DESC
}

class CercaViewModel(
    private val activityRepository: ActivityRepository,
    private val localitaRepository: LocalitaRepository,
    private val userRepository: UserRepository,
    private val itineraryRepository: ItineraryRepository
) : ViewModel() {

    var selectedTab by mutableStateOf(CercaTab.TUTTI)

    var searchQuery by mutableStateOf("")
        private set
    
    fun onSearchQueryChanged(query: String, saveToHistory: Boolean = true) {
        searchQuery = query
        applyFiltersAndSort()
        debouncedSearch()
        if (saveToHistory && query.isNotBlank()) {
            com.travel.app.data.AppContainer.sessionManager.saveLastSearchQuery(query)
        }
    }

    var minPrice by mutableStateOf<Double?>(null)
        private set

    var maxPrice by mutableStateOf<Double?>(null)
        private set

    var selectedTags by mutableStateOf<Set<TravelTag>>(emptySet())
        private set

    var minRating by mutableStateOf<Double?>(null)
        private set

    var sortBy by mutableStateOf<CercaSortOption>(CercaSortOption.NONE)
        private set

    fun onPriceRangeChanged(min: Double?, max: Double?) {
        minPrice = min
        maxPrice = max
        applyFiltersAndSort()
        performSearch()
    }

    fun toggleTag(tag: TravelTag) {
        selectedTags = if (selectedTags.contains(tag)) {
            selectedTags - tag
        } else {
            selectedTags + tag
        }
        applyFiltersAndSort()
    }

    fun onMinRatingChanged(rating: Double?) {
        minRating = rating
        applyFiltersAndSort()
    }

    fun onSortByChanged(option: CercaSortOption) {
        sortBy = option
        applyFiltersAndSort()
    }

    fun resetFilters() {
        minPrice = null
        maxPrice = null
        selectedTags = emptySet()
        minRating = null
        sortBy = CercaSortOption.NONE
        applyFiltersAndSort()
    }
    
    var allActivities by mutableStateOf<List<ActivityTemplateDto>>(emptyList())
        private set

    var activities by mutableStateOf<List<ActivityTemplateDto>>(emptyList())
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

    private var PAGE_SIZE = 10

    private var searchJob: Job? = null

    init {
        performSearch()
    }

    fun onTabSelected(tab: CercaTab) {
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
                    val result = activityRepository.searchActivities(searchQuery, null, currentActivityPage, PAGE_SIZE)
                    result.fold(
                        onSuccess = { page -> 
                            var results = page.content ?: emptyList()
                            // Filtra le attività le cui sessioni sono già iniziate/passate
                            results = results.mapNotNull { template ->
                                val activeSessions = template.sessions?.filter { session ->
                                    val start = session.startTime
                                    start == null || start.isAfter(java.time.LocalDateTime.now())
                                } ?: emptyList()
                                if (activeSessions.isNotEmpty()) {
                                    template.sessions = activeSessions
                                    template
                                } else {
                                    null
                                }
                            }
                            allActivities = results
                            applyFiltersAndSort()
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
                            applyFiltersAndSort()
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

    fun applyFiltersAndSort() {
        filterItinerariesLocally()
        filterActivitiesLocally()
    }

    fun filterItinerariesLocally() {
        val query = searchQuery.trim()
        val minP = minPrice
        val maxP = maxPrice
        val tagsFilter = selectedTags
        val ratingFilter = minRating
        
        var list = allItineraries.filter { itinerary ->
            val start = itinerary.getStartDateTime()
            val hasStarted = start != null && start.isBefore(java.time.LocalDateTime.now())
            if (hasStarted) {
                false
            } else {
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

                val tagsMatch = if (tagsFilter.isEmpty()) {
                    true
                } else {
                    itinerary.getActivities()?.any { activity ->
                        activity.tags?.any { tag -> tagsFilter.contains(tag) } == true
                    } == true
                }

                val avgRating = itinerary.getActivities()
                    ?.filter { (it.averageRating ?: 0.0) > 0.0 }
                    ?.map { it.averageRating ?: 0.0 }
                    ?.average() ?: 0.0
                val ratingMatch = if (ratingFilter == null) true else avgRating >= ratingFilter

                queryMatch && minMatch && maxMatch && tagsMatch && ratingMatch
            }
        }

        list = when (sortBy) {
            CercaSortOption.PRICE_ASC -> list.sortedBy { itinerary ->
                itinerary.getActivities()?.sumOf { it.getPrice()?.toDouble() ?: 0.0 } ?: 0.0
            }
            CercaSortOption.PRICE_DESC -> list.sortedByDescending { itinerary ->
                itinerary.getActivities()?.sumOf { it.getPrice()?.toDouble() ?: 0.0 } ?: 0.0
            }
            CercaSortOption.RATING_DESC -> list.sortedByDescending { itinerary ->
                itinerary.getActivities()
                    ?.filter { (it.averageRating ?: 0.0) > 0.0 }
                    ?.map { it.averageRating ?: 0.0 }
                    ?.average() ?: 0.0
            }
            CercaSortOption.DATE_ASC -> list.sortedWith(compareBy(nullsLast()) { it.getStartDateTime() })
            CercaSortOption.DATE_DESC -> list.sortedWith(compareByDescending(nullsFirst()) { it.getStartDateTime() })
            else -> list
        }

        filteredItineraries = list
    }

    fun filterActivitiesLocally() {
        val minP = minPrice
        val maxP = maxPrice
        val tagsFilter = selectedTags
        val ratingFilter = minRating

        var list = allActivities

        list = list.filter { template ->
            val price = template.sessions?.firstOrNull()?.price?.toDouble() ?: 0.0
            val matchesMin = minP == null || price >= minP
            val matchesMax = maxP == null || price <= maxP
            
            val matchesTags = if (tagsFilter.isEmpty()) {
                true
            } else {
                template.tags?.any { tag -> tagsFilter.contains(tag) } == true
            }

            val matchesRating = if (ratingFilter == null) {
                true
            } else {
                (template.averageRating ?: 0.0) >= ratingFilter
            }

            matchesMin && matchesMax && matchesTags && matchesRating
        }

        list = when (sortBy) {
            CercaSortOption.PRICE_ASC -> list.sortedBy { template ->
                template.sessions?.firstOrNull()?.price?.toDouble() ?: 0.0
            }
            CercaSortOption.PRICE_DESC -> list.sortedByDescending { template ->
                template.sessions?.firstOrNull()?.price?.toDouble() ?: 0.0
            }
            CercaSortOption.RATING_DESC -> list.sortedByDescending { template ->
                template.averageRating ?: 0.0
            }
            CercaSortOption.DATE_ASC -> list.sortedWith(compareBy(nullsLast()) { template ->
                template.sessions?.firstOrNull()?.startTime
            })
            CercaSortOption.DATE_DESC -> list.sortedWith(compareByDescending(nullsFirst()) { template ->
                template.sessions?.firstOrNull()?.startTime
            })
            else -> list
        }

        activities = list
    }

    fun loadMore() {
        if (isLoading || isLoadingMore) return
        
        if (selectedTab == CercaTab.TUTTI || selectedTab == CercaTab.LOCALITA) {
            if (!isLastLocalitaPage) loadMoreLocalita()
        }
        
        if (selectedTab == CercaTab.TUTTI || selectedTab == CercaTab.ATTIVITA) {
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
            val result = activityRepository.searchActivities(searchQuery, null, currentActivityPage, PAGE_SIZE)
            result.fold(
                onSuccess = { page -> 
                    var results = page.content ?: emptyList()
                    // Filtra le attività le cui sessioni sono già iniziate/passate
                    results = results.mapNotNull { template ->
                        val activeSessions = template.sessions?.filter { session ->
                            val start = session.startTime
                            start == null || start.isAfter(java.time.LocalDateTime.now())
                        } ?: emptyList()
                        if (activeSessions.isNotEmpty()) {
                            template.sessions = activeSessions
                            template
                        } else {
                            null
                        }
                    }
                    allActivities = allActivities + results
                    applyFiltersAndSort()
                    isLastActivityPage = (page.number ?: 0) >= (page.totalPages ?: 1) - 1
                },
                onFailure = { errorMessage = it.message }
            )
            isLoadingMore = false
        }
    }
}
