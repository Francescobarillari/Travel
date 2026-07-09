package com.travel.app.presentation.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.travel.app.presentation.theme.TravelTheme
import it.unical.ea.dtos.activity.ActivityDto
import it.unical.ea.dtos.user.UserDTO
import com.travel.app.presentation.admin.components.ZoomableImageDialog
import com.travel.app.domain.model.User
import com.travel.app.presentation.admin.components.AdminSectionLabel
import com.travel.app.presentation.admin.components.AdminStatusColors
import com.travel.app.presentation.admin.components.EmptyPlaceholder
import com.travel.app.presentation.admin.components.CompanyCard
import com.travel.app.presentation.admin.components.ActivityModerationCard
import com.travel.app.presentation.admin.components.KpiCard
import com.travel.app.presentation.admin.components.CompanyStatusDonutChart
import com.travel.app.presentation.admin.components.ActivityStatusDonutChart
import com.travel.app.presentation.admin.components.StatusPill
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

    // Il back di sistema torna alla panoramica quando si è nelle liste di moderazione.
    androidx.activity.compose.BackHandler(enabled = selectedTab != 0) {
        selectedTab = 0
    }

    val isPreview = androidx.compose.ui.platform.LocalInspectionMode.current
    LaunchedEffect(Unit) {
        if (!isPreview) {
            viewModel.loadData()
        }
    }

    // Stato pull-to-refresh condiviso da tutti i tab interni
    val ptrState = rememberPullToRefreshState()
    if (ptrState.isRefreshing) {
        LaunchedEffect(true) {
            viewModel.loadData()
        }
    }
    LaunchedEffect(viewModel.isRefreshing, viewModel.isLoading) {
        if (!viewModel.isRefreshing && !viewModel.isLoading) {
            ptrState.endRefresh()
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
                    IconButton(
                        onClick = { viewModel.loadData() },
                        enabled = !viewModel.isRefreshing && !viewModel.isLoading
                    ) {
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
                .padding(bottom = 80.dp) // Lascia spazio per la navbar fluttuante
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Banner di errore con azione di retry
            viewModel.errorMessage?.let { msg ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = msg,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = { viewModel.loadData() }) {
                            Text(
                                text = "Riprova",
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(ptrState.nestedScrollConnection)
            ) {
                if (viewModel.isLoading) {
                    // Spinner a schermo intero: solo al primo caricamento
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
                            actingIds = viewModel.actingIds,
                            onApprove = { viewModel.approveCompany(it) },
                            onReject = { viewModel.rejectCompany(it) },
                            onImageClick = { activeImageForZoom = it }
                        )
                        2 -> PendingActivitiesList(
                            activities = viewModel.pendingActivities,
                            actingIds = viewModel.actingIds,
                            onApprove = { viewModel.approveActivity(it) },
                            onReject = { viewModel.rejectActivity(it) }
                        )
                    }
                }

                PullToRefreshContainer(
                    state = ptrState,
                    modifier = Modifier.align(Alignment.TopCenter)
                )
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
    val pendingCompaniesFromAll = viewModel.allCompanies.count { !it.approved && !it.blocked }
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
        AdminSectionLabel("Panoramica")

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
                title = "Agenzie in attesa",
                value = "${viewModel.pendingCompanies.size}",
                icon = Icons.Default.PendingActions,
                iconColor = AdminStatusColors.pending,
                modifier = Modifier.weight(1f),
                onClick = onNavigateToCompaniesTab
            )
            KpiCard(
                title = "Attività in attesa",
                value = "$pendingActivities",
                icon = Icons.Default.Explore,
                iconColor = AdminStatusColors.pending,
                modifier = Modifier.weight(1f),
                onClick = onNavigateToActivitiesTab
            )
        }

        AdminSectionLabel("Coda di moderazione")

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column {
                ModerationQueueRow(
                    icon = Icons.Default.Business,
                    title = "Agenzie in attesa",
                    subtitle = "Registrazioni da approvare o rifiutare",
                    count = viewModel.pendingCompanies.size,
                    onClick = onNavigateToCompaniesTab
                )
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                ModerationQueueRow(
                    icon = Icons.Default.Explore,
                    title = "Attività in attesa",
                    subtitle = "Proposte delle agenzie da moderare",
                    count = pendingActivities,
                    onClick = onNavigateToActivitiesTab
                )
            }
        }

        AdminSectionLabel("Statistiche")

        // Donut Chart Card - Società
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .clickable(onClick = onNavigateToCompaniesTab),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Agenzie per stato",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                CompanyStatusDonutChart(
                    approvedCount = approvedCompanies,
                    pendingCount = pendingCompaniesFromAll,
                    blockedCount = blockedCompanies
                )
            }
        }

        // Donut Chart Card - Attività
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .clickable(onClick = onNavigateToActivitiesTab),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Attività per stato",
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
private fun ModerationQueueRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    count: Int,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (count > 0) {
            StatusPill(text = "$count", color = AdminStatusColors.pending)
        } else {
            StatusPill(text = "0", color = AdminStatusColors.approved)
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
fun PendingCompaniesList(
    companies: List<UserDTO>,
    actingIds: Set<String>,
    onApprove: (String) -> Unit,
    onReject: (String) -> Unit,
    onImageClick: (String) -> Unit
) {
    // Sempre una LazyColumn (anche da vuota) così il pull-to-refresh resta attivo
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (companies.isEmpty()) {
            item {
                EmptyPlaceholder(
                    icon = Icons.Default.CheckCircle,
                    title = "Nessuna agenzia in attesa",
                    subtitle = "Tutte le registrazioni delle agenzie sono state elaborate.",
                    modifier = Modifier.fillParentMaxSize()
                )
            }
        } else {
            items(companies, key = { it.id.toString() }) { company ->
                CompanyCard(
                    company = company,
                    onApprove = { onApprove(company.id.toString()) },
                    onReject = { onReject(company.id.toString()) },
                    onImageClick = onImageClick,
                    isActing = company.id.toString() in actingIds
                )
            }
        }
    }
}

