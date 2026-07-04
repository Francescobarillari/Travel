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
import androidx.compose.ui.tooling.preview.Preview

import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures

@Composable
fun EsploraScreen(
    viewModel: EsploraViewModel,
    onItemClick: (String, Boolean) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val focusManager = LocalFocusManager.current
    var isSearchFocused by remember { mutableStateOf(false) }

    // Hoisted state for RangeSlider
    var sliderPosition by remember(viewModel.minPrice, viewModel.maxPrice) {
        val min = viewModel.minPrice?.toFloat() ?: 0f
        val max = viewModel.maxPrice?.toFloat() ?: 500f
        mutableStateOf(min..max)
    }

    // Fetch activities from backend
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

        // Modern, premium SearchBar
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

        // Pannello filtri
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
                        text = "Fascia di prezzo: ${sliderPosition.start.toInt()}€ - $maxDisplay",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    androidx.compose.material3.RangeSlider(
                        value = sliderPosition,
                        onValueChange = { sliderPosition = it },
                        valueRange = 0f..500f,
                        steps = 49, // Intervalli di 10€ per maggiore controllo e fluidità
                        onValueChangeFinished = {
                            val maxP = if (sliderPosition.endInclusive >= 500f) null else sliderPosition.endInclusive.toDouble()
                            viewModel.onPriceRangeChanged(sliderPosition.start.toDouble(), maxP)
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // MOSTRA I TAB SOLO SE LA SEARCH BAR È ATTIVA
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
                    selected = viewModel.selectedTab == EsploraTab.VIAGGI,
                    onClick = { viewModel.onTabSelected(EsploraTab.VIAGGI) },
                    text = { Text("Viaggi") }
                )
                Tab(
                    selected = viewModel.selectedTab == EsploraTab.ATTIVITA,
                    onClick = { viewModel.onTabSelected(EsploraTab.ATTIVITA) },
                    text = { Text("Attività") }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        val listState = androidx.compose.foundation.lazy.rememberLazyListState()

        // Implementazione dello scroll infinito (Lazy Loading)
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
            
            // MOSTRA LA VETRINA SE NON IN RICERCA
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
            
            // MOSTRA I RISULTATI SE IN RICERCA
            if (isSearchFocused || viewModel.searchQuery.isNotEmpty()) {
                
                // Stato di errore o vuoto
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
                } else if (!viewModel.isLoading && viewModel.activities.isEmpty() && viewModel.trips.isEmpty()) {
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
                    // Render results based on tab
                    if (viewModel.selectedTab == EsploraTab.TUTTI || viewModel.selectedTab == EsploraTab.VIAGGI) {
                        if (viewModel.trips.isNotEmpty()) {
                            item {
                                Text(
                                    text = "Viaggi e Località",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                                )
                            }
                            items(viewModel.trips.size) { index ->
                                val trip = viewModel.trips[index]
                                TripCard(trip = trip, onClick = { onItemClick(trip.id.toString(), true) })
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
                                ActivityCard(activity = activity, onClick = { onItemClick(activity.id.toString(), false) })
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
} // Chiude main Column
} // Chiude EsploraScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityCard(activity: ActivityDto, onClick: () -> Unit = {}) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = activity.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Organizzato da: ${activity.organizer ?: "N/D"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                
                // Price Badge
                val priceDouble = activity.price?.toDouble() ?: 0.0
                val priceText = if (priceDouble <= 0.0) "Gratuito" else "€${String.format("%.2f", priceDouble)}"
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (priceDouble <= 0.0) Color(0xFFDCFCE7) else MaterialTheme.colorScheme.primaryContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = priceText,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (priceDouble <= 0.0) Color(0xFF15803D) else MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            if (!activity.description.isNullOrBlank()) {
                Text(
                    text = activity.description ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    maxLines = 3
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Location
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = activity.location,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }

                // Date
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = formatDateTime(activity.startTime),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripCard(trip: it.unical.ea.dtos.trip.TripDto, onClick: () -> Unit = {}) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = trip.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = "Organizzato da: ${trip.organizer ?: "N/D"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f)
                    )
                }
                
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "VIAGGIO",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            if (!trip.description.isNullOrBlank()) {
                Text(
                    text = trip.description ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f),
                    maxLines = 3
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Location
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = trip.location,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.9f)
                    )
                }
            }
        }
    }
}

fun formatDateTime(dateTime: java.time.LocalDateTime?): String {
    if (dateTime == null) return ""
    return try {
        val formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
        dateTime.format(formatter)
    } catch (e: Exception) {
        dateTime.toString()
    }
}

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
                tripRepository = object : com.travel.app.domain.repository.TripRepository {
                    override suspend fun getTripById(id: String) = Result.failure<it.unical.ea.dtos.trip.TripDto>(Exception("Not implemented"))
                    override suspend fun searchTrips(query: String, minPrice: Double?, maxPrice: Double?, page: Int, size: Int) = Result.success(it.unical.ea.dtos.common.PageDto<it.unical.ea.dtos.trip.TripDto>(emptyList<it.unical.ea.dtos.trip.TripDto>(), 0L, 0, 10, 0))
                }
            )
        }
        EsploraScreen(viewModel = mockViewModel)
    }
}
