package com.travel.app.presentation.components.activity

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.travel.app.data.AppContainer
import it.unical.ea.dtos.activity.ActivityDto
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import com.travel.app.domain.model.review.ReviewDto
import com.travel.app.domain.model.review.CreateReviewDto
import com.travel.app.presentation.components.review.ReviewCard
import com.travel.app.presentation.components.review.AddReviewInline
import kotlinx.coroutines.launch
import com.travel.app.utils.CalendarExportUtil

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ActivityDetailScreen(
    activityId: String,
    onNavigateBack: () -> Unit,
    isFavorite: Boolean = false,
    onFavoriteClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var activity by remember { mutableStateOf<ActivityDto?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    
    var reviews by remember { mutableStateOf<List<ReviewDto>>(emptyList()) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(activityId) {
        isLoading = true
        errorMsg = null
        val result = AppContainer.activityRepository.getActivityById(activityId)
        result.fold(
            onSuccess = {
                activity = it
                isLoading = false
            },
            onFailure = {
                errorMsg = it.message ?: "Impossibile caricare i dettagli dell'attività"
                isLoading = false
            }
        )
        val reviewsResult = AppContainer.reviewRepository.getReviewsForActivity(activityId)
        if (reviewsResult.isSuccess) {
            reviews = reviewsResult.getOrNull() ?: emptyList()
        }
    }

    val context = androidx.compose.ui.platform.LocalContext.current

    Scaffold(
        modifier = modifier,
        bottomBar = {
            if (activity != null) {
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
                                text = "Prezzo dell'attività",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            val priceVal = activity!!.price?.toDouble() ?: 0.0
                            Text(
                                text = if (priceVal == 0.0) "Gratis" else "€${String.format(Locale.getDefault(), "%.2f", priceVal)}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Button(
                            onClick = {
                                android.widget.Toast.makeText(
                                    context,
                                    "Prenotazione non ancora disponibile!",
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                            },
                            modifier = Modifier.height(48.dp),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Text("Prenota", fontWeight = FontWeight.Bold)
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
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator()
            } else if (errorMsg != null) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(text = errorMsg!!, color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onNavigateBack) {
                        Text("Torna Indietro")
                    }
                }
            } else if (activity != null) {
                val act = activity!!
                val scrollState = rememberScrollState()
                
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                ) {
                    // Hero Image Header
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                    ) {
                        val imageUrl = act.images?.firstOrNull() ?: "https://images.unsplash.com/photo-1488646953014-85cb44e25828?ixlib=rb-1.2.1&auto=format&fit=crop&w=800&q=80"
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = act.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Overlay Gradient
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    androidx.compose.ui.graphics.Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Black.copy(alpha = 0.4f),
                                            Color.Transparent,
                                            Color.Black.copy(alpha = 0.7f)
                                        )
                                    )
                                )
                        )

                        // Floating Circular Back Button
                        IconButton(
                            onClick = onNavigateBack,
                            modifier = Modifier
                                .padding(top = 16.dp, start = 16.dp)
                                .statusBarsPadding()
                                .size(44.dp)
                                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Indietro",
                                tint = Color.White
                            )
                        }

                        // Top Right Action Buttons
                        Row(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(top = 16.dp, end = 16.dp)
                                .statusBarsPadding(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Calendar Export Button
                            IconButton(
                                onClick = {
                                    CalendarExportUtil.exportToIcs(
                                        context = context,
                                        title = act.name ?: "Attività",
                                        description = act.description,
                                        location = act.location,
                                        startTime = act.startTime,
                                        endTime = act.endTime
                                    )
                                },
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = "Esporta Calendario",
                                    tint = Color.White
                                )
                            }
                            
                            // Favorite Button
                            IconButton(
                                onClick = onFavoriteClick,
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = "Preferito",
                                    tint = if (isFavorite) Color.Red else Color.White
                                )
                            }
                        }

                        // Title Overlay
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(horizontal = 24.dp, vertical = 20.dp)
                        ) {
                            Surface(
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "ATTIVITÀ",
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = act.name ?: "Nessun nome",
                                color = Color.White,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Content Details
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Info block: Dates & Locations
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Date Card
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarToday,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Column {
                                        Text(
                                            text = "Data",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        val start = act.startTime
                                        val dateText = if (start != null) formatDate(start) else "Non specificata"
                                        Text(
                                            text = dateText,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }

                            // Time Card
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Schedule,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Column {
                                        Text(
                                            text = "Orario",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        val start = act.startTime
                                        val end = act.endTime
                                        val timeText = if (start != null && end != null) {
                                            "${formatTime(start)} - ${formatTime(end)}"
                                        } else {
                                            "Non specificato"
                                        }
                                        Text(
                                            text = timeText,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }

                        // Organizer and Location Info
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = "Posizione",
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = act.location ?: "Nessuna località",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.People,
                                        contentDescription = "Posti",
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    val current = act.currentParticipants ?: 0
                                    val total = act.participants ?: 0
                                    Text(
                                        text = "Partecipanti: $current / $total posti",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }

                        Text(
                            text = "Organizzato da: ${act.organizer ?: "Operatore Locale"}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )

                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                            modifier = Modifier.padding(vertical = 8.dp)
                        )

                        // Description
                        Text(
                            text = "Descrizione",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = act.description ?: "Nessuna descrizione disponibile.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.3f
                        )

                        // Tags
                        val tags = act.tags ?: emptySet()
                        if (tags.isNotEmpty()) {
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                modifier = Modifier.padding(vertical = 8.dp)
                            )

                            Text(
                                text = "Tag / Categorie",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                tags.forEach { tag ->
                                    val formattedTag = tag.name.lowercase().replaceFirstChar { 
                                        if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() 
                                    }
                                    val bgColor = try {
                                        Color(android.graphics.Color.parseColor(tag.getBgColorHex()))
                                    } catch (e: Exception) {
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                    }
                                    val textColor = try {
                                        Color(android.graphics.Color.parseColor(tag.getTextColorHex()))
                                    } catch (e: Exception) {
                                        MaterialTheme.colorScheme.primary
                                    }
                                    Surface(
                                        color = bgColor,
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(1.dp, textColor.copy(alpha = 0.2f))
                                    ) {
                                        Text(
                                            text = formattedTag,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = textColor,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }

                        // REVIEWS SECTION
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Recensioni",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }

                        // Form inline per aggiungere una nuova recensione
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                AddReviewInline(
                                    onSubmit = { rating, comment ->
                                        scope.launch {
                                            val newReview = CreateReviewDto(
                                                activityId = activityId,
                                                rating = rating,
                                                comment = comment
                                            )
                                            AppContainer.reviewRepository.createReview(newReview)
                                            val reviewsResult = AppContainer.reviewRepository.getReviewsForActivity(activityId)
                                            if (reviewsResult.isSuccess) {
                                                reviews = reviewsResult.getOrNull() ?: emptyList()
                                            }
                                        }
                                    }
                                )
                            }
                        }
                        
                        if (reviews.isEmpty()) {
                            Text(
                                text = "Ancora nessuna recensione. Sii il primo!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                reviews.forEach { review ->
                                    ReviewCard(
                                        review = review, 
                                        showActivityName = false,
                                        onUpdate = { updatedRating, updatedComment ->
                                            scope.launch {
                                                val updateDto = CreateReviewDto(
                                                    activityId = activityId,
                                                    rating = updatedRating,
                                                    comment = updatedComment
                                                )
                                                review.id?.let {
                                                    AppContainer.reviewRepository.updateReview(it, updateDto)
                                                    val reviewsResult = AppContainer.reviewRepository.getReviewsForActivity(activityId)
                                                    if (reviewsResult.isSuccess) {
                                                        reviews = reviewsResult.getOrNull() ?: emptyList()
                                                    }
                                                }
                                            }
                                        },
                                        onDelete = {
                                            scope.launch {
                                                review.id?.let {
                                                    AppContainer.reviewRepository.deleteReview(it)
                                                    val reviewsResult = AppContainer.reviewRepository.getReviewsForActivity(activityId)
                                                    if (reviewsResult.isSuccess) {
                                                        reviews = reviewsResult.getOrNull() ?: emptyList()
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
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun formatDate(dateTime: LocalDateTime): String {
    return try {
        val formatter = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ITALIAN)
        dateTime.format(formatter)
    } catch (e: Exception) {
        dateTime.toString()
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun formatTime(dateTime: LocalDateTime): String {
    return try {
        val formatter = DateTimeFormatter.ofPattern("HH:mm", Locale.ITALIAN)
        dateTime.format(formatter)
    } catch (e: Exception) {
        dateTime.toString()
    }
}
