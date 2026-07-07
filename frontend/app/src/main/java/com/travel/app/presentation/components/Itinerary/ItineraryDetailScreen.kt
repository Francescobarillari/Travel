package com.travel.app.presentation.components.itinerary

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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import it.unical.ea.dtos.activity.ActivityDto
import it.unical.ea.dtos.itinerary.ItineraryDto
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import com.travel.app.data.AppContainer
import com.travel.app.domain.model.review.ReviewDto
import com.travel.app.domain.model.review.CreateReviewDto
import com.travel.app.presentation.components.review.ReviewCard
import com.travel.app.presentation.components.review.AddReviewInline
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import com.travel.app.utils.CalendarExportUtil

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ItineraryDetailScreen(
    itinerary: ItineraryDto,
    onNavigateBack: () -> Unit,
    onActivityClick: (String) -> Unit = {},
    isFavorite: Boolean = false,
    onFavoriteClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val totalPrice = itinerary.getActivities()?.sumOf { it.getPrice()?.toDouble() ?: 0.0 } ?: 0.0
    val uniqueTags = itinerary.getActivities()?.flatMap { it.getTags() ?: emptySet() }?.toSet() ?: emptySet()
    val uniqueLocations = itinerary.getActivities()?.map { it.getLocation() }?.filter { !it.isNullOrBlank() }?.distinct() ?: emptyList()
    val context = androidx.compose.ui.platform.LocalContext.current

    var reviews by remember { mutableStateOf<List<ReviewDto>>(emptyList()) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(itinerary.id) {
        val id = itinerary.id?.toString()
        if (id != null) {
            val reviewsResult = AppContainer.reviewRepository.getReviewsForItinerary(id)
            if (reviewsResult.isSuccess) {
                reviews = reviewsResult.getOrNull() ?: emptyList()
            }
        }
    }

    Scaffold(
        bottomBar = {
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
                            text = "Prezzo Totale Itinerario",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "€${String.format("%.2f", totalPrice)}",
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
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Hero Image Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
            ) {
                AsyncImage(
                    model = itinerary.getImageUrl() ?: "https://via.placeholder.com/800x600?text=No+Image",
                    contentDescription = itinerary.getTitle(),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Overlay Gradient and Back Button
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

                // Floating Circular Back Button (Glassmorphism look)
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .padding(top = 16.dp, start = 16.dp)
                        .statusBarsPadding()
                        .size(44.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
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
                            val locs = itinerary.getActivities()?.mapNotNull { it.getLocation() }?.filter { it.isNotBlank() }?.distinct()?.joinToString(", ")
                            CalendarExportUtil.addToCalendar(
                                context = context,
                                title = itinerary.getTitle() ?: "Itinerario",
                                description = itinerary.getDescription(),
                                location = locs,
                                startTime = itinerary.getStartDateTime(),
                                endTime = itinerary.getEndDateTime()
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

                    // Floating Circular Favorite Button
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

                // Title Overlay at Bottom of Image
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
                            text = "ITINERARIO",
                            color = MaterialTheme.colorScheme.onPrimary,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = itinerary.getTitle() ?: "Senza Titolo",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            // Itinerary Details Card
            Column(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp),
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
                                    text = "Periodo",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                val start = itinerary.getStartDateTime()
                                val end = itinerary.getEndDateTime()
                                val dateText = if (start != null && end != null) {
                                    "${formatDate(start)} - ${formatDate(end)}"
                                } else {
                                    "Date non specificate"
                                }
                                Text(
                                    text = dateText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    // Location Card
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
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text(
                                    text = "Destinazioni",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                val locationText = if (uniqueLocations.isNotEmpty()) {
                                    uniqueLocations.joinToString(", ")
                                } else {
                                    "Non specificata"
                                }
                                Text(
                                    text = locationText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                // Tags aggregate
                if (uniqueTags.isNotEmpty()) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        uniqueTags.forEach { tag ->
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

                // Description section
                if (!itinerary.getDescription().isNullOrBlank()) {
                    Text(
                        text = "Descrizione",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = itinerary.getDescription() ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.3f
                    )
                }

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                // Timeline header
                Text(
                    text = "Programma e Attività",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                // Timeline View of activities
                val activitiesList = itinerary.getActivities() ?: emptyList()
                if (activitiesList.isEmpty()) {
                    Text(
                        text = "Nessuna attività programmata per questo itinerario.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    activitiesList.forEachIndexed { index, activity ->
                        ActivityTimelineRow(
                            index = index + 1,
                            activity = activity,
                            isLast = index == activitiesList.lastIndex,
                            onClick = { activity.id?.toString()?.let(onActivityClick) }
                        )
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
                                    val id = itinerary.id?.toString()
                                    if (id != null) {
                                        val newReview = CreateReviewDto(
                                            itineraryId = id,
                                            rating = rating,
                                            comment = comment
                                        )
                                        AppContainer.reviewRepository.createReview(newReview)
                                        val reviewsResult = AppContainer.reviewRepository.getReviewsForItinerary(id)
                                        if (reviewsResult.isSuccess) {
                                            reviews = reviewsResult.getOrNull() ?: emptyList()
                                        }
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
                            // Here showActivityName is true to distinguish which activity the review is for
                            ReviewCard(
                                review = review, 
                                showActivityName = true,
                                onUpdate = { updatedRating, updatedComment ->
                                    scope.launch {
                                        val id = itinerary.id?.toString()
                                        if (id != null) {
                                            val updateDto = CreateReviewDto(
                                                itineraryId = id,
                                                rating = updatedRating,
                                                comment = updatedComment
                                            )
                                            review.id?.let { reviewId ->
                                                AppContainer.reviewRepository.updateReview(reviewId, updateDto)
                                                val reviewsResult = AppContainer.reviewRepository.getReviewsForItinerary(id)
                                                if (reviewsResult.isSuccess) {
                                                    reviews = reviewsResult.getOrNull() ?: emptyList()
                                                }
                                            }
                                        }
                                    }
                                },
                                onDelete = {
                                    scope.launch {
                                        val id = itinerary.id?.toString()
                                        if (id != null) {
                                            review.id?.let { reviewId ->
                                                AppContainer.reviewRepository.deleteReview(reviewId)
                                                val reviewsResult = AppContainer.reviewRepository.getReviewsForItinerary(id)
                                                if (reviewsResult.isSuccess) {
                                                    reviews = reviewsResult.getOrNull() ?: emptyList()
                                                }
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ActivityTimelineRow(
    index: Int,
    activity: ActivityDto,
    isLast: Boolean,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min) // essential to allow the vertical line to stretch to fill height
    ) {
        // Timeline node indicator on left
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(36.dp)
        ) {
            // Node circle
            Surface(
                modifier = Modifier.size(28.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = index.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // Connector line (only if it's not the last element)
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(2.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Activity detail card on right
        Card(
            onClick = onClick,
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Header (Name & Price)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = activity.getName() ?: "Attività Senza Nome",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = activity.getPrice()?.let { "€$it" } ?: "Gratis",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Info: Location
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = activity.getLocation() ?: "N/D",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Description
                if (!activity.getDescription().isNullOrBlank()) {
                    Text(
                        text = activity.getDescription() ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f)
                    )
                }

                // Activity tags
                val tags = activity.getTags() ?: emptySet()
                if (tags.isNotEmpty()) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
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
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = formattedTag,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = textColor,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
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
