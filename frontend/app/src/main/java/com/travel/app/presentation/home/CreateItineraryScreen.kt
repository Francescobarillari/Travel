package com.travel.app.presentation.home

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.foundation.border
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.travel.app.data.AppContainer
import it.unical.ea.dtos.location.LocationDto
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import it.unical.ea.dtos.itinerary.ItineraryDto
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateItineraryScreen(
    onNavigateBack: () -> Unit,
    onNext: (ItineraryDto, String, Uri?) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    // Suggestions states for location autocomplete
    var locationSuggestions by remember { mutableStateOf<List<LocationDto>>(emptyList()) }
    var searchJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }
    val scope = rememberCoroutineScope()

    // Date/Time selection states
    var startYear by remember { mutableStateOf(0) }
    var startMonth by remember { mutableStateOf(0) }
    var startDay by remember { mutableStateOf(0) }
    var startHour by remember { mutableStateOf(0) }
    var startMinute by remember { mutableStateOf(0) }

    var endYear by remember { mutableStateOf(0) }
    var endMonth by remember { mutableStateOf(0) }
    var endDay by remember { mutableStateOf(0) }
    var endHour by remember { mutableStateOf(0) }
    var endMinute by remember { mutableStateOf(0) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    val isStartDateSet = startYear > 0
    val isEndDateSet = endYear > 0

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Crea Nuovo Itinerario",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Indietro")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Inserisci i dettagli principali del tuo itinerario di viaggio per iniziare ad aggiungere attività.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            )

            // Cover Image picker UI
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { galleryLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (selectedImageUri != null) {
                    AsyncImage(
                        model = selectedImageUri,
                        contentDescription = "Immagine di copertina",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .padding(8.dp)
                            .align(Alignment.BottomEnd)
                            .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("Cambia", color = Color.White, style = MaterialTheme.typography.bodySmall)
                    }
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = null,
                            modifier = Modifier.size(36.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Seleziona Immagine di Copertina",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Input Fields
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Titolo dell'Itinerario") },
                placeholder = { Text("es: Weekend Romantico a Roma") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            OutlinedTextField(
                value = location,
                onValueChange = {
                    location = it
                    searchJob?.cancel()
                    if (it.trim().length >= 2) {
                        searchJob = scope.launch {
                            delay(500)
                            AppContainer.localitaRepository.searchLocalita(it.trim(), includeExternal = true).fold(
                                onSuccess = { pageDto ->
                                    locationSuggestions = pageDto.content ?: emptyList()
                                },
                                onFailure = {
                                    locationSuggestions = emptyList()
                                }
                            )
                        }
                    } else {
                        locationSuggestions = emptyList()
                    }
                },
                label = { Text("Località / Città principale") },
                placeholder = { Text("Cerca città o luogo...") },
                leadingIcon = { Icon(Icons.Default.LocationOn, null, tint = MaterialTheme.colorScheme.primary) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            if (locationSuggestions.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant,
                            shape = RoundedCornerShape(12.dp)
                        ),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 200.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        locationSuggestions.forEachIndexed { index, suggestion ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        location = suggestion.name ?: ""
                                        locationSuggestions = emptyList()
                                    }
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Place,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = suggestion.name ?: "",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            if (index < locationSuggestions.size - 1) {
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.outlineVariant,
                                    thickness = 1.dp,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                            }
                        }
                    }
                }
            }

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descrizione o Dettagli (opzionale)") },
                placeholder = { Text("es: Esplorazione delle meraviglie storiche e culinarie della capitale...") },
                modifier = Modifier.fillMaxWidth().height(100.dp),
                shape = RoundedCornerShape(12.dp),
                maxLines = 4
            )

            // Start Date & Time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Start Date
                Box(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .weight(1f)
                        .clickable {
                            val initYear = if (startYear > 0) startYear else calendar.get(Calendar.YEAR)
                            val initMonth = if (startYear > 0) startMonth - 1 else calendar.get(Calendar.MONTH)
                            val initDay = if (startYear > 0) startDay else calendar.get(Calendar.DAY_OF_MONTH)
                            DatePickerDialog(context, { _, year, month, day ->
                                startYear = year
                                startMonth = month + 1
                                startDay = day
                            }, initYear, initMonth, initDay).show()
                        }
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.CalendarToday, null, tint = MaterialTheme.colorScheme.primary)
                        Column {
                            Text("Data Inizio", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = if (isStartDateSet) String.format("%02d/%02d/%d", startDay, startMonth, startYear) else "Scegli data",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }

                // Start Time
                Box(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .weight(1f)
                        .clickable {
                            val initHour = if (startYear > 0) startHour else 9
                            val initMinute = if (startYear > 0) startMinute else 0
                            TimePickerDialog(context, { _, hour, minute ->
                                startHour = hour
                                startMinute = minute
                            }, initHour, initMinute, true).show()
                        }
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.AccessTime, null, tint = MaterialTheme.colorScheme.primary)
                        Column {
                            Text("Ora Inizio", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = if (isStartDateSet) String.format("%02d:%02d", startHour, startMinute) else "--:--",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }

            // End Date & Time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // End Date
                Box(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .weight(1f)
                        .clickable {
                            val initYear = if (endYear > 0) endYear else if (startYear > 0) startYear else calendar.get(Calendar.YEAR)
                            val initMonth = if (endYear > 0) endMonth - 1 else if (startYear > 0) startMonth - 1 else calendar.get(Calendar.MONTH)
                            val initDay = if (endYear > 0) endDay else if (startYear > 0) startDay else calendar.get(Calendar.DAY_OF_MONTH)
                            DatePickerDialog(context, { _, year, month, day ->
                                endYear = year
                                endMonth = month + 1
                                endDay = day
                            }, initYear, initMonth, initDay).show()
                        }
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.CalendarToday, null, tint = Color(0xFFEF4444))
                        Column {
                            Text("Data Fine", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = if (isEndDateSet) String.format("%02d/%02d/%d", endDay, endMonth, endYear) else "Scegli data",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }

                // End Time
                Box(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .weight(1f)
                        .clickable {
                            val initHour = if (endYear > 0) endHour else 18
                            val initMinute = if (endYear > 0) endMinute else 0
                            TimePickerDialog(context, { _, hour, minute ->
                                endHour = hour
                                endMinute = minute
                            }, initHour, initMinute, true).show()
                        }
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.AccessTime, null, tint = Color(0xFFEF4444))
                        Column {
                            Text("Ora Fine", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = if (isEndDateSet) String.format("%02d:%02d", endHour, endMinute) else "--:--",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Button
            Button(
                onClick = {
                    if (title.isBlank()) {
                        Toast.makeText(context, "Il titolo è obbligatorio", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (location.isBlank()) {
                        Toast.makeText(context, "La località è obbligatoria", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (!isStartDateSet || !isEndDateSet) {
                        Toast.makeText(context, "Imposta le date di inizio e fine", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    val startLdt = LocalDateTime.of(startYear, startMonth, startDay, startHour, startMinute)
                    val endLdt = LocalDateTime.of(endYear, endMonth, endDay, endHour, endMinute)

                    if (endLdt.isBefore(startLdt)) {
                        Toast.makeText(context, "La data di fine deve seguire quella di inizio", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    val tempItinerary = ItineraryDto()
                    tempItinerary.title = title
                    tempItinerary.description = description.takeIf { it.isNotBlank() }
                    tempItinerary.startDateTime = startLdt
                    tempItinerary.endDateTime = endLdt

                    onNext(tempItinerary, location.trim(), selectedImageUri)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Avanti",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
