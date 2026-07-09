package com.travel.app.presentation.components.checkout

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import it.unical.ea.dtos.activity.ActivityDto
import java.time.format.DateTimeFormatter
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutSummaryScreen(
        bookingId: String,
        title: String,
        totalPrice: Double,
        isItinerary: Boolean,
        activities: List<ActivityDto>,
        userEmail: String,
        isConfirming: Boolean,
        onConfirm: () -> Unit,
        onCancel: () -> Unit,
        isReadOnly: Boolean = false,
        modifier: Modifier = Modifier
) {
    val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm", Locale.ITALIAN)
    val displayBookingId =
            remember(bookingId) {
                if (bookingId.startsWith("DRV-")) {
                    bookingId
                } else {
                    // Tronchiamo l'UUID a 8 caratteri giusto per farlo vedere carino all'utente
                    // (UX).
                    // per evitare qualsiasi rischio di collisione, conviene salvare
                    // direttamente sul database un codice univoco dedicato.
                    val cleanId = bookingId.replace("-", "")
                    val suffix =
                            if (cleanId.length >= 8) cleanId.substring(0, 8).uppercase()
                            else cleanId.uppercase()
                    "DRV-$suffix"
                }
            }

    Scaffold(
            modifier = modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                        title = {
                            Text(
                                    if (isReadOnly) "Ricevuta Prenotazione" else "Riepilogo Ordine",
                                    fontWeight = FontWeight.Bold
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = onCancel) {
                                Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Indietro"
                                )
                            }
                        },
                        colors =
                                TopAppBarDefaults.topAppBarColors(
                                        containerColor =
                                                MaterialTheme.colorScheme.surfaceColorAtElevation(
                                                        3.dp
                                                )
                                )
                )
            },
            bottomBar = {
                Surface(
                        tonalElevation = 8.dp,
                        shadowElevation = 16.dp,
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.navigationBarsPadding().padding(24.dp)) {
                        Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                    text = if (isReadOnly) "Totale Pagato" else "Totale Ordine",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Normal
                            )
                            Text(
                                    text =
                                            if (totalPrice == 0.0) "Gratis"
                                            else
                                                    "€${String.format(Locale.getDefault(), "%.2f", totalPrice)}",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        if (isReadOnly) {
                            Button(
                                    onClick = onCancel,
                                    modifier = Modifier.fillMaxWidth().height(50.dp),
                                    shape = RoundedCornerShape(25.dp)
                            ) {
                                Text(
                                        "Chiudi",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleMedium
                                )
                            }
                        } else {
                            Button(
                                    onClick = onConfirm,
                                    enabled = !isConfirming,
                                    modifier = Modifier.fillMaxWidth().height(50.dp),
                                    shape = RoundedCornerShape(25.dp),
                                    colors =
                                            ButtonDefaults.buttonColors(
                                                    containerColor =
                                                            MaterialTheme.colorScheme.primary
                                            )
                            ) {
                                if (isConfirming) {
                                    CircularProgressIndicator(
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            strokeWidth = 2.dp,
                                            modifier = Modifier.size(24.dp)
                                    )
                                } else {
                                    Text(
                                            text = "Acquista Ora",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleMedium
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(
                                    onClick = onCancel,
                                    enabled = !isConfirming,
                                    modifier = Modifier.fillMaxWidth().height(50.dp),
                                    shape = RoundedCornerShape(25.dp)
                            ) { Text(text = "Annulla Ordine", fontWeight = FontWeight.Medium) }
                        }
                    }
                }
            }
    ) { paddingValues ->
        val scrollState = rememberScrollState()
        Column(
                modifier =
                        Modifier.fillMaxSize()
                                .padding(paddingValues)
                                .background(MaterialTheme.colorScheme.background)
                                .verticalScroll(scrollState)
                                .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Order ID Card
            Card(
                    shape = RoundedCornerShape(12.dp),
                    colors =
                            CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                            ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                            imageVector = Icons.Default.Receipt,
                            contentDescription = "Order ID",
                            tint = MaterialTheme.colorScheme.primary
                    )
                    Column {
                        Text(
                                text = "ID Prenotazione",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                                text = displayBookingId,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Client Info Section
            Card(
                    shape = RoundedCornerShape(12.dp),
                    colors =
                            CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                            ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                            text = "Dettagli dell'utente",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                    )
                    HorizontalDivider()
                    Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Client",
                                tint = MaterialTheme.colorScheme.secondary
                        )
                        Column {
                            Text(
                                    text = "Account collegato",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                    text = userEmail,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                                imageVector = Icons.Default.Payment,
                                contentDescription = "Metodo di pagamento",
                                tint = MaterialTheme.colorScheme.secondary
                        )
                        Column {
                            Text(
                                    text = "Metodo di pagamento",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                    text = "PayPal / Pagamento Sicuro Mock",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // Items Details Section
            Card(
                    shape = RoundedCornerShape(12.dp),
                    colors =
                            CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                            ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                            text =
                                    if (isItinerary) "Articoli inclusi nell'itinerario"
                                    else "Attività selezionata",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                    )
                    Text(
                            text = title,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                    )
                    HorizontalDivider()

                    activities.forEach { activity ->
                        ActivitySummaryItem(activity = activity, formatter = formatter)
                        if (activities.indexOf(activity) < activities.size - 1) {
                            HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 4.dp),
                                    thickness = 0.5.dp
                            )
                        }
                    }
                }
            }

            // Pricing Summary Card
            Card(
                    shape = RoundedCornerShape(12.dp),
                    colors =
                            CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                            ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                                text = if (isReadOnly) "Totale Pagato" else "Totale Ordine",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                        )
                        Text(
                                text =
                                        if (totalPrice == 0.0) "Gratis"
                                        else
                                                "€${String.format(Locale.getDefault(), "%.2f", totalPrice)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ActivitySummaryItem(activity: ActivityDto, formatter: DateTimeFormatter) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        Text(
                text = activity.name ?: "Attività Senza Nome",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
        )
        Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Località",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
            )
            Text(
                    text = activity.location ?: "N/D",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = "Orari",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
            )
            val startTime = activity.startTime?.format(formatter) ?: "N/D"
            Text(
                    text = startTime,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        val price = activity.price?.toDouble() ?: 0.0
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                    text = "Prezzo unitario",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                    text =
                            if (price == 0.0) "Gratis"
                            else "€${String.format(Locale.getDefault(), "%.2f", price)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
            )
        }
    }
}
