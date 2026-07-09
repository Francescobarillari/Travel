package com.travel.app.presentation.admin.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import it.unical.ea.dtos.activity.ActivityDto
import java.time.format.DateTimeFormatter

// Helper per aggregare le sessioni di un'attività multi-giorno.
// Rispecchiano quelli privati di CompanyDashboardScreen: sull'endpoint admin
// `sessions` è null e start/end sono già min/max, quindi degradano correttamente.
private fun sessionsOf(activity: ActivityDto): List<ActivityDto> =
    activity.sessions?.takeIf { it.isNotEmpty() } ?: listOf(activity)

private fun bookedSeats(activity: ActivityDto): Int =
    sessionsOf(activity).sumOf { it.currentParticipants ?: 0 }

private fun capacitySeats(activity: ActivityDto): Int =
    sessionsOf(activity).sumOf { it.participants ?: 0 }

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ActivityModerationCard(
    activity: ActivityDto,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    isActing: Boolean = false
) {
    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd MMM yyyy") }
    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm") }

    val sessions = sessionsOf(activity)
    val firstStart = sessions.mapNotNull { it.startTime }.minOrNull()
    val lastEnd = sessions.mapNotNull { it.endTime }.maxOrNull()
    val dateText = when {
        firstStart == null -> "Date da definire"
        lastEnd == null || firstStart.toLocalDate() == lastEnd.toLocalDate() -> {
            val timePart = if (lastEnd != null)
                " · ${firstStart.format(timeFormatter)} - ${lastEnd.format(timeFormatter)}"
            else ""
            firstStart.format(dateFormatter) + timePart
        }
        else -> "Dal ${firstStart.format(dateFormatter)} al ${lastEnd.format(dateFormatter)}"
    }

    val imageUrl = activity.images?.firstOrNull()?.let { adminActivityImageUrl(it) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column {
            // Anteprima immagine con pill di stato in overlay
            if (imageUrl != null) {
                Box {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = "Immagine di ${activity.name ?: "attività"}",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                            .background(
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                                RoundedCornerShape(8.dp)
                            )
                    ) {
                        StatusPill(text = "DA MODERARE", color = AdminStatusColors.pending)
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (imageUrl == null) {
                    StatusPill(text = "DA MODERARE", color = AdminStatusColors.pending)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = activity.name ?: "Senza Nome",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "€${activity.price ?: "0.00"}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "Organizzato da: ${activity.organizer ?: "Agenzia"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    activity.createdAt?.let { created ->
                        Text(
                            text = "Proposta il ${created.format(dateFormatter)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Tag/Categorie dell'attività
                if (!activity.tags.isNullOrEmpty()) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        activity.tags.take(6).forEach { tag ->
                            val bgColor = try {
                                Color(android.graphics.Color.parseColor(tag.bgColorHex))
                            } catch (e: Exception) {
                                Color(0xFFF1F5F9)
                            }
                            val textColor = try {
                                Color(android.graphics.Color.parseColor(tag.textColorHex))
                            } catch (e: Exception) {
                                Color(0xFF475569)
                            }
                            Box(
                                modifier = Modifier
                                    .background(bgColor, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = tag.name.replace("_", " ").lowercase()
                                        .replaceFirstChar { it.uppercase() },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textColor
                                )
                            }
                        }
                    }
                }

                if (!activity.description.isNullOrBlank()) {
                    Text(
                        text = activity.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    InfoRow(icon = Icons.Default.LocationOn, label = "Luogo:", value = activity.location ?: "-")
                    InfoRow(icon = Icons.Default.CalendarToday, label = "Date:", value = dateText)
                    InfoRow(
                        icon = Icons.Default.People,
                        label = "Iscritti:",
                        value = "${bookedSeats(activity)}/${capacitySeats(activity)} posti"
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onReject,
                        enabled = !isActing,
                        modifier = Modifier.weight(1f).height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = if (isActing) 0.4f else 1f))
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Rifiuta", style = MaterialTheme.typography.labelMedium)
                    }

                    Button(
                        onClick = onApprove,
                        enabled = !isActing,
                        modifier = Modifier.weight(1f).height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        if (isActing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = LocalContentColor.current
                            )
                        } else {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Approva", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}
