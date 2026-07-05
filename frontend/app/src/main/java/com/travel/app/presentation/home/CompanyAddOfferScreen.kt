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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.travel.app.presentation.components.activity.ActivityInputField
import com.travel.app.presentation.components.activity.FormCardSection
import com.travel.app.presentation.components.activity.ImagePreviewCard
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

    fun showStartDatePicker() {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                TimePickerDialog(
                    context,
                    { _, hourOfDay, minute ->
                        viewModel.startYear = year
                        viewModel.startMonth = month + 1
                        viewModel.startDay = dayOfMonth
                        viewModel.startHour = hourOfDay
                        viewModel.startMinute = minute
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
                        viewModel.endYear = year
                        viewModel.endMonth = month + 1
                        viewModel.endDay = dayOfMonth
                        viewModel.endHour = hourOfDay
                        viewModel.endMinute = minute
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
                    text = if (viewModel.isEditMode) "Modifica Attività" else "Nuova Attività",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF0F172A)
                )
            }
            
            Text(
                text = if (viewModel.isEditMode) "Modifica i dettagli dell'attività selezionata." else "Crea e pubblica una nuova attività.",
                fontSize = 14.sp,
                color = Color(0xFF64748B), // Slate 500
                modifier = Modifier.padding(bottom = 20.dp)
            )

            // SECTION 1: INFORMAZIONI GENERALI
            Text(
                text = "Generali",
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
                        label = "Descrizione *",
                        value = viewModel.description,
                        onValueChange = { viewModel.description = it },
                        placeholder = "Fornisci dettagli sull'itinerario e l'attività...",
                        singleLine = false,
                        modifier = Modifier.height(110.dp)
                    )
                    
                    ActivityInputField(
                        label = "Posizione (Città, Luogo) *",
                        value = viewModel.location,
                        onValueChange = { viewModel.location = it },
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
                                        text = if (viewModel.startYear > 0) String.format("%02d/%02d/%d alle %02d:%02d", viewModel.startDay, viewModel.startMonth, viewModel.startYear, viewModel.startHour, viewModel.startMinute) else "Imposta data e ora",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (viewModel.startYear > 0) Color(0xFF0F172A) else Color(0xFF94A3B8)
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
                                        text = if (viewModel.endYear > 0) String.format("%02d/%02d/%d alle %02d:%02d", viewModel.endDay, viewModel.endMonth, viewModel.endYear, viewModel.endHour, viewModel.endMinute) else "Imposta data e ora",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (viewModel.endYear > 0) Color(0xFF0F172A) else Color(0xFF94A3B8)
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
                    if (viewModel.selectedImages.isEmpty()) {
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
                                items(viewModel.selectedImages.size) { index ->
                                    val uri = viewModel.selectedImages[index]
                                    ImagePreviewCard(
                                        uri = uri,
                                        size = 72.dp,
                                        onRemove = { viewModel.selectedImages.removeAt(index) }
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
                                value = viewModel.maxParticipantsText,
                                onValueChange = { viewModel.maxParticipantsText = it },
                                placeholder = "es. 15",
                                keyboardType = KeyboardType.Number
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            ActivityInputField(
                                label = "Prezzo (€) *",
                                value = viewModel.priceText,
                                onValueChange = { viewModel.priceText = it },
                                placeholder = "es. 49.90",
                                keyboardType = KeyboardType.Decimal
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { viewModel.submitActivity() },
                        modifier = Modifier
                            .weight(1f)
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

                    var showDeleteConfirm by remember { mutableStateOf(false) }
                    Button(
                        onClick = { showDeleteConfirm = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        enabled = !viewModel.isLoading
                    ) {
                        Text(
                            text = "Elimina",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
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
                    onClick = { viewModel.submitActivity() },
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
            title = { Text(if (viewModel.isEditMode) "Offerta Modificata" else "Offerta Pubblicata") },
            text = { Text(if (viewModel.isEditMode) "La tua attività è stata modificata con successo." else "La tua attività è stata pubblicata con successo ed è ora visibile ai viaggiatori.") },
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

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, showSystemUi = true)
@Composable
fun CompanyAddOfferScreenPreview() {
    com.travel.app.presentation.theme.TravelTheme {
        val mockViewModel = remember {
            CompanyAddOfferViewModel(
                activityRepository = object : com.travel.app.domain.repository.ActivityRepository {
                    override suspend fun createActivity(activity: it.unical.ea.dtos.activity.ActivityDto) = Result.success(activity)
                    override suspend fun getActivities() = Result.success(emptyList<it.unical.ea.dtos.activity.ActivityDto>())
                    override suspend fun getActivityById(id: String) = Result.success(it.unical.ea.dtos.activity.ActivityDto())
                    override suspend fun searchActivities(query: String, minPrice: Double?, maxPrice: Double?, page: Int, size: Int) = Result.success(it.unical.ea.dtos.common.PageDto<it.unical.ea.dtos.activity.ActivityDto>())
                },
                userRepository = object : com.travel.app.domain.repository.UserRepository {
                    override fun getSessionUser(): com.travel.app.domain.model.User? = null
                    override suspend fun login(email: String, password: String, captchaToken: String?) = Result.failure<com.travel.app.domain.model.User>(Exception())
                    override suspend fun registerViaggiatoreUser(email: String, firstName: String, lastName: String, password: String, phone: String?, captchaToken: String?) = Result.failure<com.travel.app.domain.model.User>(Exception())
                    override suspend fun registerSocietaUser(email: String, companyName: String, vatNumber: String, password: String, phone: String?, captchaToken: String?, documentPhotos: List<String>) = Result.failure<com.travel.app.domain.model.User>(Exception())
                    override suspend fun getMe() = Result.failure<com.travel.app.domain.model.User>(Exception())
                    override suspend fun updateMe(user: com.travel.app.domain.model.User) = Result.success(user)
                    override fun logout() {}
                    override fun saveSession(user: com.travel.app.domain.model.User, token: String) {}
                    override suspend fun uploadDocument(fileBytes: ByteArray, filename: String) = Result.success("mock")
                    override suspend fun getAllCompanies() = Result.success(emptyList<com.travel.app.domain.model.User>())
                    override suspend fun blockCompany(id: String) = Result.success(Unit)
                    override suspend fun unblockCompany(id: String) = Result.success(Unit)
                }
            )
        }
        CompanyAddOfferScreen(viewModel = mockViewModel)
    }
}
