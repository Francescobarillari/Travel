package com.travel.app.presentation.home

import android.widget.Toast
import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Reorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.travel.app.data.AppContainer
import it.unical.ea.dtos.activity.ActivityDto
import it.unical.ea.dtos.activity.ActivityTemplateDto
import it.unical.ea.dtos.itinerary.CreateItineraryRequest
import it.unical.ea.dtos.itinerary.ItineraryDto
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PersonalizeItineraryScreen(
    itinerary: ItineraryDto,
    initialCity: String? = null,
    coverImageUri: Uri? = null,
    onNavigateBack: () -> Unit,
    onPersonalizeSuccess: () -> Unit,
    onActivityClick: (String) -> Unit
) {
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current
    val isOnline by AppContainer.networkMonitor.isOnline.collectAsState(initial = true)
    val scope = rememberCoroutineScope()

    val city = remember(itinerary) {
        initialCity ?: itinerary.activities?.firstOrNull()?.location?.split(",")?.firstOrNull()?.trim() ?: ""
    }

    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    
    var availableActivities by remember { mutableStateOf<List<ActivityTemplateDto>>(emptyList()) }
    var selectedActivities by remember(itinerary) {
        mutableStateOf((itinerary.activities ?: emptyList()).sortedBy { it.startTime })
    }

    var activeTab by remember { mutableStateOf(0) } // 0 = Ordina, 1 = Aggiungi
    var searchQuery by remember { mutableStateOf("") }
    var activeTemplateForSessionSelection by remember { mutableStateOf<ActivityTemplateDto?>(null) }

    LaunchedEffect(city) {
        if (city.isNotBlank()) {
            isLoading = true
            val res = AppContainer.activityRepository.searchActivities(
                query = city, 
                minStartTime = null,
                size = 100
            )
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
                            Text("Personalizzazione a $city", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
            val totalPrice = selectedActivities.sumOf { it.price?.toDouble() ?: 0.0 }
            
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
                            text = "${selectedActivities.size} attività selezionate",
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
                            if (!isOnline) {
                                Toast.makeText(context, "Sei offline. Non è possibile salvare l'itinerario senza connessione.", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            val userId = AppContainer.sessionManager.getSessionUser()?.id
                            if (userId == null) {
                                Toast.makeText(context, "Errore: utente non loggato", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (selectedActivities.isEmpty()) {
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
                                    this.activityIds = selectedActivities.mapNotNull { it.id?.toString() }
                                    visibility = itinerary.visibility ?: "PRIVATE"
                                }
                                val res = AppContainer.itineraryRepository.createItinerary(req)
                                if (res.isSuccess && coverImageUri != null) {
                                    val createdItin = res.getOrNull()
                                    val itinId = createdItin?.id?.toString()
                                    if (itinId != null) {
                                        try {
                                            val inputStream = context.contentResolver.openInputStream(coverImageUri)
                                            val bytes = inputStream?.readBytes()
                                            inputStream?.close()
                                            if (bytes != null) {
                                                val mimeType = context.contentResolver.getType(coverImageUri) ?: "image/jpeg"
                                                val uploadRes = AppContainer.itineraryRepository.uploadItineraryImage(itinId, bytes, mimeType, "cover.jpg")
                                                if (uploadRes.isFailure) {
                                                    Toast.makeText(context, "Errore caricamento immagine copertina: ${uploadRes.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Impossibile leggere l'immagine di copertina", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                                isSaving = false
                                res.fold(
                                    onSuccess = {
                                        Toast.makeText(context, "Itinerario creato con successo!", Toast.LENGTH_SHORT).show()
                                        onPersonalizeSuccess()
                                    },
                                    onFailure = { err ->
                                        Toast.makeText(context, "Errore: ${err.message}", Toast.LENGTH_LONG).show()
                                    }
                                )
                            }
                        },
                        enabled = !isSaving && selectedActivities.isNotEmpty(),
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
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    focusManager.clearFocus()
                }
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
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Tab Header
                    TabRow(
                        selectedTabIndex = activeTab,
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.primary
                    ) {
                        Tab(
                            selected = activeTab == 0,
                            onClick = { activeTab = 0 },
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Reorder, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Ordina (${selectedActivities.size})", fontWeight = FontWeight.Bold)
                                }
                            }
                        )
                        Tab(
                            selected = activeTab == 1,
                            onClick = { activeTab = 1 },
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Aggiungi", fontWeight = FontWeight.Bold)
                                }
                            }
                        )
                    }

                    if (activeTab == 0) {
                        // TAB 1: ORDERING AND MANAGEMENT
                        if (selectedActivities.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                                Text("Nessuna attività selezionata. Vai su 'Aggiungi' per cercarne.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        } else {
                            Column(modifier = Modifier.fillMaxSize().weight(1f)) {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize().weight(1f),
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    items(
                                        items = selectedActivities,
                                        key = { act -> act.id?.toString() ?: "" }
                                    ) { activity ->
                                        val actId = activity.id?.toString() ?: ""
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { onActivityClick(actId) },
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
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = activity.name ?: "Attività senza nome",
                                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                        color = MaterialTheme.colorScheme.onSurface,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    val formattedDate = activity.startTime?.let { "${it.dayOfMonth}/${it.monthValue}/${it.year} ${String.format("%02d:%02d", it.hour, it.minute)}" } ?: "N/D"
                                                    val formattedEnd = activity.endTime?.let { "${String.format("%02d:%02d", it.hour, it.minute)}" } ?: "N/D"
                                                    Text(
                                                        text = "$formattedDate - $formattedEnd",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Text(
                                                        text = "€${String.format("%.2f", activity.price?.toDouble() ?: 0.0)}",
                                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                }

                                                Spacer(modifier = Modifier.width(16.dp))

                                                IconButton(
                                                    onClick = {
                                                        selectedActivities = selectedActivities.filter { it.id?.toString() != actId }
                                                    },
                                                    colors = IconButtonDefaults.iconButtonColors(
                                                        contentColor = MaterialTheme.colorScheme.error
                                                    )
                                                ) {
                                                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Rimuovi")
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // TAB 2: FIND AND ADD NEW ACTIVITIES
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            placeholder = { Text("Filtra attività...") },
                            leadingIcon = { Icon(Icons.Default.Search, null, tint = MaterialTheme.colorScheme.primary) },
                            shape = RoundedCornerShape(24.dp)
                        )

                        val filtered = availableActivities.filter { activity ->
                            activity.name?.contains(searchQuery, ignoreCase = true) == true ||
                            activity.location?.contains(searchQuery, ignoreCase = true) == true
                        }

                        if (filtered.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                                Text("Nessuna attività trovata", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize().weight(1f),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(filtered) { activity ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                val actId = activity.id?.toString()
                                                if (actId != null) {
                                                    onActivityClick(actId)
                                                }
                                            },
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
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
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        text = "⭐ ${activity.averageRating ?: 0.0}",
                                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                                        color = Color(0xFFEAB308)
                                                    )
                                                    activity.sessions?.firstOrNull()?.price?.let { price ->
                                                        Spacer(modifier = Modifier.width(12.dp))
                                                        Text(
                                                            text = "Da €${String.format("%.2f", price.toDouble())}",
                                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                                            color = MaterialTheme.colorScheme.primary
                                                        )
                                                    }
                                                }
                                            }

                                            Spacer(modifier = Modifier.width(16.dp))

                                            val countSelected = activity.sessions?.count { s -> selectedActivities.any { it.id?.toString() == s.id?.toString() } } ?: 0

                                            Button(
                                                onClick = {
                                                    activeTemplateForSessionSelection = activity
                                                },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = if (countSelected > 0) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer,
                                                    contentColor = if (countSelected > 0) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer
                                                ),
                                                shape = RoundedCornerShape(12.dp)
                                            ) {
                                                Text(if (countSelected > 0) "Aggiunto ($countSelected)" else "Scegli Orario")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (activeTemplateForSessionSelection != null) {
                val template = activeTemplateForSessionSelection!!
                AlertDialog(
                    onDismissRequest = { activeTemplateForSessionSelection = null },
                    title = { Text("Seleziona data e ora per ${template.name}") },
                    text = {
                        val sessions = template.sessions ?: emptyList()
                        if (sessions.isEmpty()) {
                            Text("Nessuna sessione programmata al momento.", style = MaterialTheme.typography.bodyMedium)
                        } else {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(sessions) { session ->
                                    val isSessionAdded = selectedActivities.any { it.id?.toString() == session.id?.toString() }
                                    val formattedDate = session.startTime?.let { "${it.dayOfMonth}/${it.monthValue}/${it.year} ${String.format("%02d:%02d", it.hour, it.minute)}" } ?: "N/D"
                                    val formattedEnd = session.endTime?.let { "${String.format("%02d:%02d", it.hour, it.minute)}" } ?: "N/D"
                                    
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable(enabled = !isSessionAdded) {
                                                val overlap = selectedActivities.any { existing ->
                                                    val sStart = session.startTime
                                                    val sEnd = session.endTime
                                                    val eStart = existing.startTime
                                                    val eEnd = existing.endTime
                                                    if (sStart != null && sEnd != null && eStart != null && eEnd != null) {
                                                        sStart.isBefore(eEnd) && sEnd.isAfter(eStart)
                                                    } else {
                                                        false
                                                    }
                                                }
                                                if (overlap) {
                                                    Toast.makeText(context, "Errore: si sovrappone con un'altra attività selezionata!", Toast.LENGTH_LONG).show()
                                                } else {
                                                    selectedActivities = (selectedActivities + session).sortedBy { it.startTime }
                                                    activeTemplateForSessionSelection = null
                                                }
                                            },
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isSessionAdded) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(text = "$formattedDate - $formattedEnd", fontWeight = FontWeight.Bold)
                                                Text(text = "Prezzo: €${session.price} | Posti: ${session.participants}", style = MaterialTheme.typography.bodySmall)
                                            }
                                            if (isSessionAdded) {
                                                Text("Selezionato", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {},
                    dismissButton = {
                        TextButton(onClick = { activeTemplateForSessionSelection = null }) {
                            Text("Annulla")
                        }
                    }
                )
            }
        }
    }
}
