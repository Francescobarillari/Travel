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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.travel.app.presentation.theme.TravelTheme
import it.unical.ea.dtos.activity.ActivityDto
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.ui.tooling.preview.Preview
import com.travel.app.data.AppContainer
import com.travel.app.presentation.components.activity.ActivityCard
import com.travel.app.presentation.components.itinerary.ItineraryCard
import androidx.compose.foundation.lazy.items
import it.unical.ea.enums.TravelTag
import androidx.compose.ui.text.style.TextOverflow
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip

import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import com.travel.app.domain.model.User

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CercaScreen(
    viewModel: CercaViewModel,
    onItemClick: (String, Boolean) -> Unit = { _, _ -> },
    onUserClick: (User) -> Unit = {},
    favoritesTrigger: Int = 0,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    var isSearchActive by remember(viewModel.searchQuery) { mutableStateOf(viewModel.searchQuery.isNotEmpty()) }

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
        viewModel.selectedTab = CercaTab.TUTTI
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
                text = "Cerca",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            )
        }

        var isFilterPanelVisible by remember { mutableStateOf(false) }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            OutlinedTextField(
                value = viewModel.searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                modifier = Modifier
                    .weight(1f)
                    .onFocusChanged { 
                        if (it.isFocused) isSearchActive = true 
                    },
                placeholder = { 
                    Text(
                        text = "Cerca per utenti, itinerari, attività...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    ) 
                },
                leadingIcon = { 
                    if (isSearchActive) {
                        IconButton(onClick = {
                            focusManager.clearFocus()
                            viewModel.onSearchQueryChanged("")
                            isSearchActive = false
                        }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack, 
                                contentDescription = "Indietro",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    } else {
                        Icon(
                            imageVector = Icons.Default.Search, 
                            contentDescription = "Cerca", 
                            tint = MaterialTheme.colorScheme.primary 
                        )
                    }
                },
                trailingIcon = {
                    if (viewModel.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Cancella",
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
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

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = { isFilterPanelVisible = !isFilterPanelVisible },
                modifier = Modifier
                    .background(
                        color = if (isFilterPanelVisible) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent,
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = "Filtri",
                    tint = if (isFilterPanelVisible) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }

        androidx.compose.animation.AnimatedVisibility(visible = isFilterPanelVisible && (isSearchActive || viewModel.searchQuery.isNotEmpty())) {
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

        androidx.compose.animation.AnimatedVisibility(visible = isSearchActive || viewModel.searchQuery.isNotEmpty()) {
            val tabIndex = when (viewModel.selectedTab) {
                CercaTab.TUTTI -> 0
                CercaTab.UTENTI -> 1
                CercaTab.ATTIVITA -> 2
                CercaTab.ITINERARI -> 3
                else -> 0
            }
            TabRow(
                selectedTabIndex = tabIndex,
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 20.dp)
            ) {
                Tab(
                    selected = viewModel.selectedTab == CercaTab.TUTTI,
                    onClick = { viewModel.onTabSelected(CercaTab.TUTTI) },
                    text = { Text("Tutti") }
                )
                Tab(
                    selected = viewModel.selectedTab == CercaTab.UTENTI,
                    onClick = { viewModel.onTabSelected(CercaTab.UTENTI) },
                    text = { Text("Utenti") }
                )
                Tab(
                    selected = viewModel.selectedTab == CercaTab.ATTIVITA,
                    onClick = { viewModel.onTabSelected(CercaTab.ATTIVITA) },
                    text = { Text("Attività") }
                )
                Tab(
                    selected = viewModel.selectedTab == CercaTab.ITINERARI,
                    onClick = { viewModel.onTabSelected(CercaTab.ITINERARI) },
                    text = { Text("Itinerari") }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        val listState = androidx.compose.foundation.lazy.rememberLazyListState()

        LaunchedEffect(listState.isScrollInProgress) {
            if (listState.isScrollInProgress) {
                focusManager.clearFocus()
            }
        }

        LaunchedEffect(listState) {
            snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
                .collect { lastIndex ->
                    val totalItems = listState.layoutInfo.totalItemsCount
                    if (totalItems > 0 && lastIndex != null && lastIndex >= totalItems - 2) {
                        viewModel.loadMore()
                    }
                }
        }

        LaunchedEffect(viewModel.selectedTab) {
            listState.scrollToItem(0)
        }

        androidx.compose.foundation.lazy.LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            state = listState,
            contentPadding = PaddingValues(bottom = 110.dp)
        ) {
            
            // The placeholders are removed. Only search is allowed.
            if (viewModel.searchQuery.isEmpty() && !isSearchActive) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Inizia la tua ricerca.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            } else {
                
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
                } else if (!viewModel.isLoading && viewModel.activities.isEmpty() && viewModel.userList.isEmpty() && viewModel.filteredItineraries.isEmpty()) {
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
                    if (viewModel.selectedTab == CercaTab.TUTTI || viewModel.selectedTab == CercaTab.ATTIVITA) {
                        if (viewModel.activities.isNotEmpty()) {
                            item {
                                Text(
                                    text = "Attività Turistiche",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                                )
                            }
                            items(
                                count = viewModel.activities.size,
                                key = { index -> "activity_${viewModel.activities[index].id ?: index}" },
                                contentType = { "Activity" }
                            ) { index ->
                                val template = viewModel.activities[index]
                                val activity = ActivityDto().apply {
                                    id = template.sessions?.firstOrNull()?.id ?: template.id
                                    name = template.name
                                    description = template.description
                                    location = template.location
                                    price = template.sessions?.firstOrNull()?.price
                                    startTime = template.sessions?.firstOrNull()?.startTime
                                    endTime = template.sessions?.firstOrNull()?.endTime
                                    tags = template.tags
                                    images = template.images?.takeIf { it.isNotEmpty() }
                                        ?: template.sessions?.firstOrNull()?.images
                                }
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

                    if (viewModel.selectedTab == CercaTab.TUTTI || viewModel.selectedTab == CercaTab.ITINERARI) {
                        if (viewModel.filteredItineraries.isNotEmpty()) {
                            item {
                                Text(
                                    text = "Itinerari di Viaggio",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                                )
                            }
                            items(
                                count = viewModel.filteredItineraries.size,
                                key = { index -> "itinerary_${viewModel.filteredItineraries[index].id ?: index}" },
                                contentType = { "Itinerary" }
                            ) { index ->
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

                    if (viewModel.selectedTab == CercaTab.TUTTI || viewModel.selectedTab == CercaTab.UTENTI) {
                        if (viewModel.userList.isNotEmpty()) {
                            item {
                                Text(
                                    text = "Utenti",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                                )
                            }
                            items(
                                count = viewModel.userList.size,
                                key = { index -> "user_${viewModel.userList[index].id ?: index}" },
                                contentType = { "User" }
                            ) { index ->
                                val user = viewModel.userList[index]
                                UserCard(
                                    user = user,
                                    onClick = { onUserClick(user) }
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

@Composable
fun UserCard(
    user: User,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (!user.avatarUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = user.avatarUrl,
                        contentDescription = "Avatar",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = user.name?.firstOrNull()?.uppercase() ?: "?",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.name ?: "Utente",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (user.userType == "SOCIETA") "Società/Agenzia" else "Viaggiatore",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun EsploraScreenPreview() {
    TravelTheme {
        val fakeViewModel = CercaViewModel(
            activityRepository = AppContainer.activityRepository,
            localitaRepository = AppContainer.localitaRepository,
            userRepository = AppContainer.userRepository,
            itineraryRepository = AppContainer.itineraryRepository
        )
        CercaScreen(
            viewModel = fakeViewModel
        )
    }
}
