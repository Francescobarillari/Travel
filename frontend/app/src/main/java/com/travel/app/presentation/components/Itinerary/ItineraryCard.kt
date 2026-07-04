package com.travel.app.presentation.components.itinerary

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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.travel.app.presentation.theme.TravelTheme
import it.unical.ea.dtos.activity.ActivityDto
import it.unical.ea.dtos.itinerary.ItineraryDto
import it.unical.ea.enums.TravelTag
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.lazy.items
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ItineraryCard(
    itinerary: ItineraryDto,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            // Header Image (reduced height for 2-column grid)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            ) {
                AsyncImage(
                    model = itinerary.imageUrl ?: "https://via.placeholder.com/600x400?text=No+Image",
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Header with Title and Price on the right
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = itinerary.title ?: "Senza Titolo",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    val totalPrice = itinerary.activities?.sumOf { it.price?.toDouble() ?: 0.0 } ?: 0.0
                    Text(
                        text = "€${String.format("%.2f", totalPrice)}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Dynamic tags inherited from its activities
                val itineraryTags = itinerary.activities?.flatMap { it.tags ?: emptySet() }?.toSet() ?: emptySet()
                if (itineraryTags.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    androidx.compose.foundation.lazy.LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(itineraryTags.toList()) { tag ->
                            val formattedTag = tag.name.lowercase().replaceFirstChar { 
                                if (it.isLowerCase()) it.titlecase(java.util.Locale.getDefault()) else it.toString() 
                            }
                            val bgColor = try {
                                Color(android.graphics.Color.parseColor(tag.bgColorHex))
                            } catch (e: Exception) {
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                            }
                            val textColor = try {
                                Color(android.graphics.Color.parseColor(tag.textColorHex))
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

                // Destination
                val destination = itinerary.activities?.firstOrNull()?.location ?: "Destinazione non specificata"
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

                if (!itinerary.description.isNullOrBlank()) {
                    Text(
                        text = itinerary.description ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        maxLines = 2,
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
                    // Dates
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
                        val start = itinerary.startDateTime
                        val end = itinerary.endDateTime
                        if (start != null && end != null) {
                            Text(
                                text = "${formatDate(start)} - ${formatDate(end)}",
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

@Composable
private fun PriceBadge(
    price: Double,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.primary,
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = "€${String.format("%.2f", price)}",
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun formatDate(dateTime: LocalDateTime): String {
    return try {
        val formatter = DateTimeFormatter.ofPattern("dd MMM")
        dateTime.format(formatter)
    } catch (e: Exception) {
        dateTime.toString()
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun ItineraryCardPreview() {
    val mockItinerary = ItineraryDto().apply {
        title = "Weekend a Roma"
        description = "Un fantastico weekend alla scoperta della città eterna."
        startDateTime = LocalDateTime.of(2025, 7, 1, 9, 0)
        endDateTime = LocalDateTime.of(2025, 7, 3, 18, 0)
        imageUrl = null
        activities = listOf(
            ActivityDto().apply {
                name = "Visita al Colosseo"
                location = "Roma"
                price = BigDecimal("15.50")
            },
            ActivityDto().apply {
                name = "Cena a Trastevere"
                location = "Roma"
                price = BigDecimal("45.00")
            }
        )
    }

    TravelTheme {
        ItineraryCard(
            itinerary = mockItinerary,
            actions = {
                TextButton(onClick = {}) {
                    Text("Dettagli")
                }
            }
        )
    }
}