@Composable
fun PendingActivitiesList(
    activities: List<ActivityDto>,
    actingIds: Set<String>,
    onApprove: (String) -> Unit,
    onReject: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (activities.isEmpty()) {
            item {
                EmptyPlaceholder(
                    icon = Icons.Default.CheckCircle,
                    title = "Nessuna attività in attesa",
                    subtitle = "Tutte le proposte delle agenzie sono state moderate.",
                    modifier = Modifier.fillParentMaxSize()
                )
            }
        } else {
            items(activities, key = { it.id.toString() }) { activity ->
                ActivityModerationCard(
                    activity = activity,
                    onApprove = { onApprove(activity.id.toString()) },
                    onReject = { onReject(activity.id.toString()) },
                    isActing = activity.id.toString() in actingIds
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
        override suspend fun createActivity(request: it.unical.ea.dtos.activity.CreateActivityRequestDto) = it.unical.ea.dtos.activity.ActivityTemplateDto()
        override suspend fun getActivities() = emptyList<it.unical.ea.dtos.activity.ActivityDto>()
        override suspend fun searchActivities(query: String, minStartTime: String?, page: Int, size: Int) = it.unical.ea.dtos.common.PageDto<it.unical.ea.dtos.activity.ActivityTemplateDto>()
        override suspend fun searchLocalita(query: String, includeExternal: Boolean, page: Int, size: Int) = it.unical.ea.dtos.common.PageDto<it.unical.ea.dtos.location.LocationDto>()
        override suspend fun getLocalitaById(id: String) = it.unical.ea.dtos.location.LocationDto()
        override suspend fun getActivityById(id: String) = it.unical.ea.dtos.activity.ActivityDto()
        override suspend fun getItineraries() = emptyList<it.unical.ea.dtos.itinerary.ItineraryDto>()
        override suspend fun createItinerary(request: it.unical.ea.dtos.itinerary.CreateItineraryRequest) = it.unical.ea.dtos.itinerary.ItineraryDto()
        override suspend fun deleteItinerary(id: String) {}
        override suspend fun bookActivity(id: String) = it.unical.ea.dtos.payment.PaymentIntentResponseDto()
        override suspend fun bookItinerary(id: String) = it.unical.ea.dtos.payment.PaymentIntentResponseDto()
        override suspend fun confirmItineraryBooking(bookingId: String): retrofit2.Response<Unit> = retrofit2.Response.success(Unit)
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
        override suspend fun deleteAccount(userId: String) = Result.success(Unit)
        override suspend fun uploadAvatar(userId: String, imageBytes: ByteArray, mimeType: String, fileName: String) = Result.failure<User>(Exception())
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
