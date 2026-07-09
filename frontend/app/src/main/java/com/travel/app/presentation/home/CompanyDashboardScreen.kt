package com.travel.app.presentation.home

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.background
import com.travel.app.presentation.theme.TravelTheme
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.LocationOn
import java.time.format.DateTimeFormatter
import it.unical.ea.dtos.activity.ActivityDto
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.travel.app.presentation.home.components.CompanyKpiCard
import com.travel.app.presentation.home.components.ActivityFillRateBar
import androidx.compose.material.icons.filled.People

// Un'attività multi-giorno è rappresentata da più sessioni (una per data):
// per statistiche e periodo si aggrega sempre sull'intera lista, non sulla sola sessione primaria.
private fun sessionsOf(activity: ActivityDto): List<ActivityDto> =
    activity.sessions?.takeIf { it.isNotEmpty() } ?: listOf(activity)

private fun bookedSeats(activity: ActivityDto): Int =
    sessionsOf(activity).sumOf { it.currentParticipants ?: 0 }

private fun capacitySeats(activity: ActivityDto): Int =
    sessionsOf(activity).sumOf { it.participants ?: 0 }

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CompanyDashboardScreen(
    viewModel: CompanyDashboardViewModel,
    onEditActivityClick: (String) -> Unit,
    onViewBookingsClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val totalActivities = viewModel.activities.size
    val totalBookings = viewModel.activities.sumOf { bookedSeats(it) }
    val availableSeats = viewModel.activities.sumOf {
        (capacitySeats(it) - bookedSeats(it)).coerceAtLeast(0)
    }

    LaunchedEffect(Unit) {
        viewModel.fetchDashboardData()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Dashboard",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF0F172A)
                )
            )
            val orgName = viewModel.currentOrganizerName
            Text(
                text = if (orgName.isNotBlank())
                    "$orgName · panoramica delle tue attività e prenotazioni"
                else
                    "Panoramica delle tue attività e prenotazioni",
                fontSize = 13.sp,
                color = Color(0xFF64748B)
            )
        }

        if (viewModel.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (viewModel.errorMessage != null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.width(8.dp))
                        Text(viewModel.errorMessage ?: "", color = MaterialTheme.colorScheme.onErrorContainer)
                    }
                }
            }
        } else if (viewModel.activities.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Non ci sono ancora attività disponibili.", style = MaterialTheme.typography.bodyMedium)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        DashboardSectionLabel("Panoramica")

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CompanyKpiCard(
                                title = "Attività pubblicate",
                                value = "$totalActivities",
                                icon = Icons.Default.CalendarToday,
                                iconColor = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.weight(1f)
                            )
                            CompanyKpiCard(
                                title = "Prenotazioni totali",
                                value = "$totalBookings",
                                icon = Icons.Default.People,
                                iconColor = Color(0xFF7C3AED),
                                modifier = Modifier.weight(1f)
                            )
                            CompanyKpiCard(
                                title = "Posti disponibili",
                                value = "$availableSeats",
                                icon = Icons.Default.CheckCircle,
                                iconColor = Color(0xFF16A34A),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        DashboardSectionLabel("Prenotazioni per attività")

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                viewModel.activities.forEach { activity ->
                                    ActivityFillRateBar(
                                        name = activity.name ?: "Senza Nome",
                                        occupiedCount = bookedSeats(activity),
                                        capacityCount = capacitySeats(activity)
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    DashboardSectionLabel(
                        text = "Le tue attività",
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                    )
                }
                items(viewModel.activities) { activity ->
                    ActivityDashboardCard(
                        activity = activity,
                        onEditClick = onEditActivityClick,
                        onBookingsClick = onViewBookingsClick
                    )
                }
            }
        }
    }
}

@Composable
private fun DashboardSectionLabel(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text.uppercase(),
        modifier = modifier,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.2.sp,
        color = Color(0xFF94A3B8)
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ActivityDashboardCard(
    activity: ActivityDto,
    onEditClick: (String) -> Unit,
    onBookingsClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val sessions = sessionsOf(activity)
    val current = bookedSeats(activity)
    val capacity = capacitySeats(activity)
    val isFull = capacity > 0 && current >= capacity
    val isAlmostFull = capacity > 0 && !isFull && current >= capacity * 0.8
    val statusText = if (isFull) "Completo" else if (isAlmostFull) "Quasi completo" else "Disponibile"
    val statusBgColor = if (isFull) Color(0xFFFEF2F2) else if (isAlmostFull) Color(0xFFFFF7ED) else Color(0xFFF0FDF4)
    val statusTextColor = if (isFull) Color(0xFFEF4444) else if (isAlmostFull) Color(0xFFF97316) else Color(0xFF16A34A)

    // Il periodo dell'attività copre tutte le sessioni, dalla prima all'ultima data.
    val firstStart = sessions.mapNotNull { it.startTime }.minOrNull()
    val lastEnd = sessions.mapNotNull { it.endTime }.maxOrNull()
    val dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
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

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column {
        // Anteprima immagine dell'attività (se presente)
        val imageUrl = activity.images?.firstOrNull()
        if (imageUrl != null) {
            AsyncImage(
                model = imageUrl,
                contentDescription = "Immagine di ${activity.name ?: "attività"}",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
                contentScale = ContentScale.Crop
            )
        }
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = activity.name ?: "Senza Nome",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A),
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(statusBgColor)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = statusText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusTextColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            if (!activity.description.isNullOrBlank()) {
                Text(
                    text = activity.description ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF64748B),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = activity.location ?: "Nessuna posizione",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF64748B)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = dateText,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF64748B)
                    )
                    if (sessions.size > 1) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFF1F5F9))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "${sessions.size} date",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF475569)
                            )
                        }
                    }
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = Color(0xFFE2E8F0)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Prezzo Offerta",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF94A3B8)
                    )
                    Text(
                        text = "€${activity.price ?: "0.00"}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(
                        onClick = { activity.id?.let { onBookingsClick(it.toString()) } },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(40.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) {
                        Text(
                            text = "Iscritti (${current}/${capacity})",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { activity.id?.let { onEditClick(it.toString()) } },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(40.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(
                            text = "Modifica",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@androidx.compose.ui.tooling.preview.Preview(showBackground = true, showSystemUi = true)
@Composable
fun CompanyDashboardScreenPreview() {
    TravelTheme {
        val mockViewModel = remember {
            CompanyDashboardViewModel(
                activityRepository = object : com.travel.app.domain.repository.ActivityRepository {
                    override suspend fun createActivity(activity: it.unical.ea.dtos.activity.CreateActivityRequestDto) = Result.success(it.unical.ea.dtos.activity.ActivityTemplateDto())
                    override suspend fun getActivities() = Result.success(emptyList<it.unical.ea.dtos.activity.ActivityDto>())
                },
                userRepository = object : com.travel.app.domain.repository.UserRepository {
                    override suspend fun deleteAccount(userId: String) = Result.success(Unit)
                    override suspend fun uploadAvatar(userId: String, imageBytes: ByteArray, mimeType: String, fileName: String) = Result.failure<com.travel.app.domain.model.User>(Exception())
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
        CompanyDashboardScreen(
            viewModel = mockViewModel,
            onEditActivityClick = {},
            onViewBookingsClick = {}
        )
    }
}
