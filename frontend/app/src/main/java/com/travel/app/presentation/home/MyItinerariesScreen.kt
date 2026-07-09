package com.travel.app.presentation.home

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import android.content.Intent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.travel.app.data.AppContainer
import com.travel.app.domain.model.User
import com.travel.app.presentation.components.itinerary.ItineraryCard
import it.unical.ea.dtos.itinerary.ItineraryDto
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyItinerariesScreen(
    user: User?,
    onBack: () -> Unit,
    onItineraryClick: (ItineraryDto) -> Unit,
    refreshTrigger: Int = 0,
    modifier: Modifier = Modifier
) {
    var itineraries by remember { mutableStateOf<List<ItineraryDto>>(emptyList()) }
    var favoriteItineraryIds by remember { 
        mutableStateOf(AppContainer.sessionManager.getFavoriteItineraryIds()) 
    }
    var isLoading by remember { mutableStateOf(true) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val reloadFavorites = {
        favoriteItineraryIds = AppContainer.sessionManager.getFavoriteItineraryIds()
    }

    LaunchedEffect(user?.id, refreshTrigger) {
        val creatorId = user?.id
        if (creatorId == null) {
            errorMsg = "ID utente non valido. Effettua nuovamente il login."
            isLoading = false
            return@LaunchedEffect
        }
        
        isLoading = true
        errorMsg = null
        val result = AppContainer.itineraryRepository.getItinerariesByCreator(creatorId)
        result.fold(
            onSuccess = { list ->
                itineraries = list
            },
            onFailure = { err ->
                errorMsg = err.message ?: "Errore durante il caricamento dei tuoi itinerari"
            }
        )
        isLoading = false
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
                text = "I miei itinerari",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            )
        }

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
                    modifier = Modifier.padding(24.dp),
                    textAlign = TextAlign.Center
                )
            } else if (itineraries.isEmpty()) {
                EmptyItinerariesState()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 90.dp)
                ) {
                    items(itineraries) { itinerary ->
                        val idStr = itinerary.id?.toString() ?: ""
                        val isFav = favoriteItineraryIds.contains(idStr)
                        var currentVisibility by remember(itinerary.id) { 
                            mutableStateOf(itinerary.visibility ?: "PRIVATE") 
                        }
                        var menuExpanded by remember { mutableStateOf(false) }
                        val context = LocalContext.current
                        val coroutineScope = rememberCoroutineScope()

                        ItineraryCard(
                            itinerary = itinerary,
                            isFavorite = isFav,
                            onFavoriteClick = {
                                AppContainer.sessionManager.toggleFavoriteItinerary(idStr)
                                reloadFavorites()
                            },
                            onClick = { onItineraryClick(itinerary) },
                            actions = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Visibility Button
                                    Box {
                                        val visibilityIcon = when (currentVisibility) {
                                            "PUBLIC" -> Icons.Default.Public
                                            "SHARED" -> Icons.Default.Group
                                            else -> Icons.Default.Lock
                                        }
                                        val visibilityText = when (currentVisibility) {
                                            "PUBLIC" -> "Pubblico"
                                            "SHARED" -> "Condiviso"
                                            else -> "Privato"
                                        }
                                        
                                        IconButton(onClick = { menuExpanded = true }) {
                                            Icon(
                                                imageVector = visibilityIcon,
                                                contentDescription = "Visibilità: $visibilityText",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        
                                        MaterialTheme(
                                            shapes = MaterialTheme.shapes.copy(extraSmall = RoundedCornerShape(16.dp))
                                        ) {
                                            DropdownMenu(
                                                expanded = menuExpanded,
                                                onDismissRequest = { menuExpanded = false },
                                                modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f))
                                            ) {
                                                DropdownMenuItem(
                                                    text = { Text("Privato", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
                                                    leadingIcon = { Icon(Icons.Default.Lock, null, tint = MaterialTheme.colorScheme.primary) },
                                                    onClick = {
                                                        menuExpanded = false
                                                        coroutineScope.launch {
                                                            val res = AppContainer.itineraryRepository.updateItineraryVisibility(idStr, "PRIVATE")
                                                            if (res.isSuccess) {
                                                                currentVisibility = "PRIVATE"
                                                            }
                                                        }
                                                    }
                                                )
                                                DropdownMenuItem(
                                                    text = { Text("Pubblico", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
                                                    leadingIcon = { Icon(Icons.Default.Public, null, tint = MaterialTheme.colorScheme.primary) },
                                                    onClick = {
                                                        menuExpanded = false
                                                        coroutineScope.launch {
                                                            val res = AppContainer.itineraryRepository.updateItineraryVisibility(idStr, "PUBLIC")
                                                            if (res.isSuccess) {
                                                                currentVisibility = "PUBLIC"
                                                            }
                                                        }
                                                    }
                                                )
                                                DropdownMenuItem(
                                                    text = { Text("Condiviso", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
                                                    leadingIcon = { Icon(Icons.Default.Group, null, tint = MaterialTheme.colorScheme.primary) },
                                                    onClick = {
                                                        menuExpanded = false
                                                        coroutineScope.launch {
                                                            val res = AppContainer.itineraryRepository.updateItineraryVisibility(idStr, "SHARED")
                                                            if (res.isSuccess) {
                                                                currentVisibility = "SHARED"
                                                            }
                                                        }
                                                    }
                                                )
                                            }
                                        }
                                    }
                                    
                                    // Share button: only shown if visbility is PUBLIC or SHARED
                                    if (currentVisibility == "PUBLIC" || currentVisibility == "SHARED") {
                                        IconButton(
                                            onClick = {
                                                val sendIntent = Intent().apply {
                                                    action = Intent.ACTION_SEND
                                                    putExtra(Intent.EXTRA_TEXT, "Guarda il mio itinerario '${itinerary.title}' su Dèrive! http://derive.app/itinerary/$idStr")
                                                    type = "text/plain"
                                                }
                                                val shareIntent = Intent.createChooser(sendIntent, "Condividi itinerario")
                                                context.startActivity(shareIntent)
                                            }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Share,
                                                contentDescription = "Condividi con amici",
                                                tint = MaterialTheme.colorScheme.secondary
                                            )
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyItinerariesState() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Construction,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outlineVariant,
            modifier = Modifier.size(72.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Non hai ancora creato alcun itinerario personalizzato.",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Apri un itinerario esistente nella scheda Esplora e premi l'icona del martello per personalizzarlo!",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
