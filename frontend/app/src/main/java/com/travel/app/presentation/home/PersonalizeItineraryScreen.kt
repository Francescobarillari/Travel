package com.travel.app.presentation.home

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.travel.app.data.AppContainer
import it.unical.ea.dtos.activity.ActivityDto
import it.unical.ea.dtos.itinerary.CreateItineraryRequest
import it.unical.ea.dtos.itinerary.ItineraryDto
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalizeItineraryScreen(
    itinerary: ItineraryDto,
    onNavigateBack: () -> Unit,
    onPersonalizeSuccess: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Extract city name from original itinerary's activities
    val city = remember(itinerary) {
        itinerary.activities?.firstOrNull()?.location?.split(",")?.firstOrNull()?.trim() ?: ""
    }

    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var availableActivities by remember { mutableStateOf<List<ActivityDto>>(emptyList()) }
    
    // Set of selected activity IDs, pre-populated with original activities
    var selectedActivityIds by remember(itinerary) {
        mutableStateOf(itinerary.activities?.mapNotNull { it.id?.toString() }?.toSet() ?: emptySet())
    }

    LaunchedEffect(city) {
        if (city.isNotBlank()) {
            isLoading = true
            val res = AppContainer.activityRepository.searchActivities(query = city, size = 100)
            isLoading = false
            res.fold(
                onSuccess = { page ->
                    availableActivities = page.content ?: emptyList()
                },
                onFailure = { err ->
                    error = err.message
                }
            )
        } else {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Personalizza Itinerario", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        if (city.isNotBlank()) {
                            Text("Attività a $city", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Indietro")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            val selectedActs = availableActivities.filter { selectedActivityIds.contains(it.id?.toString()) }
            val totalPrice = selectedActs.sumOf { it.price?.toDouble() ?: 0.0 }
            
            Surface(
                shadowElevation = 12.dp,
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .navigationBarsPadding()
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "${selectedActivityIds.size} attività selezionate",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "€${String.format("%.2f", totalPrice)}",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Button(
                        onClick = {
                            val userId = AppContainer.sessionManager.getSessionUser()?.id
                            if (userId == null) {
                                Toast.makeText(context, "Errore: utente non loggato", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (selectedActivityIds.isEmpty()) {
                                Toast.makeText(context, "Seleziona almeno un'attività!", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            isSaving = true
                            scope.launch {
                                val req = CreateItineraryRequest().apply {
                                    title = itinerary.title
                                    description = itinerary.description
                                    startDateTime = itinerary.startDateTime
                                    endDateTime = itinerary.endDateTime
                                    creatorId = userId
                                    this.activityIds = selectedActivityIds.toList()
                                    visibility = "PRIVATE"
                                }
                                val res = AppContainer.itineraryRepository.createItinerary(req)
                                isSaving = false
                                res.fold(
                                    onSuccess = {
                                        Toast.makeText(context, "Itinerario personalizzato creato!", Toast.LENGTH_SHORT).show()
                                        onPersonalizeSuccess()
                                    },
                                    onFailure = { err ->
                                        Toast.makeText(context, "Errore: ${err.message}", Toast.LENGTH_LONG).show()
                                    }
                                )
                            }
                        },
                        enabled = !isSaving && selectedActivityIds.isNotEmpty(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
                        } else {
                            Text("Salva e Crea")
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (error != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                        .align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "Errore durante il caricamento: $error", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = {
                        isLoading = true
                        error = null
                        scope.launch {
                            val res = AppContainer.activityRepository.searchActivities(query = city, size = 100)
                            isLoading = false
                            res.fold(
                                onSuccess = { page -> availableActivities = page.content ?: emptyList() },
                                onFailure = { err -> error = err.message }
                            )
                        }
                    }) {
                        Text("Riprova")
                    }
                }
            } else if (availableActivities.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Nessuna attività disponibile in questa città", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(availableActivities) { activity ->
                        val actId = activity.id?.toString() ?: ""
                        val isSelected = selectedActivityIds.contains(actId)
                        
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = activity.name ?: "Attività senza nome",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = activity.location ?: "N/D",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "€${String.format("%.2f", activity.price?.toDouble() ?: 0.0)}",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                if (isSelected) {
                                    IconButton(
                                        onClick = { selectedActivityIds = selectedActivityIds - actId },
                                        colors = IconButtonDefaults.iconButtonColors(
                                            containerColor = MaterialTheme.colorScheme.errorContainer,
                                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                    ) {
                                        Icon(imageVector = Icons.Default.Remove, contentDescription = "Rimuovi")
                                    }
                                } else {
                                    IconButton(
                                        onClick = { selectedActivityIds = selectedActivityIds + actId },
                                        colors = IconButtonDefaults.iconButtonColors(
                                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    ) {
                                        Icon(imageVector = Icons.Default.Add, contentDescription = "Aggiungi")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
