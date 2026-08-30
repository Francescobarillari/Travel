package com.travel.app.presentation.home

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.travel.app.data.AppContainer
import com.travel.app.domain.model.User
import com.travel.app.presentation.components.activity.ActivityCard
import com.travel.app.presentation.components.itinerary.ItineraryCard
import it.unical.ea.dtos.activity.ActivityDto
import it.unical.ea.dtos.itinerary.ItineraryDto
import kotlinx.coroutines.launch

import java.time.LocalDateTime

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyBookingsScreen(
    user: User?,
    onBack: () -> Unit,
    onActivityClick: (String) -> Unit,
    onItineraryClick: (ItineraryDto) -> Unit,
    refreshTrigger: Int = 0,
    modifier: Modifier = Modifier
) {
    var bookedActivities by remember { mutableStateOf<List<ActivityDto>>(emptyList()) }
    var bookedItineraries by remember { mutableStateOf<List<ItineraryDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var filterStatus by remember { mutableStateOf("ALL") } // "ALL", "UPCOMING", "PAST"
    val tabs = listOf("Attività", "Itinerari")
    val scope = rememberCoroutineScope()

    val fetchBookings = {
        scope.launch {
            isLoading = true
            errorMsg = null
            try {
                val actResult = AppContainer.activityRepository.getBookedActivities()
                actResult.fold(
                    onSuccess = { list -> bookedActivities = list },
                    onFailure = { err -> errorMsg = err.message ?: "Errore caricamento attività" }
                )
                
                val itinResult = AppContainer.itineraryRepository.getBookedItineraries()
                itinResult.fold(
                    onSuccess = { list -> bookedItineraries = list },
                    onFailure = { err -> errorMsg = errorMsg ?: (err.message ?: "Errore caricamento itinerari") }
                )
            } catch (e: Exception) {
                errorMsg = e.message ?: "Errore imprevisto"
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(user?.id, refreshTrigger) {
        fetchBookings()
    }

    val now = remember { LocalDateTime.now() }
    val isPastActivity = { act: ActivityDto ->
        val end = act.endTime ?: act.startTime
        end != null && end.isBefore(now)
    }
    val isPastItinerary = { itin: ItineraryDto ->
        val end = itin.endDateTime ?: itin.startDateTime
        end != null && end.isBefore(now)
    }

    val filteredActivities = remember(bookedActivities, filterStatus) {
        bookedActivities.filter {
            when (filterStatus) {
                "UPCOMING" -> !isPastActivity(it)
                "PAST" -> isPastActivity(it)
                else -> true
            }
        }.sortedWith(compareBy<ActivityDto> { isPastActivity(it) }.thenBy { it.startTime })
    }

    val filteredItineraries = remember(bookedItineraries, filterStatus) {
        bookedItineraries.filter {
            when (filterStatus) {
                "UPCOMING" -> !isPastItinerary(it)
                "PAST" -> isPastItinerary(it)
                else -> true
            }
        }.sortedWith(compareBy<ItineraryDto> { isPastItinerary(it) }.thenBy { it.startDateTime })
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp)
                .statusBarsPadding()
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Indietro",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "I miei prenotati",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            )
        }

        // Tabs
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(title, fontWeight = FontWeight.SemiBold) }
                )
            }
        }

        // Sub Filter Chips (Tutti, In Programma, Passati)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = filterStatus == "ALL",
                onClick = { filterStatus = "ALL" },
                label = { Text("Tutti") }
            )
            FilterChip(
                selected = filterStatus == "UPCOMING",
                onClick = { filterStatus = "UPCOMING" },
                label = { Text("In Programma") }
            )
            FilterChip(
                selected = filterStatus == "PAST",
                onClick = { filterStatus = "PAST" },
                label = { Text("Conclusi / Passati") }
            )
        }

        // Content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (errorMsg != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = errorMsg ?: "Si è verificato un errore",
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { fetchBookings() }) {
                        Text("Riprova")
                    }
                }
            } else {
                if (selectedTabIndex == 0) {
                    // Activities Tab
                    if (filteredActivities.isEmpty()) {
                        EmptyBookingsView(
                            text = if (filterStatus == "PAST") "Nessuna attività passata" 
                                   else if (filterStatus == "UPCOMING") "Nessuna attività in programma"
                                   else "Nessuna attività prenotata"
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 80.dp)
                        ) {
                            items(filteredActivities) { activity ->
                                ActivityCard(
                                    activity = activity,
                                    onClick = {
                                        activity.id?.let { onActivityClick(it.toString()) }
                                    }
                                )
                            }
                        }
                    }
                } else {
                    // Itineraries Tab
                    if (filteredItineraries.isEmpty()) {
                        EmptyBookingsView(
                            text = if (filterStatus == "PAST") "Nessun itinerario passato"
                                   else if (filterStatus == "UPCOMING") "Nessun itinerario in programma"
                                   else "Nessun itinerario prenotato"
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 80.dp)
                        ) {
                            items(filteredItineraries) { itinerary ->
                                ItineraryCard(
                                    itinerary = itinerary,
                                    onClick = {
                                        onItineraryClick(itinerary)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyBookingsView(text: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.BookmarkBorder,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
