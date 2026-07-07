package com.travel.app.presentation.components.activity

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.travel.app.presentation.theme.TravelTheme
import androidx.compose.foundation.lazy.items
import it.unical.ea.dtos.activity.ActivityDto
import it.unical.ea.enums.TravelTag
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.background
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ActivityCard(
    activity: ActivityDto,
    modifier: Modifier = Modifier,
    isFavorite: Boolean = false,
    onFavoriteClick: () -> Unit = {},
    onClick: () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {}
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            // Header Image (first image from images list, or placeholder)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                val imageUrl = activity.images?.firstOrNull()
                AsyncImage(
                    model = imageUrl ?: "https://via.placeholder.com/600x400?text=No+Image",
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
                    contentScale = ContentScale.Crop
                )

                // Heart icon in top-right corner
                IconButton(
                    onClick = onFavoriteClick,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .size(36.dp)
                        .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Preferiti",
                        tint = if (isFavorite) Color.Red else Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Title and Price Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = activity.name ?: "Senza Titolo",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    val priceDouble = activity.price?.toDouble() ?: 0.0
                    val priceText = if (priceDouble <= 0.0) "Gratuito" else "€${String.format("%.2f", priceDouble)}"
                    Text(
                        text = priceText,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (priceDouble <= 0.0) Color(0xFF15803D) else MaterialTheme.colorScheme.primary
                    )
                }

                // Organizer info
                if (!activity.organizer.isNullOrBlank()) {
                    Text(
                        text = "Organizzato da: ${activity.organizer}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Tags row
                val activityTags = activity.tags
                if (!activityTags.isNullOrEmpty()) {
                    androidx.compose.foundation.lazy.LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(activityTags.toList()) { tag ->
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
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    textColor.copy(alpha = 0.2f)
                                )
                            ) {
                                Text(
                                    text = formattedTag,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = textColor,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                // Location / Position
                val destination = activity.location ?: "Posizione non specificata"
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = destination,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Description
                if (!activity.description.isNullOrBlank()) {
                    Text(
                        text = activity.description ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 2.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Dates/Times
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(14.dp)
                        )
                        val start = activity.startTime
                        val end = activity.endTime
                        if (start != null && end != null) {
                            Text(
                                text = "${formatDateTime(start)} - ${formatDateTime(end)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }

                    // Optional Actions
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        content = actions
                    )
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun formatDateTime(dateTime: LocalDateTime): String {
    return try {
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
        dateTime.format(formatter)
    } catch (e: Exception) {
        dateTime.toString()
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun ActivityCardPreview() {
    val mockActivity = ActivityDto().apply {
        name = "Visita Guidata al Colosseo"
        description = "Esplora il monumento più famoso di Roma con una guida esperta che ti racconterà i dettagli storici."
        location = "Roma, Italia"
        startTime = LocalDateTime.of(2025, 7, 1, 10, 0)
        endTime = LocalDateTime.of(2025, 7, 1, 13, 0)
        price = BigDecimal("25.00")
        organizer = "Roma Tour Operator"
        tags = setOf(TravelTag.CULTURA, TravelTag.STORIA)
    }

    TravelTheme {
        ActivityCard(
            activity = mockActivity,
            actions = {
                TextButton(onClick = {}) {
                    Text("Dettagli")
                }
            }
        )
    }
}
