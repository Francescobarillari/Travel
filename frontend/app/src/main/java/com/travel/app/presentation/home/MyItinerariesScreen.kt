package com.travel.app.presentation.home

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Share
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.travel.app.data.AppContainer
import com.travel.app.domain.model.User
import com.travel.app.presentation.components.itinerary.ItineraryCard
import com.travel.app.presentation.components.itinerary.JoinItineraryDialog
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
    var joinedItineraries by remember { mutableStateOf<List<ItineraryDto>>(emptyList()) }
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var showJoinDialog by remember { mutableStateOf(false) }
    var shareDialogData by remember { mutableStateOf<Pair<String, String>?>(null) }

    var favoriteItineraryIds by remember { 
        mutableStateOf(AppContainer.sessionManager.getFavoriteItineraryIds()) 
    }
    var isLoading by remember { mutableStateOf(true) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val reloadFavorites = {
        favoriteItineraryIds = AppContainer.sessionManager.getFavoriteItineraryIds()
    }

    val loadData: () -> Unit = {
        val creatorId = user?.id
        if (creatorId != null) {
            isLoading = true
            errorMsg = null
            scope.launch {
                val myRes = AppContainer.itineraryRepository.getItinerariesByCreator(creatorId)
                myRes.fold(
                    onSuccess = { itineraries = it },
                    onFailure = { errorMsg = it.message }
                )
                val joinedRes = AppContainer.itineraryRepository.getJoinedItineraries()
                joinedRes.fold(
                    onSuccess = { joinedItineraries = it },
                    onFailure = { /* non bloccare se fallisce joined */ }
                )
                isLoading = false
            }
        }
    }

    LaunchedEffect(user?.id, refreshTrigger) {
        val creatorId = user?.id
        if (creatorId == null) {
            errorMsg = "ID utente non valido. Effettua nuovamente il login."
            isLoading = false
            return@LaunchedEffect
        }
        loadData()
    }

    if (showJoinDialog) {
        JoinItineraryDialog(
            onDismiss = { showJoinDialog = false },
            onJoinSuccess = {
                loadData()
            }
        )
    }

    if (shareDialogData != null) {
        ShareItineraryDialog(
            itineraryTitle = shareDialogData!!.first,
            shareCode = shareDialogData!!.second,
            onDismiss = { shareDialogData = null }
        )
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
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .statusBarsPadding()
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Indietro",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "I miei itinerari",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.weight(1f)
            )
            FilledTonalButton(
                onClick = { showJoinDialog = true },
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.GroupAdd,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Unisciti",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Tabs: Creati da me & Condivisi con me
        PrimaryTabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            Tab(
                selected = selectedTabIndex == 0,
                onClick = { selectedTabIndex = 0 },
                text = {
                    Text(
                        text = "Creati da me (${itineraries.size})",
                        fontWeight = if (selectedTabIndex == 0) FontWeight.Bold else FontWeight.Medium
                    )
                }
            )
            Tab(
                selected = selectedTabIndex == 1,
                onClick = { selectedTabIndex = 1 },
                text = {
                    Text(
                        text = "Condivisi con me (${joinedItineraries.size})",
                        fontWeight = if (selectedTabIndex == 1) FontWeight.Bold else FontWeight.Medium
                    )
                }
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
            } else if (selectedTabIndex == 0) {
                if (itineraries.isEmpty()) {
                    EmptyItinerariesState()
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp)
                    ) {
                        items(itineraries) { itinerary ->
                            val idStr = itinerary.id?.toString() ?: ""
                            val isFav = favoriteItineraryIds.contains(idStr)
                            var currentVisibility by remember(itinerary.id) { 
                                mutableStateOf(itinerary.visibility ?: "PRIVATE") 
                            }
                            var currentShareCode by remember(itinerary.id) {
                                mutableStateOf(itinerary.shareCode)
                            }
                            var menuExpanded by remember { mutableStateOf(false) }
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
                                        // Pending requests badge indicator
                                        if (itinerary.pendingRequestsCount != null && itinerary.pendingRequestsCount > 0) {
                                            Badge(
                                                containerColor = MaterialTheme.colorScheme.error,
                                                contentColor = MaterialTheme.colorScheme.onError
                                            ) {
                                                Text(
                                                    text = "${itinerary.pendingRequestsCount} in attesa",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                )
                                            }
                                        }

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
                                                                    currentShareCode = res.getOrNull()?.shareCode ?: currentShareCode
                                                                }
                                                            }
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                        
                                        // Share button: shown if visibility is PUBLIC or SHARED
                                        if (currentVisibility == "PUBLIC" || currentVisibility == "SHARED") {
                                            IconButton(
                                                onClick = {
                                                    if (currentVisibility == "SHARED") {
                                                        val codeToUse = currentShareCode ?: itinerary.shareCode
                                                        if (!codeToUse.isNullOrBlank()) {
                                                            shareDialogData = Pair(itinerary.title ?: "Itinerario", codeToUse)
                                                        } else {
                                                            coroutineScope.launch {
                                                                val res = AppContainer.itineraryRepository.updateItineraryVisibility(idStr, "SHARED")
                                                                val code = res.getOrNull()?.shareCode
                                                                if (!code.isNullOrBlank()) {
                                                                    currentShareCode = code
                                                                    itinerary.shareCode = code
                                                                    shareDialogData = Pair(itinerary.title ?: "Itinerario", code)
                                                                } else {
                                                                    val getRes = AppContainer.itineraryRepository.getItineraryById(idStr)
                                                                    val fallbackCode = getRes.getOrNull()?.shareCode
                                                                    if (!fallbackCode.isNullOrBlank()) {
                                                                        currentShareCode = fallbackCode
                                                                        itinerary.shareCode = fallbackCode
                                                                        shareDialogData = Pair(itinerary.title ?: "Itinerario", fallbackCode)
                                                                    } else {
                                                                        Toast.makeText(context, "Impossibile recuperare il codice di condivisione. Riprova.", Toast.LENGTH_SHORT).show()
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    } else {
                                                        val shareText = "Guarda il mio itinerario '${itinerary.title}' su Dèrive! http://derive.app/itinerary/$idStr"
                                                        val sendIntent = Intent().apply {
                                                            action = Intent.ACTION_SEND
                                                            putExtra(Intent.EXTRA_TEXT, shareText)
                                                            type = "text/plain"
                                                        }
                                                        val shareIntent = Intent.createChooser(sendIntent, "Condividi itinerario")
                                                        context.startActivity(shareIntent)
                                                    }
                                                }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Share,
                                                    contentDescription = "Condividi",
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
            } else {
                // Tab 1: Condivisi con me
                if (joinedItineraries.isEmpty()) {
                    EmptyJoinedItinerariesState(onJoinClick = { showJoinDialog = true })
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp)
                    ) {
                        items(joinedItineraries) { itinerary ->
                            val idStr = itinerary.id?.toString() ?: ""
                            val isFav = favoriteItineraryIds.contains(idStr)

                            ItineraryCard(
                                itinerary = itinerary,
                                isFavorite = isFav,
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
        }
    }
}

@Composable
fun ShareItineraryDialog(
    itineraryTitle: String,
    shareCode: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Condividi Codice",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Ecco il codice di accesso per '$itineraryTitle'. Chi riceve questo codice potrà inserirlo nell'app per richiedere di unirsi al viaggio:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(vertical = 16.dp, horizontal = 12.dp)
                    ) {
                        Text(
                            text = "CODICE DI CONDIVISIONE",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = shareCode,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 4.sp
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FilledTonalButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                            val clip = ClipData.newPlainText("Codice Itinerario", shareCode)
                            clipboard?.setPrimaryClip(clip)
                            Toast.makeText(context, "Codice '$shareCode' copiato negli appunti!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copia")
                    }

                    Button(
                        onClick = {
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, shareCode)
                                type = "text/plain"
                            }
                            val shareIntent = Intent.createChooser(sendIntent, "Invia codice")
                            context.startActivity(shareIntent)
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Share, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Invia")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Chiudi", fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
private fun EmptyItinerariesState() {
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
            text = "Non hai ancora creato nessun itinerario.",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Inizia subito a pianificare la tua prossima avventura creando un nuovo itinerario personalizzato!",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun EmptyJoinedItinerariesState(onJoinClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Group,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outlineVariant,
            modifier = Modifier.size(72.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Nessun itinerario condiviso con te.",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Hai ricevuto un codice di condivisione da un amico o compagno di viaggio? Unisciti al suo itinerario per visualizzarlo qui!",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = onJoinClick,
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.GroupAdd,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Unisciti con Codice",
                fontWeight = FontWeight.Bold
            )
        }
    }
}
