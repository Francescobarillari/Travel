package com.travel.app.presentation.components.notifications

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.travel.app.data.AppContainer
import com.travel.app.service.notification.NotificationHelper
import it.unical.ea.dtos.notification.NotificationDto
import it.unical.ea.enums.NotificationType
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

private const val POLL_INTERVAL_MS = 30_000L

/**
 * Campanella delle notifiche da mettere in alto a destra nelle homepage.
 *
 * Finché è visibile fa polling delle notifiche non lette: aggiorna il badge e
 * mostra anche la push di sistema per quelle nuove (così le notifiche arrivano
 * pure ad app aperta; ad app chiusa ci pensa il NotificationSyncWorker).
 * Toccandola si apre il pannello per leggere e segnare come lette le notifiche.
 */
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationBell(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val isPreview = androidx.compose.ui.platform.LocalInspectionMode.current
    var notifications by remember { mutableStateOf<List<NotificationDto>>(emptyList()) }
    var showPanel by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        if (isPreview) return@LaunchedEffect
        while (true) {
            try {
                val unread = AppContainer.apiService.getUnreadNotifications()
                // Push di sistema per le notifiche mai mostrate (consegna in foreground)
                for (notification in unread) {
                    val notificationId = notification.id?.toString() ?: continue
                    if (!NotificationHelper.hasBeenShown(context, notificationId)) {
                        NotificationHelper.showNotification(
                            context = context,
                            title = notification.title ?: "Dèrive",
                            message = notification.message ?: "",
                            channelId = NotificationHelper.CHANNEL_BOOKINGS_ID,
                            notificationId = notificationId.hashCode()
                        )
                        NotificationHelper.markAsShown(context, notificationId)
                    }
                }
                notifications = unread.sortedByDescending { it.createdAt }
            } catch (_: Exception) {
                // Rete assente o sessione scaduta: si riprova al prossimo giro
            }
            delay(POLL_INTERVAL_MS)
        }
    }

    fun markAsRead(notification: NotificationDto) {
        val id = notification.id?.toString() ?: return
        scope.launch {
            try {
                AppContainer.apiService.markNotificationAsRead(id)
                notifications = notifications.filterNot { it.id?.toString() == id }
            } catch (_: Exception) {
            }
        }
    }

    Box(modifier = modifier) {
        IconButton(onClick = { showPanel = true }) {
            Icon(
                imageVector = Icons.Outlined.Notifications,
                contentDescription = "Notifiche",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
        if (notifications.isNotEmpty()) {
            Badge(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 6.dp, end = 6.dp),
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError
            ) {
                Text(
                    text = if (notifications.size > 9) "9+" else "${notifications.size}",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    if (showPanel) {
        ModalBottomSheet(
            onDismissRequest = { showPanel = false },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Notifiche",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    if (notifications.isNotEmpty()) {
                        TextButton(onClick = {
                            notifications.forEach { markAsRead(it) }
                        }) {
                            Text(
                                text = "Segna tutte come lette",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                if (notifications.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Notifications,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(40.dp)
                        )
                        Text(
                            text = "Nessuna nuova notifica",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Ti avviseremo qui quando ci saranno novità.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 480.dp),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(notifications, key = { it.id?.toString() ?: it.hashCode().toString() }) { notification ->
                            NotificationRow(
                                notification = notification,
                                onMarkAsRead = { markAsRead(notification) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun NotificationRow(
    notification: NotificationDto,
    onMarkAsRead: () -> Unit
) {
    val (icon, iconColor) = notificationVisuals(notification.type)
    val timeFormatter = remember { DateTimeFormatter.ofPattern("dd MMM · HH:mm") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = notification.title ?: "Notifica",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = notification.message ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                notification.createdAt?.let { created ->
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = created.format(timeFormatter),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            IconButton(onClick = onMarkAsRead) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Segna come letta",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun notificationVisuals(type: NotificationType?): Pair<ImageVector, Color> = when (type) {
    NotificationType.PRENOTAZIONE_SUCCESSO -> Icons.Default.CheckCircle to Color(0xFF16A34A)
    NotificationType.NUOVA_PRENOTAZIONE -> Icons.Default.People to MaterialTheme.colorScheme.primary
    NotificationType.APPROVAZIONE_SOCIETA -> Icons.Default.CheckCircle to Color(0xFF16A34A)
    NotificationType.RIFIUTO_SOCIETA -> Icons.Default.Close to Color(0xFFDC2626)
    NotificationType.NUOVO_ANNUNCIO -> Icons.Default.Info to MaterialTheme.colorScheme.primary
    else -> Icons.Outlined.Notifications to MaterialTheme.colorScheme.onSurfaceVariant
}
