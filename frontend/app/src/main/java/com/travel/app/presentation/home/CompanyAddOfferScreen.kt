package com.travel.app.presentation.home

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.travel.app.data.AppContainer
import com.travel.app.presentation.components.auth.TravelTextField
import it.unical.ea.dtos.activity.ActivityDto
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompanyAddOfferScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    val currentUser = remember { 
        try {
            AppContainer.userRepository.getSessionUser()
        } catch (e: Exception) {
            null
        }
    }
    val defaultOrganizer = currentUser?.name ?: ""

    // Form fields state
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    
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

    var maxParticipantsText by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }

    // UI Status state
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    val calendar = Calendar.getInstance()

    fun showStartDatePicker() {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                TimePickerDialog(
                    context,
                    { _, hourOfDay, minute ->
                        startYear = year
                        startMonth = month + 1
                        startDay = dayOfMonth
                        startHour = hourOfDay
                        startMinute = minute
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true
                ).show()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    fun showEndDatePicker() {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                TimePickerDialog(
                    context,
                    { _, hourOfDay, minute ->
                        endYear = year
                        endMonth = month + 1
                        endDay = dayOfMonth
                        endHour = hourOfDay
                        endMinute = minute
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true
                ).show()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    fun resetForm() {
        name = ""
        description = ""
        location = ""
        startYear = 0
        startMonth = 0
        startDay = 0
        startHour = 0
        startMinute = 0
        endYear = 0
        endMonth = 0
        endDay = 0
        endHour = 0
        endMinute = 0
        maxParticipantsText = ""
        priceText = ""
        errorMessage = null
    }

    fun submitActivity() {
        // Validation
        if (name.isBlank()) {
            errorMessage = "Il nome dell'attività è obbligatorio"
            return
        }
        if (location.isBlank()) {
            errorMessage = "La posizione dell'attività è obbligatoria"
            return
        }
        if (startYear == 0) {
            errorMessage = "La data di inizio è obbligatoria"
            return
        }
        if (endYear == 0) {
            errorMessage = "La data di fine è obbligatoria"
            return
        }

        // Compare dates
        val startCal = Calendar.getInstance().apply {
            set(startYear, startMonth - 1, startDay, startHour, startMinute)
        }
        val endCal = Calendar.getInstance().apply {
            set(endYear, endMonth - 1, endDay, endHour, endMinute)
        }
        if (startCal.after(endCal)) {
            errorMessage = "La data di inizio deve essere precedente alla data di fine"
            return
        }

        val participants = maxParticipantsText.toIntOrNull()
        if (participants == null || participants < 1) {
            errorMessage = "Il numero massimo di partecipanti deve essere almeno 1"
            return
        }

        val priceVal = priceText.toDoubleOrNull()
        if (priceVal == null || priceVal < 0.0) {
            errorMessage = "Il prezzo non può essere negativo o vuoto"
            return
        }

        isLoading = true
        errorMessage = null

        coroutineScope.launch {
            try {
                // Create LocalDateTime objects for DTO
                val startLdt = LocalDateTime.of(startYear, startMonth, startDay, startHour, startMinute)
                val endLdt = LocalDateTime.of(endYear, endMonth, endDay, endHour, endMinute)

                val activityDto = ActivityDto()
                activityDto.setName(name)
                activityDto.setDescription(if (description.isNotBlank()) description else null)
                activityDto.setLocation(location)
                activityDto.setStartTime(startLdt)
                activityDto.setEndTime(endLdt)
                activityDto.setParticipants(participants)
                activityDto.setPrice(BigDecimal.valueOf(priceVal))
                activityDto.setOrganizer(defaultOrganizer)

                val result = AppContainer.activityRepository.createActivity(activityDto)
                result.onSuccess {
                    showSuccessDialog = true
                    resetForm()
                }.onFailure { e ->
                    errorMessage = e.message ?: "Si è verificato un errore durante la pubblicazione"
                }
            } catch (e: Exception) {
                errorMessage = e.message ?: "Errore imprevisto"
            } finally {
                isLoading = false
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(24.dp)
                .padding(bottom = 80.dp) // Avoid overlap with bottom nav bar
        ) {
            Text(
                text = "Nuova Offerta",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.padding(bottom = 4.dp)
            )
            
            Text(
                text = "Crea e pubblica una nuova attività turistica.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(bottom = 20.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    errorMessage?.let { error ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Error,
                                    contentDescription = "Errore",
                                    tint = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = error,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }

                    // Sezione 1: Informazioni Generali
                    Text(
                        text = "Informazioni Attività",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    TravelTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = "Nome Attività *",
                        leadingIcon = Icons.Default.Title
                    )

                    TravelTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = "Descrizione",
                        leadingIcon = Icons.Default.Description,
                        singleLine = false,
                        modifier = Modifier.height(100.dp)
                    )

                    TravelTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = "Posizione (es. Roma, Colosseo) *",
                        leadingIcon = Icons.Default.LocationOn
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    // Sezione 2: Periodo Attività (Inizio e Fine integrati)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Periodo Attività *",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Inizio Box
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.surface)
                                        .clickable { showStartDatePicker() }
                                        .padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarToday,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Inizio",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = if (startYear > 0) String.format("%02d/%02d\n%02d:%02d", startDay, startMonth, startHour, startMinute) else "Imposta",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (startYear > 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                        textAlign = TextAlign.Center
                                    )
                                }

                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )

                                // Fine Box
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.surface)
                                        .clickable { showEndDatePicker() }
                                        .padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarToday,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Fine",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = if (endYear > 0) String.format("%02d/%02d\n%02d:%02d", endDay, endMonth, endHour, endMinute) else "Imposta",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (endYear > 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    // Sezione 3: Prezzo e Partecipanti
                    Text(
                        text = "Dettagli Offerta",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            TravelTextField(
                                value = maxParticipantsText,
                                onValueChange = { maxParticipantsText = it },
                                label = "Max Partecipanti *",
                                leadingIcon = Icons.Default.Group,
                                keyboardType = KeyboardType.Number
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            TravelTextField(
                                value = priceText,
                                onValueChange = { priceText = it },
                                label = "Prezzo (€) *",
                                leadingIcon = Icons.Default.EuroSymbol,
                                keyboardType = KeyboardType.Decimal
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { submitActivity() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Pubblica Offerta",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            title = { Text("Offerta Pubblicata") },
            text = { Text("La tua attività è stata pubblicata con successo ed è ora visibile ai viaggiatori.") },
            confirmButton = {
                Button(
                    onClick = { showSuccessDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("OK", color = Color.White)
                }
            }
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun CompanyAddOfferScreenPreview() {
    com.travel.app.presentation.theme.TravelTheme {
        CompanyAddOfferScreen()
    }
}
