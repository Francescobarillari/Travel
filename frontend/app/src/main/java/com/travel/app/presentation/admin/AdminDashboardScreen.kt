package com.travel.app.presentation.admin

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.travel.app.BuildConfig
import com.travel.app.data.AppContainer
import androidx.compose.ui.tooling.preview.Preview
import com.travel.app.service.ApiService
import com.travel.app.presentation.theme.TravelTheme
import it.unical.ea.dtos.activity.ActivityDto
import it.unical.ea.dtos.user.UserDTO
import com.travel.app.presentation.admin.components.ZoomableImageDialog
import java.time.format.DateTimeFormatter
import com.travel.app.domain.model.User
import com.travel.app.presentation.admin.components.EmptyPlaceholder
import com.travel.app.presentation.admin.components.CompanyCard
import com.travel.app.presentation.admin.components.ActivityModerationCard
import com.travel.app.presentation.admin.components.KpiCard
import com.travel.app.presentation.admin.components.CompanyStatusDonutChart
import com.travel.app.presentation.admin.components.ActivityStatusDonutChart
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    viewModel: AdminViewModel,
    onLogout: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    var activeImageForZoom by remember { mutableStateOf<String?>(null) }

    val isPreview = androidx.compose.ui.platform.LocalInspectionMode.current
    LaunchedEffect(Unit) {
        if (!isPreview) {
            viewModel.loadData()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = "Admin",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Text(
                            text = when(selectedTab) {
                                0 -> "Admin Dashboard"
                                1 -> "Agenzie da approvare"
                                2 -> "Attività da moderare"
                                else -> "Admin Dashboard"
                            },
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                },
                navigationIcon = {
                    if (selectedTab != 0) {
                        IconButton(onClick = { selectedTab = 0 }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Indietro",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadData() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Aggiorna",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Error Banner
            viewModel.errorMessage?.let { msg ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Text(
                        text = msg,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                if (viewModel.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    when (selectedTab) {
                        0 -> OverviewTabContent(
                            viewModel = viewModel,
                            onNavigateToCompaniesTab = { selectedTab = 1 },
                            onNavigateToActivitiesTab = { selectedTab = 2 }
                        )
                        1 -> PendingCompaniesList(
                            companies = viewModel.pendingCompanies,
                            onApprove = { viewModel.approveCompany(it) },
                            onReject = { viewModel.rejectCompany(it) },
                            onImageClick = { activeImageForZoom = it }
                        )
                        2 -> PendingActivitiesList(
                            activities = viewModel.pendingActivities,
                            onApprove = { viewModel.approveActivity(it) },
                            onReject = { viewModel.rejectActivity(it) }
                        )
                    }
                }
            }
        }
    }

    // Zoom Dialog per visualizzare i documenti a schermo intero
    activeImageForZoom?.let { path ->
        ZoomableImageDialog(
            imagePath = path,
            onDismiss = { activeImageForZoom = null }
        )
    }
}

@Composable
fun OverviewTabContent(
    viewModel: AdminViewModel,
    onNavigateToCompaniesTab: () -> Unit,
    onNavigateToActivitiesTab: () -> Unit
) {
    val totalCompanies = viewModel.allCompanies.size
    val approvedCompanies = viewModel.allCompanies.count { it.approved && !it.blocked }
    val pendingCompanies = viewModel.allCompanies.count { !it.approved && !it.blocked }
    val blockedCompanies = viewModel.allCompanies.count { it.blocked }

    val approvedActivities = viewModel.approvedActivitiesCount
    val pendingActivities = viewModel.pendingActivities.size

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // KPI Cards Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            KpiCard(
                title = "Agenzie Totali",
                value = "$totalCompanies",
                icon = Icons.Default.Business,
                iconColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            KpiCard(
                title = "Agenzie Pending",
                value = "${viewModel.pendingCompanies.size}",
                icon = Icons.Default.PendingActions,
                iconColor = Color(0xFFE65100),
                modifier = Modifier.weight(1f),
                onClick = onNavigateToCompaniesTab
            )
            KpiCard(
                title = "Attività Pending",
                value = "$pendingActivities",
                icon = Icons.Default.Explore,
                iconColor = Color(0xFFC62828),
                modifier = Modifier.weight(1f),
                onClick = onNavigateToActivitiesTab
            )
        }

        // Donut Chart Card - Società
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Stato di approvazione Agenzie",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                CompanyStatusDonutChart(
                    approvedCount = approvedCompanies,
                    pendingCount = pendingCompanies,
                    blockedCount = blockedCompanies
                )
            }
        }

        // Donut Chart Card - Attività
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Stato di approvazione Attività",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                ActivityStatusDonutChart(
                    approvedCount = approvedActivities,
                    pendingCount = pendingActivities
                )
            }
        }
    }
}

