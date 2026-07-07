package com.travel.app.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.travel.app.presentation.theme.TravelTheme
import it.unical.ea.dtos.activity.ActivityDto
import it.unical.ea.dtos.location.LocationDto as LocalitaDto
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.ui.tooling.preview.Preview
import com.travel.app.presentation.components.activity.ActivityCard
import com.travel.app.presentation.components.localita.LocalitaCard
import com.travel.app.presentation.components.itinerary.ItineraryCard
import androidx.compose.foundation.lazy.items
import it.unical.ea.enums.TravelTag

import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun EsploraScreen(
    viewModel: EsploraViewModel,
    onItemClick: (String, Boolean) -> Unit = { _, _ -> },
    favoritesTrigger: Int = 0,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val focusManager = LocalFocusManager.current
    var isSearchFocused by remember { mutableStateOf(false) }

    val favoriteActivities = remember { mutableStateMapOf<String, Boolean>() }
    val favoriteItineraries = remember { mutableStateMapOf<String, Boolean>() }

    LaunchedEffect(viewModel.activities, viewModel.filteredItineraries, favoritesTrigger) {
        val favActIds = com.travel.app.data.AppContainer.sessionManager.getFavoriteActivityIds()
        viewModel.activities.forEach { act ->
            val idStr = act.id?.toString() ?: ""
            favoriteActivities[idStr] = favActIds.contains(idStr)
        }
        
        val favItIds = com.travel.app.data.AppContainer.sessionManager.getFavoriteItineraryIds()
        viewModel.filteredItineraries.forEach { itin ->
            val idStr = itin.id?.toString() ?: ""
            favoriteItineraries[idStr] = favItIds.contains(idStr)
        }
    }

    var sliderPosition by remember(viewModel.minPrice, viewModel.maxPrice) {
        val min = viewModel.minPrice?.toFloat() ?: 0f
        val max = viewModel.maxPrice?.toFloat() ?: 500f
        mutableStateOf(min..max)
    }

    LaunchedEffect(Unit) {
        viewModel.performSearch()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = { focusManager.clearFocus() })
            }
            .padding(top = 16.dp)
    ) {

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Text(
                text = "Esplora",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            )
        }

        var isFilterPanelVisible by remember { mutableStateOf(false) }

        OutlinedTextField(
            value = viewModel.searchQuery,
            onValueChange = { viewModel.onSearchQueryChanged(it) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .onFocusChanged { isSearchFocused = it.isFocused },
            placeholder = { 
                Text(
                    text = "Cerca per località, nome...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                ) 
            },
            leadingIcon = { 
                Icon(
                    imageVector = Icons.Default.Search, 
                    contentDescription = "Cerca", 
                    tint = MaterialTheme.colorScheme.primary 
                ) 
            },
            trailingIcon = {
                if (isSearchFocused || viewModel.searchQuery.isNotEmpty()) {
                    IconButton(onClick = { isFilterPanelVisible = !isFilterPanelVisible }) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filtri",
                            tint = if (isFilterPanelVisible) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(32.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                focusedLabelColor = MaterialTheme.colorScheme.primary
            )
        )

        androidx.compose.animation.AnimatedVisibility(visible = isFilterPanelVisible && (isSearchFocused || viewModel.searchQuery.isNotEmpty())) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    val maxDisplay = if (sliderPosition.endInclusive >= 500f) "500+€" else "${sliderPosition.endInclusive.toInt()}€"
                    Text(
                        text = "Fascia di prezzo (solo per attività): ${sliderPosition.start.toInt()}€ - $maxDisplay",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    androidx.compose.material3.RangeSlider(
                        value = sliderPosition,
                        onValueChange = { sliderPosition = it },
                        valueRange = 0f..500f,
                        steps = 49,
                        onValueChangeFinished = {
                            val maxP = if (sliderPosition.endInclusive >= 500f) null else sliderPosition.endInclusive.toDouble()
                            viewModel.onPriceRangeChanged(sliderPosition.start.toDouble(), maxP)
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        androidx.compose.animation.AnimatedVisibility(visible = isSearchFocused || viewModel.searchQuery.isNotEmpty()) {
            TabRow(
                selectedTabIndex = viewModel.selectedTab.ordinal,
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 20.dp)
            ) {
                Tab(
                    selected = viewModel.selectedTab == EsploraTab.TUTTI,
                    onClick = { viewModel.onTabSelected(EsploraTab.TUTTI) },
                    text = { Text("Tutti") }
                )
                Tab(
                    selected = viewModel.selectedTab == EsploraTab.LOCALITA,
                    onClick = { viewModel.onTabSelected(EsploraTab.LOCALITA) },
                    text = { Text("Località") }
                )
                Tab(
                    selected = viewModel.selectedTab == EsploraTab.ATTIVITA,
                    onClick = { viewModel.onTabSelected(EsploraTab.ATTIVITA) },
                    text = { Text("Attività") }
                )
                Tab(
                    selected = viewModel.selectedTab == EsploraTab.ITINERARI,
                    onClick = { viewModel.onTabSelected(EsploraTab.ITINERARI) },
                    text = { Text("Itinerari") }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        val listState = androidx.compose.foundation.lazy.rememberLazyListState()

        LaunchedEffect(listState) {
            snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
                .collect { lastIndex ->
                    val totalItems = listState.layoutInfo.totalItemsCount
                    if (totalItems > 0 && lastIndex != null && lastIndex >= totalItems - 2) {
                        viewModel.loadMore()
                    }
                }
        }

        androidx.compose.foundation.lazy.LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            state = listState,
            contentPadding = PaddingValues(bottom = 110.dp)
        ) {
            
            item {
                androidx.compose.animation.AnimatedVisibility(visible = !isSearchFocused && viewModel.searchQuery.isEmpty()) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Ispirazioni di Viaggio",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                Text("Scopri le nostre migliori destinazioni", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSecondaryContainer)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Text(
                            text = "Attività in Evidenza",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                Text("Esperienze uniche da non perdere", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onTertiaryContainer)
                            }
                        }
                    }
                }
            }
            
            if (isSearchFocused || viewModel.searchQuery.isNotEmpty()) {
                
                if (viewModel.errorMessage != null) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Error,
                                    contentDescription = "Errore",
                                    tint = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = viewModel.errorMessage ?: "",
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                } else if (!viewModel.isLoading && viewModel.activities.isEmpty() && viewModel.localitaList.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 80.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (viewModel.searchQuery.isBlank()) "Inizia la tua ricerca." else "Nessun risultato trovato.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                } else {
                    if (viewModel.selectedTab == EsploraTab.TUTTI || viewModel.selectedTab == EsploraTab.LOCALITA) {
                        if (viewModel.localitaList.isNotEmpty()) {
                            item {
                                Text(
                                    text = "Località",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                                )
                            }
                            items(viewModel.localitaList.size) { index ->
                                val localita = viewModel.localitaList[index]
                                LocalitaCard(
                                    localita = localita,
                                    onClick = {
                                        viewModel.onSearchQueryChanged(localita.name ?: "")
                                        viewModel.performSearch()
                                    }
                                )
                            }
                        }
                    }
                    
                    if (viewModel.selectedTab == EsploraTab.TUTTI || viewModel.selectedTab == EsploraTab.ATTIVITA) {
                        if (viewModel.activities.isNotEmpty()) {
                            item {
                                Text(
                                    text = "Attività Turistiche",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                                )
                            }
                            items(viewModel.activities.size) { index ->
                                val activity = viewModel.activities[index]
                                val actId = activity.id?.toString() ?: ""
                                val isFav = favoriteActivities[actId] == true
                                ActivityCard(
                                    activity = activity,
                                    isFavorite = isFav,
                                    onFavoriteClick = {
                                        com.travel.app.data.AppContainer.sessionManager.toggleFavoriteActivity(actId)
                                        favoriteActivities[actId] = !isFav
                                    },
                                    onClick = { onItemClick(actId, false) }
                                )
                            }
                        }
                    }

                    if (viewModel.selectedTab == EsploraTab.TUTTI || viewModel.selectedTab == EsploraTab.ITINERARI) {
                        if (viewModel.filteredItineraries.isNotEmpty()) {
                            item {
                                Text(
                                    text = "Itinerari di Viaggio",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                                )
                            }
                            items(viewModel.filteredItineraries.size) { index ->
                                val itinerary = viewModel.filteredItineraries[index]
                                val itinId = itinerary.id?.toString() ?: ""
                                val isFav = favoriteItineraries[itinId] == true
                                ItineraryCard(
                                    itinerary = itinerary,
                                    isFavorite = isFav,
                                    onFavoriteClick = {
                                        com.travel.app.data.AppContainer.sessionManager.toggleFavoriteItinerary(itinId)
                                        favoriteItineraries[itinId] = !isFav
                                    },
                                    onClick = { onItemClick(itinId, true) }
                                )
                            }
                        }
                    }

                    if (viewModel.isLoading || viewModel.isLoadingMore) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                }
            }
        }
    } 
} 

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun EsploraScreenPreview() {
    TravelTheme {
        val mockViewModel = remember {
            EsploraViewModel(
                activityRepository = object : com.travel.app.domain.repository.ActivityRepository {
                    override suspend fun createActivity(activity: ActivityDto) = Result.success(activity)
                    override suspend fun getActivities() = Result.success(emptyList<ActivityDto>())
                    override suspend fun getActivityById(id: String) = Result.failure<ActivityDto>(Exception("Not implemented"))
                    override suspend fun searchActivities(query: String, minPrice: Double?, maxPrice: Double?, page: Int, size: Int) = Result.success(it.unical.ea.dtos.common.PageDto<ActivityDto>(emptyList<ActivityDto>(), 0L, 0, 10, 0))
                },
                localitaRepository = object : com.travel.app.domain.repository.LocalitaRepository {
                    override suspend fun getLocalitaById(id: String) = Result.failure<LocalitaDto>(Exception("Not implemented"))
                    override suspend fun searchLocalita(query: String, page: Int, size: Int) = Result.success(it.unical.ea.dtos.common.PageDto<LocalitaDto>(emptyList<LocalitaDto>(), 0L, 0, 10, 0))
                },
                itineraryRepository = object : com.travel.app.domain.repository.ItineraryRepository {
                    override suspend fun getItineraries() = Result.success(emptyList<it.unical.ea.dtos.itinerary.ItineraryDto>())
                    override suspend fun getItinerariesByCreator(creatorId: String) = Result.success(emptyList<it.unical.ea.dtos.itinerary.ItineraryDto>())
                    override suspend fun createItinerary(request: it.unical.ea.dtos.itinerary.CreateItineraryRequest) = Result.failure<it.unical.ea.dtos.itinerary.ItineraryDto>(Exception("Not implemented"))
                    override suspend fun updateItineraryVisibility(id: String, visibility: String) = Result.failure<it.unical.ea.dtos.itinerary.ItineraryDto>(Exception("Not implemented"))
                    override suspend fun deleteItinerary(id: String) = Result.success(Unit)
                }
            )
        }
        EsploraScreen(viewModel = mockViewModel)
    }
}
