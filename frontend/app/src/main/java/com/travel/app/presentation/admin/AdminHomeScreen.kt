package com.travel.app.presentation.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.travel.app.domain.model.User
import com.travel.app.presentation.theme.TravelTheme

@Composable
fun AdminHomeScreen(
    user: User?,
    viewModel: AdminViewModel,
    isDarkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit,
    onLogout: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(AdminTab.DASHBOARD) }

    TravelTheme(darkTheme = isDarkMode) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (selectedTab) {
                AdminTab.DASHBOARD -> {
                    AdminDashboardScreen(
                        viewModel = viewModel,
                        onLogout = onLogout
                    )
                }
                AdminTab.SOCIETA -> {
                    AdminCompaniesScreen(
                        viewModel = viewModel
                    )
                }
                AdminTab.MENU -> {
                    AdminMenuScreen(
                        user = user,
                        isDarkMode = isDarkMode,
                        onDarkModeChange = onDarkModeChange,
                        onLogout = onLogout
                    )
                }
            }

            // Barra di navigazione fluttuante in basso
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
            ) {
                AdminBottomNavBar(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it }
                )
            }
        }
    }
}

@Composable
fun AdminBottomNavBar(
    selectedTab: AdminTab,
    onTabSelected: (AdminTab) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(32.dp),
                    clip = false
                )
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(32.dp)
                )
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            AdminTabItem(
                icon = Icons.Default.Dashboard,
                label = "Dashboard",
                isSelected = selectedTab == AdminTab.DASHBOARD,
                onClick = { onTabSelected(AdminTab.DASHBOARD) },
                modifier = Modifier.weight(1f)
            )
            AdminTabItem(
                icon = Icons.Default.Business,
                label = "Agenzie",
                isSelected = selectedTab == AdminTab.SOCIETA,
                onClick = { onTabSelected(AdminTab.SOCIETA) },
                modifier = Modifier.weight(1f)
            )
            AdminTabItem(
                icon = Icons.Default.Menu,
                label = "Menù",
                isSelected = selectedTab == AdminTab.MENU,
                onClick = { onTabSelected(AdminTab.MENU) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun AdminTabItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val contentColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
    val backgroundColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent

    Box(
        modifier = modifier
            .padding(vertical = 6.dp, horizontal = 4.dp)
            .height(48.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = contentColor,
            modifier = Modifier.size(24.dp)
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, showSystemUi = true)
@Composable
fun AdminHomeScreenPreview() {
    val mockApiService = object : com.travel.app.service.MockApiService() {
        override suspend fun login(request: it.unical.ea.dtos.authDto.LoginRequest) = it.unical.ea.dtos.authDto.JwtResponse("mock", "mock_refresh")
        override suspend fun register(request: it.unical.ea.dtos.authDto.SignupRequest) = "mock"
        override suspend fun getMe() = it.unical.ea.dtos.user.UserDTO()
        override suspend fun updateMe(request: it.unical.ea.dtos.user.UserDTO) = request
        override suspend fun createActivity(request: it.unical.ea.dtos.activity.ActivityDto) = request
        override suspend fun getActivities() = emptyList<it.unical.ea.dtos.activity.ActivityDto>()
        override suspend fun searchActivities(query: String, minPrice: Double?, maxPrice: Double?, page: Int, size: Int) = it.unical.ea.dtos.common.PageDto<it.unical.ea.dtos.activity.ActivityDto>()
        override suspend fun searchLocalita(query: String, page: Int, size: Int) = it.unical.ea.dtos.common.PageDto<it.unical.ea.dtos.location.LocationDto>()
        override suspend fun getLocalitaById(id: String) = it.unical.ea.dtos.location.LocationDto()
        override suspend fun getActivityById(id: String) = it.unical.ea.dtos.activity.ActivityDto()
        override suspend fun getItineraries() = emptyList<it.unical.ea.dtos.itinerary.ItineraryDto>()
        override suspend fun createItinerary(request: it.unical.ea.dtos.itinerary.CreateItineraryRequest) = it.unical.ea.dtos.itinerary.ItineraryDto()
        override suspend fun deleteItinerary(id: String) {}
        override suspend fun uploadDocument(file: okhttp3.MultipartBody.Part) = "mock"
        override suspend fun getPendingCompanies(): List<it.unical.ea.dtos.user.UserDTO> {
            return listOf(
                it.unical.ea.dtos.user.UserDTO().apply {
                    id = java.util.UUID.fromString("11111111-1111-1111-1111-111111111111")
                    email = "azienda@esempio.com"
                    userType = it.unical.ea.enums.UserType.SOCIETA
                    companyName = "Azienda di Trasporti S.r.l."
                    vatNumber = "IT12345678901"
                    phone = "+39 02 123456"
                    documentPhotos = listOf("companies/documents/doc1.jpg")
                }
            )
        }
        override suspend fun approveCompany(id: String) {}
        override suspend fun rejectCompany(id: String) {}
        override suspend fun getPendingActivities(): List<it.unical.ea.dtos.activity.ActivityDto> {
            return listOf(
                it.unical.ea.dtos.activity.ActivityDto().apply {
                    id = java.util.UUID.fromString("22222222-2222-2222-2222-222222222222")
                    name = "Gita in barca"
                    description = "Escursione splendida."
                    location = "Napoli"
                    startTime = java.time.LocalDateTime.now()
                    endTime = java.time.LocalDateTime.now().plusHours(2)
                    price = java.math.BigDecimal("40")
                    participants = 10
                    organizer = "Velisti Anonimi"
                }
            )
        }
        override suspend fun approveActivity(id: String) {}
        override suspend fun rejectActivity(id: String) {}
        override suspend fun getAllCompanies() = emptyList<it.unical.ea.dtos.user.UserDTO>()
        override suspend fun blockCompany(id: String) {}
        override suspend fun unblockCompany(id: String) {}
    }

    val mockUserRepository = object : com.travel.app.domain.repository.UserRepository {
        override suspend fun login(email: String, password: String, captchaToken: String?) = Result.failure<User>(Exception())
        override suspend fun registerViaggiatoreUser(email: String, firstName: String, lastName: String, password: String, phone: String?, captchaToken: String?) = Result.failure<User>(Exception())
        override suspend fun registerSocietaUser(email: String, companyName: String, vatNumber: String, password: String, phone: String?, captchaToken: String?, documentPhotos: List<String>) = Result.failure<User>(Exception())
        override fun getSessionUser(): User? = null
        override fun saveSession(user: User, token: String) {}
        override fun logout() {}
        override suspend fun getMe() = Result.failure<User>(Exception())
        override suspend fun updateMe(user: User) = Result.success(user)
        override suspend fun uploadDocument(fileBytes: ByteArray, filename: String) = Result.success("mock")
        override suspend fun getAllCompanies() = Result.success(emptyList<User>())
        override suspend fun blockCompany(id: String) = Result.success(Unit)
        override suspend fun unblockCompany(id: String) = Result.success(Unit)
    }

    val mockViewModel = remember {
        AdminViewModel(mockApiService, mockUserRepository).apply {
            loadDataForPreview(
                companies = emptyList(),
                activities = emptyList(),
                allComps = emptyList()
            )
        }
    }

    AdminHomeScreen(
        user = User(email = "admin@travel.com", name = "Amministratore"),
        viewModel = mockViewModel,
        isDarkMode = false,
        onDarkModeChange = {},
        onLogout = {}
    )
}
