package com.travel.app.presentation.home

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.travel.app.data.AppContainer

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

    // Selected images list
    val selectedImages = remember { mutableStateListOf<Uri>() }

    // Image Picker Launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        uris.forEach { uri ->
            if (!selectedImages.contains(uri)) {
                selectedImages.add(uri)
            }
        }
    }

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
        selectedImages.clear()
        errorMessage = null
    }

    fun submitActivity() {
        // Validation
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

                val activityName = if (defaultOrganizer.isNotBlank()) defaultOrganizer else "Attività"

                val activityDto = ActivityDto()
                activityDto.setName(activityName)
                activityDto.setDescription(if (description.isNotBlank()) description else null)
                activityDto.setLocation(location)
                activityDto.setStartTime(startLdt)
                activityDto.setEndTime(endLdt)
                activityDto.setParticipants(participants)
                activityDto.setPrice(BigDecimal.valueOf(priceVal))
                activityDto.setOrganizer(defaultOrganizer)

                val result = AppContainer.activityRepository.createActivity(activityDto)
                result.onSuccess {
                    // visual success dialog, images upload mock
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
            .background(Color(0xFFF8FAFC)) // Match dashboard background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .padding(bottom = 90.dp) // Avoid overlap with bottom nav bar
        ) {
            // TOP HEADER
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Nuova Attività",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF0F172A)
                )
            }
            
            Text(
                text = "Crea e pubblica una nuova attività turistica per i viaggiatori.",
                fontSize = 14.sp,
                color = Color(0xFF64748B), // Slate 500
                modifier = Modifier.padding(bottom = 20.dp)
            )

            // SECTION 1: INFORMAZIONI GENERALI
            Text(
                text = "Informazioni Generali",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            FormCardSection {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ActivityInputField(
                        label = "Descrizione",
                        value = description,
                        onValueChange = { description = it },
                        placeholder = "Fornisci dettagli sull'itinerario e l'attività...",
                        singleLine = false,
                        modifier = Modifier.height(110.dp)
                    )
                    
                    ActivityInputField(
                        label = "Posizione (Città, Luogo) *",
                        value = location,
                        onValueChange = { location = it },
                        placeholder = "Cerca città o luogo...",
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = Color(0xFF64748B)
                            )
                        },
                        shape = RoundedCornerShape(28.dp)
                    )
                }
            }

            // SECTION 2: PERIODO ATTIVITÀ
            Text(
                text = "Periodo Attività",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A),
                modifier = Modifier.padding(top = 20.dp, bottom = 8.dp)
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
                        // Inizio Date Box Selector
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(84.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFFF8FAFC))
                                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
                                .clickable { showStartDatePicker() }
                                .padding(12.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(Color(0xFFEFF6FF), CircleShape), // Soft blue badge
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarToday,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "Inizio",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF64748B)
                                    )
                                    Text(
                                        text = if (startYear > 0) String.format("%02d/%02d/%d alle %02d:%02d", startDay, startMonth, startYear, startHour, startMinute) else "Imposta data e ora",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (startYear > 0) Color(0xFF0F172A) else Color(0xFF94A3B8)
                                    )
                                }
                            }
                        }

                        // Fine Date Box Selector
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(84.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFFF8FAFC))
                                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
                                .clickable { showEndDatePicker() }
                                .padding(12.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(Color(0xFFFEF2F2), CircleShape), // Soft red badge
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarToday,
                                        contentDescription = null,
                                        tint = Color(0xFFEF4444),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "Fine",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF64748B)
                                    )
                                    Text(
                                        text = if (endYear > 0) String.format("%02d/%02d/%d alle %02d:%02d", endDay, endMonth, endYear, endHour, endMinute) else "Imposta data e ora",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (endYear > 0) Color(0xFF0F172A) else Color(0xFF94A3B8)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // SECTION 3: IMMAGINI ATTIVITÀ
            Text(
                text = "Immagini Attività",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A),
                modifier = Modifier.padding(top = 20.dp, bottom = 8.dp)
            )

            FormCardSection {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (selectedImages.isEmpty()) {
                        // Empty State: Sleek full-width upload card
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(90.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFFF8FAFC))
                                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
                                .clickable { imagePickerLauncher.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddPhotoAlternate,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                                Text(
                                    text = "Carica foto dell'attività",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    } else {
                        // Selected State: Compact grid list with a trailing add button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0xFFF8FAFC))
                                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
                                    .clickable { imagePickerLauncher.launch("image/*") },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Aggiungi altra foto",
                                    tint = Color(0xFF64748B),
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            androidx.compose.foundation.lazy.LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                items(selectedImages.size) { index ->
                                    val uri = selectedImages[index]
                                    ImagePreviewCard(
                                        uri = uri,
                                        size = 72.dp,
                                        onRemove = { selectedImages.removeAt(index) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // SECTION 4: DETTAGLI OFFERTA
            Text(
                text = "Dettagli Offerta",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A),
                modifier = Modifier.padding(top = 20.dp, bottom = 8.dp)
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
                                value = maxParticipantsText,
                                onValueChange = { maxParticipantsText = it },
                                placeholder = "es. 15",
                                keyboardType = KeyboardType.Number
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            ActivityInputField(
                                label = "Prezzo (€) *",
                                value = priceText,
                                onValueChange = { priceText = it },
                                placeholder = "es. 49.90",
                                keyboardType = KeyboardType.Decimal
                            )
                        }
                    }
                }
            }

            errorMessage?.let { error ->
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

            Button(
                onClick = { submitActivity() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                enabled = !isLoading
            ) {
                if (isLoading) {
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

@Composable
fun FormCardSection(
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        content = content
    )
}

@Composable
fun ActivityInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    singleLine: Boolean = true,
    keyboardType: KeyboardType = KeyboardType.Text,
    leadingIcon: @Composable (() -> Unit)? = null,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(16.dp)
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = label,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF334155) // Slate 700
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = if (placeholder.isNotEmpty()) { { Text(placeholder, color = Color(0xFF94A3B8)) } } else null,
            modifier = Modifier.fillMaxWidth(),
            shape = shape,
            singleLine = singleLine,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            leadingIcon = leadingIcon,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = Color(0xFFE2E8F0),
                focusedTextColor = Color(0xFF0F172A),
                unfocusedTextColor = Color(0xFF334155)
            )
        )
    }
}

@Composable
fun rememberUriImageBitmap(uri: Uri): ImageBitmap? {
    val context = LocalContext.current
    return remember(uri) {
        try {
            val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(context.contentResolver, uri)
                ImageDecoder.decodeBitmap(source)
            } else {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            }
            bitmap.asImageBitmap()
        } catch (e: Exception) {
            null
        }
    }
}

@Composable
fun ImagePreviewCard(
    uri: Uri,
    size: androidx.compose.ui.unit.Dp = 72.dp,
    onRemove: () -> Unit
) {
    val bitmap = rememberUriImageBitmap(uri)
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap,
                contentDescription = "Anteprima immagine",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier.fillMaxSize().background(Color(0xFFF1F5F9)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            }
        }

        // Remove overlay button
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
                .size(20.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable { onRemove() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Rimuovi foto",
                tint = Color.White,
                modifier = Modifier.size(12.dp)
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun CompanyAddOfferScreenPreview() {
    com.travel.app.presentation.theme.TravelTheme {
        CompanyAddOfferScreen()
    }
}
