package com.travel.app.presentation.home

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.travel.app.presentation.components.activity.ActivityInputField
import com.travel.app.presentation.components.activity.FormCardSection
import com.travel.app.presentation.components.activity.ImagePreviewCard
import com.travel.app.presentation.components.activity.SectionHeader
import com.travel.app.presentation.components.activity.ActivityDatePickerField
import com.travel.app.presentation.components.activity.ActivityImageSelector
import com.travel.app.presentation.components.activity.showDateTimePicker
import com.travel.app.presentation.components.activity.TagSelectionSection
import androidx.compose.foundation.horizontalScroll
import java.util.Calendar



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompanyAddOfferScreen(
    viewModel: CompanyAddOfferViewModel,
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val calendar = Calendar.getInstance()
    val focusManager = LocalFocusManager.current

    var startYearState by remember { mutableStateOf(viewModel.startYear) }
    var startMonthState by remember { mutableStateOf(viewModel.startMonth) }
    var startDayState by remember { mutableStateOf(viewModel.startDay) }
    var endYearState by remember { mutableStateOf(viewModel.endYear) }
    var endMonthState by remember { mutableStateOf(viewModel.endMonth) }
    var endDayState by remember { mutableStateOf(viewModel.endDay) }
    var startHourState by remember { mutableStateOf(viewModel.startHour) }
    var startMinuteState by remember { mutableStateOf(viewModel.startMinute) }
    var endHourState by remember { mutableStateOf(viewModel.endHour) }
    var endMinuteState by remember { mutableStateOf(viewModel.endMinute) }

    LaunchedEffect(viewModel.startYear, viewModel.startMonth, viewModel.startDay) {
        startYearState = viewModel.startYear
        startMonthState = viewModel.startMonth
        startDayState = viewModel.startDay
    }

    LaunchedEffect(viewModel.endYear, viewModel.endMonth, viewModel.endDay) {
        endYearState = viewModel.endYear
        endMonthState = viewModel.endMonth
        endDayState = viewModel.endDay
    }

    LaunchedEffect(viewModel.startHour, viewModel.startMinute) {
        startHourState = viewModel.startHour
        startMinuteState = viewModel.startMinute
    }

    LaunchedEffect(viewModel.endHour, viewModel.endMinute) {
        endHourState = viewModel.endHour
        endMinuteState = viewModel.endMinute
    }

    LaunchedEffect(scrollState.isScrollInProgress) {
        if (scrollState.isScrollInProgress) {
            focusManager.clearFocus()
        }
    }

    // Image Picker Launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        uris.forEach { uri ->
            if (!viewModel.selectedImages.contains(uri)) {
                viewModel.selectedImages.add(uri)
            }
        }
    }



    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC)) // Match dashboard background
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                focusManager.clearFocus()
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .padding(bottom = 90.dp) // Avoid overlap with bottom nav bar
        ) {
            // TOP HEADER
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = if (viewModel.isEditMode) "Modifica Attività" else "Nuova Attività",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF0F172A)
                )
                Text(
                    text = if (viewModel.isEditMode) "Modifica i dettagli dell'attività selezionata." else "Crea e pubblica una nuova attività.",
                    fontSize = 14.sp,
                    color = Color(0xFF64748B)
                )
            }

            // SECTION 1: INFORMAZIONI GENERALI
            SectionHeader(
                title = "Informazioni Generali",
                icon = Icons.Default.Description,
                iconColor = Color(0xFF2563EB),
                badgeBgColor = Color(0xFFDBEAFE)
            )
            
            FormCardSection {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ActivityInputField(
                        label = "Titolo attività *",
                        value = viewModel.title,
                        onValueChange = { viewModel.title = it },
                        placeholder = "Es. Tour guidato del Colosseo...",
                        singleLine = true,
                        enabled = !viewModel.isEditMode,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                tint = Color(0xFF64748B)
                            )
                        }
                    )

                    ActivityInputField(
                        label = "Descrizione *",
                        value = viewModel.description,
                        onValueChange = { viewModel.description = it },
                        placeholder = "Fornisci dettagli sull'itinerario e l'attività...",
                        singleLine = false,
                        modifier = Modifier.height(110.dp),
                        enabled = !viewModel.isEditMode,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                tint = Color(0xFF64748B)
                            )
                        }
                    )
                    
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        ActivityInputField(
                            label = "Posizione (Città, Luogo) *",
                            value = viewModel.location,
                            onValueChange = {
                                viewModel.location = it
                                viewModel.fetchLocationSuggestions(it)
                            },
                            placeholder = "Cerca città o luogo...",
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = Color(0xFF64748B)
                                )
                            },
                            shape = RoundedCornerShape(28.dp),
                            enabled = !viewModel.isEditMode
                        )

                        if (viewModel.locationSuggestions.isNotEmpty()) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(
                                        width = 1.dp,
                                        color = Color(0xFFE2E8F0),
                                        shape = RoundedCornerShape(16.dp)
                                    ),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 200.dp)
                                        .verticalScroll(rememberScrollState())
                                ) {
                                    viewModel.locationSuggestions.forEachIndexed { index, suggestion ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    viewModel.location = suggestion.name ?: ""
                                                    viewModel.locationSuggestions.clear()
                                                    focusManager.clearFocus()
                                                }
                                                .padding(horizontal = 16.dp, vertical = 12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Place,
                                                contentDescription = null,
                                                tint = Color(0xFF3B82F6),
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Text(
                                                text = suggestion.name ?: "",
                                                fontSize = 14.sp,
                                                color = Color(0xFF1E293B),
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                        if (index < viewModel.locationSuggestions.size - 1) {
                                            HorizontalDivider(
                                                color = Color(0xFFF1F5F9),
                                                thickness = 1.dp,
                                                modifier = Modifier.padding(horizontal = 16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // SECTION 2: PERIODO ATTIVITÀ
            SectionHeader(
                title = "Periodo dell'Attività",
                icon = Icons.Default.CalendarToday,
                iconColor = Color(0xFFEA580C),
                badgeBgColor = Color(0xFFFFEDD5)
            )

            FormCardSection {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (viewModel.isEditMode) {
                            ActivityDatePickerField(
                                label = "Data e Ora Inizio",
                                dateText = if (startYearState > 0) String.format("%02d/%02d/%d alle %02d:%02d", startDayState, startMonthState, startYearState, startHourState, startMinuteState) else "",
                                isSet = startYearState > 0,
                                badgeColor = Color(0xFFEFF6FF),
                                iconColor = MaterialTheme.colorScheme.primary,
                                onClick = {
                                    if (startYearState > 0) {
                                        calendar.set(Calendar.YEAR, startYearState)
                                        calendar.set(Calendar.MONTH, startMonthState - 1)
                                        calendar.set(Calendar.DAY_OF_MONTH, startDayState)
                                        calendar.set(Calendar.HOUR_OF_DAY, startHourState)
                                        calendar.set(Calendar.MINUTE, startMinuteState)
                                    } else {
                                        val now = Calendar.getInstance()
                                        calendar.time = now.time
                                    }
                                    showDateTimePicker(context, calendar) { year, month, day, hour, minute ->
                                        viewModel.startYear = year
                                        viewModel.startMonth = month
                                        viewModel.startDay = day
                                        viewModel.startHour = hour
                                        viewModel.startMinute = minute
                                        startYearState = year
                                        startMonthState = month
                                        startDayState = day
                                        startHourState = hour
                                        startMinuteState = minute
                                    }
                                }
                            )

                            ActivityDatePickerField(
                                label = "Data e Ora Fine",
                                dateText = if (endYearState > 0) String.format("%02d/%02d/%d alle %02d:%02d", endDayState, endMonthState, endYearState, endHourState, endMinuteState) else "",
                                isSet = endYearState > 0,
                                badgeColor = Color(0xFFFEF2F2),
                                iconColor = Color(0xFFEF4444),
                                onClick = {
                                    if (endYearState > 0) {
                                        calendar.set(Calendar.YEAR, endYearState)
                                        calendar.set(Calendar.MONTH, endMonthState - 1)
                                        calendar.set(Calendar.DAY_OF_MONTH, endDayState)
                                        calendar.set(Calendar.HOUR_OF_DAY, endHourState)
                                        calendar.set(Calendar.MINUTE, endMinuteState)
                                    } else {
                                        val now = Calendar.getInstance()
                                        calendar.time = now.time
                                    }
                                    showDateTimePicker(context, calendar) { year, month, day, hour, minute ->
                                        viewModel.endYear = year
                                        viewModel.endMonth = month
                                        viewModel.endDay = day
                                        viewModel.endHour = hour
                                        viewModel.endMinute = minute
                                        endYearState = year
                                        endMonthState = month
                                        endDayState = day
                                        endHourState = hour
                                        endMinuteState = minute
                                    }
                                }
                            )
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(modifier = Modifier.weight(1f)) {
                                    ActivityDatePickerField(
                                        label = "Inizio Periodo",
                                        dateText = if (startYearState > 0) String.format("%02d/%02d/%d", startDayState, startMonthState, startYearState) else "",
                                        isSet = startYearState > 0,
                                        badgeColor = Color(0xFFEFF6FF),
                                        iconColor = MaterialTheme.colorScheme.primary,
                                        onClick = {
                                            val initYear = if (startYearState > 0) startYearState else calendar.get(Calendar.YEAR)
                                            val initMonth = if (startYearState > 0) startMonthState - 1 else calendar.get(Calendar.MONTH)
                                            val initDay = if (startYearState > 0) startDayState else calendar.get(Calendar.DAY_OF_MONTH)
                                            DatePickerDialog(context, { _, year, month, day ->
                                                viewModel.startYear = year
                                                viewModel.startMonth = month + 1
                                                viewModel.startDay = day
                                                startYearState = year
                                                startMonthState = month + 1
                                                startDayState = day
                                            }, initYear, initMonth, initDay).show()
                                        }
                                    )
                                }
                                Box(modifier = Modifier.weight(1f)) {
                                    ActivityDatePickerField(
                                        label = "Fine Periodo",
                                        dateText = if (endYearState > 0) String.format("%02d/%02d/%d", endDayState, endMonthState, endYearState) else "",
                                        isSet = endYearState > 0,
                                        badgeColor = Color(0xFFFEF2F2),
                                        iconColor = Color(0xFFEF4444),
                                        onClick = {
                                            val initYear = if (endYearState > 0) endYearState else calendar.get(Calendar.YEAR)
                                            val initMonth = if (endYearState > 0) endMonthState - 1 else calendar.get(Calendar.MONTH)
                                            val initDay = if (endYearState > 0) endDayState else calendar.get(Calendar.DAY_OF_MONTH)
                                            DatePickerDialog(context, { _, year, month, day ->
                                                viewModel.endYear = year
                                                viewModel.endMonth = month + 1
                                                viewModel.endDay = day
                                                endYearState = year
                                                endMonthState = month + 1
                                                endDayState = day
                                            }, initYear, initMonth, initDay).show()
                                        }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                "Giorni della settimana *",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B)
                            )
                            val daysList = listOf(
                                "MONDAY" to "Lun",
                                "TUESDAY" to "Mar",
                                "WEDNESDAY" to "Mer",
                                "THURSDAY" to "Gio",
                                "FRIDAY" to "Ven",
                                "SATURDAY" to "Sab",
                                "SUNDAY" to "Dom"
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState())
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                daysList.forEach { (dayKey, dayLabel) ->
                                    val isSelected = viewModel.selectedDaysOfWeek.contains(dayKey)
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { viewModel.toggleDayOfWeek(dayKey) },
                                        label = { Text(dayLabel, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                                            selectedLabelColor = Color.White
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Fasce Orarie *",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E293B)
                                )
                                TextButton(
                                    onClick = {
                                        TimePickerDialog(context, { _, sh, sm ->
                                            TimePickerDialog(context, { _, eh, em ->
                                                viewModel.addTimeSlot(sh, sm, eh, em)
                                            }, sh + 2, sm, true).show()
                                        }, 14, 0, true).show()
                                    }
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Aggiungi Orario")
                                }
                            }

                            viewModel.timeSlots.forEachIndexed { index, slot ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "${slot.startTime} - ${slot.endTime}",
                                            fontWeight = FontWeight.Medium,
                                            color = Color(0xFF334155),
                                            fontSize = 14.sp
                                        )
                                        IconButton(
                                            onClick = { viewModel.removeTimeSlot(index) },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Rimuovi",
                                                tint = Color(0xFFEF4444)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // SECTION 3: IMMAGINI ATTIVITÀ
            SectionHeader(
                title = "Immagini dell'Attività",
                icon = Icons.Default.AddPhotoAlternate,
                iconColor = Color(0xFF7C3AED),
                badgeBgColor = Color(0xFFEDE9FE)
            )

            FormCardSection {
                ActivityImageSelector(
                    selectedImages = viewModel.selectedImages,
                    isEditMode = viewModel.isEditMode,
                    onAddImageClick = { imagePickerLauncher.launch("image/*") },
                    onRemoveImageClick = { index -> viewModel.selectedImages.removeAt(index) },
                    modifier = Modifier.padding(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            SectionHeader(
                title = "Seleziona Categorie / Tag",
                icon = Icons.Default.Sell,
                iconColor = Color(0xFF8B5CF6),
                badgeBgColor = Color(0xFFF3E8FF)
            )

            FormCardSection {
                TagSelectionSection(
                    selectedTags = viewModel.selectedTags.toSet(),
                    onTagToggle = { viewModel.toggleTag(it) },
                    modifier = Modifier.padding(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // SECTION 4: DETTAGLI OFFERTA
            SectionHeader(
                title = "Dettagli dell'Offerta",
                icon = Icons.Default.LocalOffer,
                iconColor = Color(0xFF16A34A),
                badgeBgColor = Color(0xFFDCFCE7)
            )

            FormCardSection {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            ActivityInputField(
                                label = "Max Partecipanti *",
                                value = viewModel.maxParticipantsText,
                                onValueChange = { viewModel.maxParticipantsText = it },
                                placeholder = "es. 15",
                                keyboardType = KeyboardType.Number,
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.People,
                                        contentDescription = null,
                                        tint = Color(0xFF64748B)
                                    )
                                }
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            ActivityInputField(
                                label = "Prezzo (€) *",
                                value = viewModel.priceText,
                                onValueChange = { viewModel.priceText = it },
                                placeholder = "es. 49.90",
                                keyboardType = KeyboardType.Decimal,
                                leadingIcon = {
                                    Text(
                                        text = "€",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF64748B)
                                    )
                                }
                            )
                        }
                    }
                }
            }

            viewModel.errorMessage?.let { error ->
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
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
                            tint = Color(0xFFDC2626)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = error,
                            color = Color(0xFFDC2626),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (viewModel.isEditMode) {
                var showSaveConfirm by remember { mutableStateOf(false) }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { showSaveConfirm = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        enabled = !viewModel.isLoading
                    ) {
                        if (viewModel.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Salva Modifiche",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    if (showSaveConfirm) {
                        AlertDialog(
                            onDismissRequest = { showSaveConfirm = false },
                            title = { Text("Conferma Modifica") },
                            text = { Text("Sei sicuro di voler salvare le modifiche apportate a questa attività?") },
                            confirmButton = {
                                Button(
                                    onClick = {
                                        showSaveConfirm = false
                                        viewModel.submitActivity(context)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Text("Conferma", color = Color.White)
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showSaveConfirm = false }) {
                                    Text("Annulla")
                                }
                            }
                        )
                    }

                    var showDeleteConfirm by remember { mutableStateOf(false) }
                    OutlinedButton(
                        onClick = { showDeleteConfirm = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                        enabled = !viewModel.isLoading
                    ) {
                        Text(
                            text = "Elimina Attività",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (showDeleteConfirm) {
                        AlertDialog(
                            onDismissRequest = { showDeleteConfirm = false },
                            title = { Text("Elimina Attività") },
                            text = { Text("Sei sicuro di voler eliminare questa attività? Questa azione non può essere annullata.") },
                            confirmButton = {
                                Button(
                                    onClick = {
                                        showDeleteConfirm = false
                                        viewModel.deleteActivity(onSuccess = {
                                            onNavigateBack()
                                        })
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                ) {
                                    Text("Elimina", color = Color.White)
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showDeleteConfirm = false }) {
                                    Text("Annulla")
                                }
                            }
                        )
                    }
                }
            } else {
                Button(
                    onClick = { viewModel.submitActivity(context) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    enabled = !viewModel.isLoading
                ) {
                    if (viewModel.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Pubblica Offerta",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }

    if (viewModel.showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { 
                viewModel.showSuccessDialog = false
                onNavigateBack()
            },
            title = { Text(if (viewModel.isEditMode) "Offerta Modificata" else "In Attesa di Approvazione") },
            text = { Text(if (viewModel.isEditMode) "La tua attività è stata modificata con successo." else "La tua attività è stata creata con successo. Sarà pubblicata e visibile ai viaggiatori non appena un amministratore l'avrà approvata.") },
            confirmButton = {
                Button(
                    onClick = { 
                        viewModel.showSuccessDialog = false
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("OK", color = Color.White)
                }
            }
        )
    }

}


