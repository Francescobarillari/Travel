package com.travel.app.presentation.home

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import it.unical.ea.dtos.activity.ActivityDto
import it.unical.ea.dtos.itinerary.ItineraryDto
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState

data class LocalitaItem(
    val name: String,
    val imageUrl: String
)

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeFeedScreen(
    onLocalitaClick: (String) -> Unit,
    onActivityClick: (String) -> Unit,
    onItineraryClick: (ItineraryDto) -> Unit,
    favoritesTrigger: Int = 0,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    val localitaList = listOf(
        LocalitaItem("Roma", "https://images.unsplash.com/photo-1552832230-c0197dd311b5?w=500&auto=format&fit=crop"),
        LocalitaItem("Venezia", "https://images.unsplash.com/photo-1527631746610-bca00a040d60?w=500&auto=format&fit=crop"),
        LocalitaItem("Firenze", "https://images.unsplash.com/photo-1478147427282-58a87a120781?w=500&auto=format&fit=crop"),
        LocalitaItem("Milano", "https://images.unsplash.com/photo-1520175480921-4edfa2983e0f?w=500&auto=format&fit=crop"),
        LocalitaItem("Napoli", "https://images.unsplash.com/photo-1595877244574-e90ce41ce089?w=500&auto=format&fit=crop"),
        LocalitaItem("Parigi", "https://images.unsplash.com/photo-1502602898657-3e91760cbb34?w=500&auto=format&fit=crop"),
        LocalitaItem("Palermo", "https://images.unsplash.com/photo-1590001155093-a3c66ab0c3ff?w=500&auto=format&fit=crop")
    )

    var allActivities by remember { mutableStateOf<List<ActivityDto>>(emptyList()) }
    var allItineraries by remember { mutableStateOf<List<ItineraryDto>>(emptyList()) }
    var favoriteActivityIds by remember { mutableStateOf(emptySet<String>()) }
    var favoriteItineraryIds by remember { mutableStateOf(emptySet<String>()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        isLoading = true
        val actResult = com.travel.app.data.AppContainer.activityRepository.getActivities()
        val itResult = com.travel.app.data.AppContainer.itineraryRepository.getItineraries()
        
        actResult.fold(
            onSuccess = { allActivities = it },
            onFailure = {}
        )
        itResult.fold(
            onSuccess = { allItineraries = it },
            onFailure = {}
        )
        isLoading = false
    }

    LaunchedEffect(favoritesTrigger) {
        favoriteActivityIds = com.travel.app.data.AppContainer.sessionManager.getFavoriteActivityIds()
        favoriteItineraryIds = com.travel.app.data.AppContainer.sessionManager.getFavoriteItineraryIds()
    }

    val favoriteItineraries = allItineraries.filter {
        favoriteItineraryIds.contains(it.id?.toString() ?: "")
    }

    val favoriteActivities = allActivities.filter {
        favoriteActivityIds.contains(it.id?.toString() ?: "")
    }

    // Algoritmo di raccomandazione:
    // 1. Legge la località con più interazioni dell'utente (se ha attività nel DB)
    // 2. Cerca località preferite dalle attività/itinerari preferiti (se hanno attività nel DB)
    // 3. Usa qualsiasi città con attività nel DB
    // 4. Fallback finale a "Roma"
    val mostInteracted = com.travel.app.data.AppContainer.sessionManager.getMostInteractedLocation()
    val favoriteLocations = (favoriteActivities.mapNotNull { it.location } + 
                              favoriteItineraries.flatMap { it.activities ?: emptyList() }.mapNotNull { it.location })
                              .map { it.split(",").first().trim() }
                              .filter { it.isNotBlank() }

    val citiesWithActivities = allActivities.mapNotNull { it.location?.split(",")?.first()?.trim() }
        .filter { it.isNotBlank() }
        .distinct()

    val recommendedCity = when {
        !mostInteracted.isNullOrBlank() && citiesWithActivities.any { it.equals(mostInteracted, ignoreCase = true) } -> {
            citiesWithActivities.first { it.equals(mostInteracted, ignoreCase = true) }
        }
        favoriteLocations.isNotEmpty() && favoriteLocations.any { fav -> citiesWithActivities.any { it.equals(fav, ignoreCase = true) } } -> {
            favoriteLocations.first { fav -> citiesWithActivities.any { it.equals(fav, ignoreCase = true) } }
        }
        citiesWithActivities.isNotEmpty() -> {
            citiesWithActivities.first()
        }
        else -> "Roma"
    }

    var recommendedActivities = allActivities.filter { activity ->
        val hasStarted = activity.startTime?.isBefore(java.time.LocalDateTime.now()) == true
        val city = activity.location?.split(",")?.first()?.trim() ?: ""
        city.equals(recommendedCity, ignoreCase = true) && 
        !favoriteActivityIds.contains(activity.id?.toString() ?: "") && // Esclude se già nei preferiti
        !hasStarted
    }

    // Se tutte le attività della città consigliata sono già preferite, mostrale comunque per non far sparire la sezione
    if (recommendedActivities.isEmpty()) {
        recommendedActivities = allActivities.filter { activity ->
            val hasStarted = activity.startTime?.isBefore(java.time.LocalDateTime.now()) == true
            val city = activity.location?.split(",")?.first()?.trim() ?: ""
            city.equals(recommendedCity, ignoreCase = true) &&
            !hasStarted
        }
    }

    val ptrState = rememberPullToRefreshState()
    if (ptrState.isRefreshing) {
        LaunchedEffect(true) {
            com.travel.app.data.AppContainer.clearCache()
            val actResult = com.travel.app.data.AppContainer.activityRepository.getActivities()
            val itResult = com.travel.app.data.AppContainer.itineraryRepository.getItineraries()
            actResult.fold(onSuccess = { allActivities = it }, onFailure = {})
            itResult.fold(onSuccess = { allItineraries = it }, onFailure = {})
            ptrState.endRefresh()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(ptrState.nestedScrollConnection)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(top = 16.dp, bottom = 110.dp)
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

        // 1. LOCALITÀ
        Text(
            text = "Località",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
        )
        
        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(localitaList) { item ->
                Card(
                    modifier = Modifier
                        .width(300.dp)
                        .height(200.dp)
                        .clickable { onLocalitaClick(item.name) },
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        AsyncImage(
                            model = item.imageUrl,
                            contentDescription = item.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color.Black.copy(alpha = 0.7f)
                                        )
                                    )
                                )
                        )
                        Text(
                            text = item.name,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            ),
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(20.dp)
                        )
                    }
                }
            }
        }

        // 2. TI POTREBBE INTERESSARE (Mostra solo se ci sono raccomandazioni)
        if (recommendedActivities.isNotEmpty()) {
            Spacer(modifier = Modifier.height(28.dp))
            
            Text(
                text = "Ti potrebbe interessare a $recommendedCity",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                contentPadding = PaddingValues(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(recommendedActivities) { activity ->
                    Card(
                        modifier = Modifier
                            .width(300.dp)
                            .height(200.dp)
                            .clickable { onActivityClick(activity.id?.toString() ?: "") },
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            val activityImg = activity.images?.firstOrNull() ?: "https://images.unsplash.com/photo-1488646953014-85cb44e25828?w=500&auto=format&fit=crop"
                            AsyncImage(
                                model = activityImg,
                                contentDescription = activity.name,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                Color.Transparent,
                                                Color.Black.copy(alpha = 0.7f)
                                            )
                                        )
                                    )
                            )
                            Column(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(20.dp)
                            ) {
                                Text(
                                    text = activity.name ?: "",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                val price = activity.price?.toDouble() ?: 0.0
                                val priceText = if (price <= 0.0) "Gratuito" else "€${String.format("%.2f", price)}"
                                Text(
                                    text = "${activity.location?.split(",")?.first()?.trim() ?: ""} • $priceText",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. ITINERARI PREFERITI
        Spacer(modifier = Modifier.height(28.dp))
        
        Text(
            text = "Itinerari Preferiti",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (favoriteItineraries.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .width(300.dp)
                            .height(200.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize().padding(24.dp)) {
                            Text(
                                text = "Non hai ancora salvato alcun itinerario nei preferiti.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(favoriteItineraries) { itinerary ->
                    Card(
                        modifier = Modifier
                            .width(300.dp)
                            .height(200.dp)
                            .clickable { onItineraryClick(itinerary) },
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            val itineraryImg = itinerary.imageUrl ?: "https://images.unsplash.com/photo-1469854523086-cc02fe5d8800?w=500&auto=format&fit=crop"
                            AsyncImage(
                                model = itineraryImg,
                                contentDescription = itinerary.title,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                Color.Transparent,
                                                Color.Black.copy(alpha = 0.7f)
                                            )
                                        )
                                    )
                            )
                            Column(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(20.dp)
                            ) {
                                Text(
                                    text = itinerary.title ?: "",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                val actCount = itinerary.activities?.size ?: 0
                                val actText = if (actCount == 1) "1 attività" else "$actCount attività"
                                Text(
                                    text = "Creato da: ${itinerary.creatorName ?: "Utente"} • $actText",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 4. ATTIVITÀ PREFERITE
        Spacer(modifier = Modifier.height(28.dp))
        
        Text(
            text = "Attività Preferite",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (favoriteActivities.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .width(300.dp)
                            .height(200.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize().padding(24.dp)) {
                            Text(
                                text = "Non hai ancora salvato alcuna attività nei preferiti.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(favoriteActivities) { activity ->
                    Card(
                        modifier = Modifier
                            .width(300.dp)
                            .height(200.dp)
                            .clickable { onActivityClick(activity.id?.toString() ?: "") },
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            val activityImg = activity.images?.firstOrNull() ?: "https://images.unsplash.com/photo-1488646953014-85cb44e25828?w=500&auto=format&fit=crop"
                            AsyncImage(
                                model = activityImg,
                                contentDescription = activity.name,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                Color.Transparent,
                                                Color.Black.copy(alpha = 0.7f)
                                            )
                                        )
                                    )
                            )
                            Column(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(20.dp)
                            ) {
                                Text(
                                    text = activity.name ?: "",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                val price = activity.price?.toDouble() ?: 0.0
                                val priceText = if (price <= 0.0) "Gratuito" else "€${String.format("%.2f", price)}"
                                Text(
                                    text = "${activity.location?.split(",")?.first()?.trim() ?: ""} • $priceText",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

        PullToRefreshContainer(
            state = ptrState,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}
