package com.travel.app.presentation.home

import android.widget.Toast
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
import it.unical.ea.dtos.itinerary.CreateItineraryRequest
import it.unical.ea.dtos.itinerary.ItineraryDto
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PersonalizeItineraryScreen(
    itinerary: ItineraryDto,
    onNavigateBack: () -> Unit,
    onPersonalizeSuccess: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val itemHeightPx = remember(density) { with(density) { 110.dp.toPx() } }

    val city = remember(itinerary) {
        itinerary.activities?.firstOrNull()?.location?.split(",")?.firstOrNull()?.trim() ?: ""
    }

    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    
    var availableActivities by remember { mutableStateOf<List<ActivityDto>>(emptyList()) }
    var selectedActivities by remember(itinerary) {
        mutableStateOf(itinerary.activities ?: emptyList())
    }

    var activeTab by remember { mutableStateOf(0) } // 0 = Ordina, 1 = Aggiungi
    var searchQuery by remember { mutableStateOf("") }

    // Drag and drop states
    var draggingIndex by remember { mutableStateOf<Int?>(null) }
    var dragOffsetY by remember { mutableStateOf(0f) }

    LaunchedEffect(city, selectedActivities) {
        if (city.isNotBlank()) {
            isLoading = true
            
            // Calculate minStartTime
            val minStartTime = if (selectedActivities.isNotEmpty()) {
                selectedActivities.maxByOrNull { it.endTime ?: "" }?.endTime
            } else {
                itinerary.startDateTime
            }
            
            val res = AppContainer.activityRepository.searchActivities(
                query = city, 
                minStartTime = minStartTime,
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
                                // Info banner
                                Card(
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            "Tieni premuto e trascina un'attività su o giù per riordinarla.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }

                                LazyColumn(
                                    modifier = Modifier.fillMaxSize().weight(1f),
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    itemsIndexed(
                                        items = selectedActivities,
                                        key = { _, act -> act.id?.toString() ?: "" }
                                    ) { index, activity ->
                                        Box(
                                            modifier = Modifier
                                                .animateItemPlacement()
                                                .fillMaxWidth()
                                        ) {
                                            val actId = activity.id?.toString() ?: ""
                                            val isDraggingThis = draggingIndex == index
                                            
                                            Card(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .graphicsLayer {
                                                        translationY = if (isDraggingThis) dragOffsetY else 0f
                                                        scaleX = if (isDraggingThis) 1.04f else 1.0f
                                                        scaleY = if (isDraggingThis) 1.04f else 1.0f
                                                        shadowElevation = if (isDraggingThis) 8.dp.toPx() else 0f
                                                    },
                                                shape = RoundedCornerShape(16.dp),
                                                colors = CardDefaults.cardColors(
                                                    containerColor = if (isDraggingThis) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
                                                ),
                                                elevation = CardDefaults.cardElevation(defaultElevation = if (isDraggingThis) 6.dp else 2.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(16.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    // Drag handle
                                                    Box(
                                                        modifier = Modifier
                                                            .size(40.dp)
                                                            .pointerInput(index) {
                                                                detectDragGesturesAfterLongPress(
                                                                    onDragStart = {
                                                                        draggingIndex = index
                                                                        dragOffsetY = 0f
                                                                    },
                                                                    onDrag = { change, dragAmount ->
                                                                        change.consume()
                                                                        dragOffsetY += dragAmount.y
                                                                        
                                                                        val dragIdx = draggingIndex
                                                                        if (dragIdx != null) {
                                                                            if (dragOffsetY > itemHeightPx / 2 && dragIdx < selectedActivities.size - 1) {
                                                                                // Swap with next
                                                                                val mutableList = selectedActivities.toMutableList()
                                                                                val temp = mutableList[dragIdx]
                                                                                mutableList[dragIdx] = mutableList[dragIdx + 1]
                                                                                mutableList[dragIdx + 1] = temp
                                                                                selectedActivities = mutableList
                                                                                draggingIndex = dragIdx + 1
                                                                                dragOffsetY -= itemHeightPx
                                                                            } else if (dragOffsetY < -itemHeightPx / 2 && dragIdx > 0) {
                                                                                // Swap with previous
                                                                                val mutableList = selectedActivities.toMutableList()
                                                                                val temp = mutableList[dragIdx]
                                                                                mutableList[dragIdx] = mutableList[dragIdx - 1]
                                                                                mutableList[dragIdx - 1] = temp
                                                                                selectedActivities = mutableList
                                                                                draggingIndex = dragIdx - 1
                                                                                dragOffsetY += itemHeightPx
                                                                            }
                                                                        }
                                                                    },
                                                                    onDragEnd = {
                                                                        draggingIndex = null
                                                                        dragOffsetY = 0f
                                                                    },
                                                                    onDragCancel = {
                                                                        draggingIndex = null
                                                                        dragOffsetY = 0f
                                                                    }
                                                                )
                                                            },
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.DragHandle,
                                                            contentDescription = "Trascina",
                                                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                                        )
                                                    }

                                                    Spacer(modifier = Modifier.width(8.dp))

                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(
                                                            text = activity.name ?: "Attività senza nome",
                                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                            color = MaterialTheme.colorScheme.onSurface,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                        Spacer(modifier = Modifier.height(4.dp))
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
                                    val actId = activity.id?.toString() ?: ""
                                    val isAdded = selectedActivities.any { it.id?.toString() == actId }

                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
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
                                                Text(
                                                    text = "€${String.format("%.2f", activity.price?.toDouble() ?: 0.0)}",
                                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }

                                            Spacer(modifier = Modifier.width(16.dp))

                                            if (isAdded) {
                                                Button(
                                                    onClick = {
                                                        selectedActivities = selectedActivities.filter { it.id?.toString() != actId }
                                                    },
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = MaterialTheme.colorScheme.errorContainer,
                                                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                                                    ),
                                                    shape = RoundedCornerShape(12.dp)
                                                ) {
                                                    Text("Aggiunto")
                                                }
                                            } else {
                                                Button(
                                                    onClick = {
                                                        selectedActivities = selectedActivities + activity
                                                    },
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                                                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                                    ),
                                                    shape = RoundedCornerShape(12.dp)
                                                ) {
                                                    Text("Aggiungi")
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
        }
    }
}
