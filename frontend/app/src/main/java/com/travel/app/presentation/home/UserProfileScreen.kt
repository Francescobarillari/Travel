package com.travel.app.presentation.home

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.travel.app.data.AppContainer
import com.travel.app.domain.model.User
import com.travel.app.presentation.components.itinerary.ItineraryCard
import com.travel.app.presentation.components.review.ReviewCard
import com.travel.app.domain.model.review.ReviewDto
import it.unical.ea.dtos.itinerary.ItineraryDto
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    user: User,
    onBack: () -> Unit,
    onItineraryClick: (ItineraryDto) -> Unit,
    onReviewClick: (ReviewDto) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var itineraries by remember { mutableStateOf<List<ItineraryDto>>(emptyList()) }
    var reviews by remember { mutableStateOf<List<ReviewDto>>(emptyList()) }
    var selectedTab by remember { mutableStateOf(0) }
    
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val favoriteItineraryIds = remember { mutableStateMapOf<String, Boolean>() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(user.id) {
        val userId = user.id
        if (userId == null) {
            errorMessage = "ID utente non valido."
            isLoading = false
            return@LaunchedEffect
        }

        isLoading = true
        errorMessage = null
        
        // 1. Fetch itineraries
        val result = AppContainer.itineraryRepository.getItinerariesByCreator(userId)
        result.fold(
            onSuccess = { list ->
                // Filter only PUBLIC itineraries
                itineraries = list.filter { it.visibility == "PUBLIC" }
                
                // Initialize favorites
                val favItIds = AppContainer.sessionManager.getFavoriteItineraryIds()
                itineraries.forEach { itin ->
                    val idStr = itin.id?.toString() ?: ""
                    favoriteItineraryIds[idStr] = favItIds.contains(idStr)
                }
            },
            onFailure = { err ->
                errorMessage = err.message ?: "Errore durante il caricamento degli itinerari"
            }
        )

        // 2. Fetch reviews
        val reviewResult = AppContainer.reviewRepository.getReviewsByUser(userId)
        reviewResult.fold(
            onSuccess = { list ->
                reviews = list
            },
            onFailure = { err ->
                if (errorMessage == null) {
                    errorMessage = err.message ?: "Errore durante il caricamento delle recensioni"
                }
            }
        )
        
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profilo Utente", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Indietro")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // Header Info Card
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp,
                    shadowElevation = 4.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Large Avatar
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!user.avatarUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = user.avatarUrl,
                                    contentDescription = "Avatar grande",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Text(
                                    text = user.name?.firstOrNull()?.uppercase() ?: "?",
                                    style = MaterialTheme.typography.headlineLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = user.name ?: "Utente",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = if (user.userType == "SOCIETA") "Società/Agenzia" else "Viaggiatore",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        if (!user.phone.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Tel: ${user.phone}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Tabs for Itineraries vs Reviews
            item {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 24.dp)
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Itinerari (${itineraries.size})", fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Recensioni (${reviews.size})", fontWeight = FontWeight.Bold) }
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            } else if (errorMessage != null) {
                item {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(24.dp)
                    )
                }
            } else {
                if (selectedTab == 0) {
                    // ITINERARIES TAB
                    if (itineraries.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(48.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Nessun itinerario pubblico creato da questo utente.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }
                    } else {
                        items(itineraries) { itinerary ->
                            val idStr = itinerary.id?.toString() ?: ""
                            val isFav = favoriteItineraryIds[idStr] == true
                            
                            ItineraryCard(
                                itinerary = itinerary,
                                isFavorite = isFav,
                                onFavoriteClick = {
                                    AppContainer.sessionManager.toggleFavoriteItinerary(idStr)
                                    favoriteItineraryIds[idStr] = !isFav
                                },
                                onClick = { onItineraryClick(itinerary) }
                            )
                        }
                    }
                } else {
                    // REVIEWS TAB
                    if (reviews.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(48.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Nessuna recensione scritta da questo utente.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }
                    } else {
                        items(reviews) { review ->
                            ReviewCard(
                                review = review,
                                showActivityName = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp, vertical = 8.dp)
                                    .clickable { onReviewClick(review) }
                            )
                        }
                    }
                }
            }
        }
    }
}
