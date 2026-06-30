package com.travel.app.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.travel.app.domain.model.User
import com.travel.app.data.AppContainer
import com.travel.app.presentation.menu.MenuScreen
import com.travel.app.presentation.profile.ProfileScreen
import com.travel.app.presentation.components.home.FloatingBottomNavBar
import com.travel.app.presentation.theme.TravelTheme
import it.unical.ea.dtos.activity.ActivityDto

@Composable
fun HomeScreen(user: User? = null, onLogout: () -> Unit = {}) {
    var selectedTab by remember { mutableStateOf(HomeTab.ESPLORA) }
    var currentUser by remember(user) { 
        mutableStateOf(user ?: User(
            email = "johnkinggraphics@gmail.com", 
            username = "johnkinggraphics", 
            userType = "VIAGGIATORE",
            phone = "6895312",
            name = "Charlotte king",
            password = "password123"
        )) 
    }

    val isSocieta = currentUser.userType == "SOCIETA"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {

        when (selectedTab) {
            HomeTab.ESPLORA -> {
                if (isSocieta) {
                    CompanyDashboardScreen()
                } else {
                    EsploraScreen()
                }
            }
            HomeTab.PREFERITI -> {
                if (isSocieta) {
                    CompanyAddOfferScreen()
                } else {
                    SimplePlaceholderScreen(title = "Preferiti")
                }
            }
            HomeTab.PROFILO -> {
                val userRepository = AppContainer.userRepository
                val isMockUser = currentUser.email in listOf("test@travel.com", "societa@travel.com", "johnkinggraphics@gmail.com")
                ProfileScreen(
                    user = currentUser,
                    onBack = { selectedTab = HomeTab.MENU },
                    onSave = { updatedUser ->
                        if (isMockUser) {
                            currentUser = updatedUser
                            selectedTab = HomeTab.MENU
                            Result.success(updatedUser)
                        } else {
                            val result = userRepository.updateMe(updatedUser)
                            result.onSuccess { savedUser ->
                                currentUser = savedUser
                                selectedTab = HomeTab.MENU
                            }
                            result
                        }
                    }
                )
            }
            HomeTab.MENU -> MenuScreen(
                user = currentUser,
                onBack = { selectedTab = HomeTab.ESPLORA },
                onNavigateToProfile = { selectedTab = HomeTab.PROFILO },
                onLogout = onLogout
            )
        }


        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            FloatingBottomNavBar(
                selectedTab = selectedTab,
                isSocieta = isSocieta,
                onTabSelected = { selectedTab = it }
            )
        }
    }
}

@Composable
fun EsploraScreen() {
    var searchQuery by remember { mutableStateOf("") }
    var activities by remember { mutableStateOf<List<ActivityDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val scrollState = rememberScrollState()

    // Fetch activities from backend
    LaunchedEffect(Unit) {
        isLoading = true
        errorMessage = null
        try {
            val result = AppContainer.activityRepository.getActivities()
            result.onSuccess { list ->
                activities = list
            }.onFailure { e ->
                errorMessage = e.message ?: "Impossibile caricare le attività"
            }
        } catch (e: Exception) {
            errorMessage = e.message ?: "Errore imprevisto"
        } finally {
            isLoading = false
        }
    }

    // Filter activities in real-time
    val filteredActivities = remember(activities, searchQuery) {
        activities.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
            (it.description?.contains(searchQuery, ignoreCase = true) == true) ||
            it.location.contains(searchQuery, ignoreCase = true) ||
            (it.organizer?.contains(searchQuery, ignoreCase = true) == true)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(top = 16.dp)
    ) {

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Text(
                text = "Esplora Attività",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            )
        }

        // Modern, premium SearchBar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            placeholder = { 
                Text(
                    text = "Cerca per nome, luogo, descrizione...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                ) 
            },
            leadingIcon = { 
                Icon(
                    imageVector = Icons.Default.Search, 
                    contentDescription = "Cerca", 
                    tint = MaterialTheme.colorScheme.primary 
                ) 
            },
            singleLine = true,
            shape = RoundedCornerShape(32.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                focusedLabelColor = MaterialTheme.colorScheme.primary
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // State displays
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(40.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (errorMessage != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = "Errore",
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = errorMessage ?: "",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else if (filteredActivities.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 80.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (searchQuery.isBlank()) "Non ci sono ancora attività disponibili." else "Nessuna attività corrisponde alla ricerca.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        } else {
            // Render activities list
            filteredActivities.forEach { activity ->
                ActivityCard(activity = activity)
            }
        }

        Spacer(modifier = Modifier.height(110.dp))
    }
}

@Composable
fun ActivityCard(activity: ActivityDto) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = activity.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Organizzato da: ${activity.organizer ?: "N/D"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                
                // Price Badge
                val priceDouble = activity.price?.toDouble() ?: 0.0
                val priceText = if (priceDouble <= 0.0) "Gratuito" else "€${String.format("%.2f", priceDouble)}"
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (priceDouble <= 0.0) Color(0xFFDCFCE7) else MaterialTheme.colorScheme.primaryContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = priceText,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (priceDouble <= 0.0) Color(0xFF15803D) else MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            if (!activity.description.isNullOrBlank()) {
                Text(
                    text = activity.description ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    maxLines = 3
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Location
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = activity.location,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }

                // Date
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
                    Text(
                        text = formatDateTime(activity.startTime),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

fun formatDateTime(dateTime: java.time.LocalDateTime?): String {
    if (dateTime == null) return ""
    return try {
        val formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
        dateTime.format(formatter)
    } catch (e: Exception) {
        dateTime.toString()
    }
}

@Composable
fun SimplePlaceholderScreen(title: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeScreenViaggiatorePreview() {
    TravelTheme {
        HomeScreen(
            user = User(
                email = "viaggiatore@travel.com",
                username = "viaggiatore123",
                userType = "VIAGGIATORE",
                name = "Marco Rossi"
            )
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeScreenSocietaPreview() {
    TravelTheme {
        HomeScreen(
            user = User(
                email = "societa@travel.com",
                username = "travel_agency",
                userType = "SOCIETA",
                name = "Agenzia Viaggi Italia S.r.l."
            )
        )
    }
}
