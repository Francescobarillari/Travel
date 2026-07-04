package com.travel.app.presentation.home

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.travel.app.presentation.components.itinerary.ItineraryCard
import com.travel.app.presentation.theme.TravelTheme
import it.unical.ea.dtos.itinerary.ItineraryDto
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.LocationOn
import java.time.format.DateTimeFormatter
import it.unical.ea.dtos.activity.ActivityDto
import androidx.compose.ui.graphics.Color

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CompanyDashboardScreen(
    viewModel: CompanyDashboardViewModel,
    onEditActivityClick: (String) -> Unit,
    onViewBookingsClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    onItineraryClick: (ItineraryDto) -> Unit = {}
) {
    LaunchedEffect(Unit) {
        viewModel.fetchDashboardData()
    }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        Text(
            text = "Dashboard Società",
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp),
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        )

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
        } else if (viewModel.itineraries.isEmpty() && viewModel.activities.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Non ci sono ancora itinerari o attività disponibili.", style = MaterialTheme.typography.bodyMedium)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                if (viewModel.itineraries.isNotEmpty()) {
                    item {
                        Text(
                            text = "Itinerari Disponibili",
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    items(viewModel.itineraries) { itinerary ->
                        ItineraryCard(
                            itinerary = itinerary,
                            onClick = { onItineraryClick(itinerary) },
                            actions = {
                                IconButton(onClick = { /* TODO */ }) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Modifica",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        )
                    }
                }

                if (viewModel.activities.isNotEmpty()) {
                    item {
                        Text(
                            text = "Le Tue Attività",
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
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
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ActivityDashboardCard(
    activity: ActivityDto,
    onEditClick: (String) -> Unit,
    onBookingsClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = activity.name ?: "Senza Nome",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (!activity.description.isNullOrBlank()) {
                Text(
                    text = activity.description ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = activity.location ?: "Nessuna posizione",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(16.dp)
                )
                val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")
                val start = activity.startTime?.format(formatter) ?: ""
                val end = activity.endTime?.format(formatter) ?: ""
                Text(
                    text = "$start - $end",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Prezzo: €${activity.price ?: "0.00"}",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Iscritti: ${activity.currentParticipants ?: 0}/${activity.participants ?: 0}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = { activity.id?.let { onBookingsClick(it.toString()) } }) {
                    Text("Iscritti")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { activity.id?.let { onEditClick(it.toString()) } },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Modifica", color = Color.White)
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
                itineraryRepository = object : com.travel.app.domain.repository.ItineraryRepository {
                    override suspend fun getItineraries() = Result.success(emptyList<ItineraryDto>())
                },
                activityRepository = object : com.travel.app.domain.repository.ActivityRepository {
                    override suspend fun createActivity(activity: it.unical.ea.dtos.activity.ActivityDto) = Result.success(activity)
                    override suspend fun getActivities() = Result.success(emptyList<it.unical.ea.dtos.activity.ActivityDto>())
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
        CompanyDashboardScreen(
            viewModel = mockViewModel,
            onEditActivityClick = {},
            onViewBookingsClick = {}
        )
    }
}