@Composable
fun PendingCompaniesList(
    companies: List<UserDTO>,
    onApprove: (String) -> Unit,
    onReject: (String) -> Unit,
    onImageClick: (String) -> Unit
) {
    if (companies.isEmpty()) {
        EmptyPlaceholder(
            icon = Icons.Default.CheckCircle,
            title = "Nessuna agenzia in attesa",
            subtitle = "Tutte le registrazioni delle agenzie sono state elaborate."
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(companies) { company ->
                CompanyCard(
                    company = company,
                    onApprove = { onApprove(company.id.toString()) },
                    onReject = { onReject(company.id.toString()) },
                    onImageClick = onImageClick
                )
            }
        }
    }
}

@Composable
fun PendingActivitiesList(
    activities: List<ActivityDto>,
    onApprove: (String) -> Unit,
    onReject: (String) -> Unit
) {
    if (activities.isEmpty()) {
        EmptyPlaceholder(
            icon = Icons.Default.CheckCircle,
            title = "Nessuna attività in attesa",
            subtitle = "Tutte le proposte delle agenzie sono state moderate."
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(activities) { activity ->
                ActivityModerationCard(
                    activity = activity,
                    onApprove = { onApprove(activity.id.toString()) },
                    onReject = { onReject(activity.id.toString()) }
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AdminDashboardScreenPreview() {
    val mockApiService = object : com.travel.app.service.MockApiService() {
        override suspend fun login(request: it.unical.ea.dtos.authDto.LoginRequest) = it.unical.ea.dtos.authDto.JwtResponse("mock", "mock_refresh")
        override suspend fun register(request: it.unical.ea.dtos.authDto.SignupRequest) = "mock"
        override suspend fun getMe() = it.unical.ea.dtos.user.UserDTO()
        override suspend fun updateMe(request: it.unical.ea.dtos.user.UserDTO) = request
        override suspend fun createActivity(request: it.unical.ea.dtos.activity.ActivityDto) = request
        override suspend fun getActivities() = emptyList<it.unical.ea.dtos.activity.ActivityDto>()
        override suspend fun searchActivities(query: String, minPrice: Double?, maxPrice: Double?, page: Int, size: Int) = it.unical.ea.dtos.common.PageDto<it.unical.ea.dtos.activity.ActivityDto>()
        override suspend fun searchLocalita(query: String, includeExternal: Boolean, page: Int, size: Int) = it.unical.ea.dtos.common.PageDto<it.unical.ea.dtos.location.LocationDto>()
        override suspend fun getLocalitaById(id: String) = it.unical.ea.dtos.location.LocationDto()
        override suspend fun getActivityById(id: String) = it.unical.ea.dtos.activity.ActivityDto()
        override suspend fun getItineraries() = emptyList<it.unical.ea.dtos.itinerary.ItineraryDto>()
        override suspend fun createItinerary(request: it.unical.ea.dtos.itinerary.CreateItineraryRequest) = it.unical.ea.dtos.itinerary.ItineraryDto()
        override suspend fun deleteItinerary(id: String) {}
        override suspend fun uploadDocument(file: okhttp3.MultipartBody.Part) = "mock"
        override suspend fun getPendingCompanies(): List<UserDTO> = emptyList()
        override suspend fun approveCompany(id: String) {}
        override suspend fun rejectCompany(id: String) {}
        override suspend fun getPendingActivities(): List<ActivityDto> = emptyList()
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

    TravelTheme {
        AdminDashboardScreen(
            viewModel = mockViewModel,
            onLogout = {}
        )
    }
}
