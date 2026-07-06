package com.travel.app.presentation.home

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.travel.app.data.AppContainer
import com.travel.app.presentation.components.activity.ActivityCard
import com.travel.app.presentation.components.itinerary.ItineraryCard
import it.unical.ea.dtos.activity.ActivityDto
import it.unical.ea.dtos.itinerary.ItineraryDto

enum class FavoritesTab { ITINERARI, ATTIVITA }

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreferitiScreen(
    onActivityClick: (String) -> Unit,
    onItineraryClick: (ItineraryDto) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(FavoritesTab.ITINERARI) }
    var activities by remember { mutableStateOf<List<ActivityDto>>(emptyList()) }
    var itineraries by remember { mutableStateOf<List<ItineraryDto>>(emptyList()) }
    
    var favoriteActivityIds by remember { 
        mutableStateOf(AppContainer.sessionManager.getFavoriteActivityIds()) 
    }
    var favoriteItineraryIds by remember { 
        mutableStateOf(AppContainer.sessionManager.getFavoriteItineraryIds()) 
    }

    var isLoading by remember { mutableStateOf(true) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val reloadFavorites = {
        favoriteActivityIds = AppContainer.sessionManager.getFavoriteActivityIds()
        favoriteItineraryIds = AppContainer.sessionManager.getFavoriteItineraryIds()
    }

    LaunchedEffect(Unit) {
        isLoading = true
        errorMsg = null
        
        // Fetch activities
        val actResult = AppContainer.activityRepository.getActivities()
        // Fetch itineraries
        val itResult = AppContainer.itineraryRepository.getItineraries()

        actResult.fold(
            onSuccess = { actList ->
                activities = actList
            },
            onFailure = {
                errorMsg = "Errore durante il caricamento delle attività preferite"
            }
        )

        itResult.fold(
            onSuccess = { itList ->
                itineraries = itList
            },
            onFailure = {
                if (errorMsg == null) {
                    errorMsg = "Errore durante il caricamento degli itinerari preferiti"
                }
            }
        )
        isLoading = false
    }

    val filteredItineraries = itineraries.filter { 
        favoriteItineraryIds.contains(it.id?.toString() ?: "") 
    }
    
    val filteredActivities = activities.filter { 
        favoriteActivityIds.contains(it.id?.toString() ?: "") 
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(top = 16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Text(
                text = "Preferiti",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            )
        }

        TabRow(
            selectedTabIndex = selectedTab.ordinal,
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 20.dp)
        ) {
            Tab(
                selected = selectedTab == FavoritesTab.ITINERARI,
                onClick = { selectedTab = FavoritesTab.ITINERARI },
                text = { Text("Itinerari") }
            )
            Tab(
                selected = selectedTab == FavoritesTab.ATTIVITA,
                onClick = { selectedTab = FavoritesTab.ATTIVITA },
                text = { Text("Attività") }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator()
            } else if (errorMsg != null) {
                Text(
                    text = errorMsg!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(24.dp)
                )
            } else {
                when (selectedTab) {
                    FavoritesTab.ITINERARI -> {
                        if (filteredItineraries.isEmpty()) {
                            EmptyFavoritesState(message = "Non hai ancora salvato alcun itinerario nei preferiti.")
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(bottom = 110.dp)
                            ) {
                                items(filteredItineraries) { itinerary ->
                                    val idStr = itinerary.id?.toString() ?: ""
                                    ItineraryCard(
                                        itinerary = itinerary,
                                        isFavorite = true,
                                        onFavoriteClick = {
                                            AppContainer.sessionManager.toggleFavoriteItinerary(idStr)
                                            reloadFavorites()
                                        },
                                        onClick = { onItineraryClick(itinerary) }
                                    )
                                }
                            }
                        }
                    }
                    FavoritesTab.ATTIVITA -> {
                        if (filteredActivities.isEmpty()) {
                            EmptyFavoritesState(message = "Non hai ancora salvato alcuna attività nei preferiti.")
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(bottom = 110.dp)
                            ) {
                                items(filteredActivities) { activity ->
                                    val idStr = activity.id?.toString() ?: ""
                                    ActivityCard(
                                        activity = activity,
                                        isFavorite = true,
                                        onFavoriteClick = {
                                            AppContainer.sessionManager.toggleFavoriteActivity(idStr)
                                            reloadFavorites()
                                        },
                                        onClick = { onActivityClick(idStr) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyFavoritesState(message: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Favorite,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outlineVariant,
            modifier = Modifier.size(72.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
