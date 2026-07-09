package com.travel.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import com.travel.app.data.AppContainer
import com.travel.app.data.repository.UserRepositoryImpl
import com.travel.app.presentation.app.TravelApp
import com.travel.app.presentation.auth.LoginViewModel
import com.travel.app.presentation.auth.LoginScreen
import com.travel.app.presentation.auth.RegisterScreen
import com.travel.app.presentation.theme.TravelTheme
import com.travel.app.service.ApiService
import java.util.UUID

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        // Permesso gestito
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Crea i canali per le notifiche native
        com.travel.app.service.notification.NotificationHelper.createNotificationChannels(this)
        
        // Richiede i permessi su Android 13+
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }

        // Avvia il WorkManager per il polling in background
        setupNotificationWorkManager()

        setContent {
            var isDarkMode by remember {
                mutableStateOf(AppContainer.sessionManager.isDarkMode())
            }
            TravelTheme(darkTheme = isDarkMode) {
                TravelApp(
                    isDarkMode = isDarkMode,
                    onDarkModeChange = { enabled ->
                        isDarkMode = enabled
                        AppContainer.sessionManager.setDarkMode(enabled)
                    }
                )
            }
        }
    }

    private fun setupNotificationWorkManager() {
        val workRequest = androidx.work.PeriodicWorkRequestBuilder<com.travel.app.service.notification.NotificationSyncWorker>(
            15, java.util.concurrent.TimeUnit.MINUTES
        ).setConstraints(
            androidx.work.Constraints.Builder()
                .setRequiredNetworkType(androidx.work.NetworkType.CONNECTED)
                .build()
        ).build()

        androidx.work.WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "NotificationSyncWork",
            androidx.work.ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LoginScreenPreview() {
    val mockApiService = object : com.travel.app.service.MockApiService() {
        override suspend fun login(request: it.unical.ea.dtos.authDto.LoginRequest) = it.unical.ea.dtos.authDto.JwtResponse("mock_token", "mock_refresh")
        override suspend fun register(request: it.unical.ea.dtos.authDto.SignupRequest) = "mock_user_id"
        override suspend fun getMe() = it.unical.ea.dtos.user.UserDTO().apply { email = "test@travel.com" }
        override suspend fun updateMe(request: it.unical.ea.dtos.user.UserDTO) = request
        override suspend fun createActivity(request: it.unical.ea.dtos.activity.CreateActivityRequestDto) = it.unical.ea.dtos.activity.ActivityTemplateDto()
        override suspend fun getActivities() = emptyList<it.unical.ea.dtos.activity.ActivityDto>()
        override suspend fun searchActivities(
            query: String, minStartTime: String?, page: Int, size: Int
        ) = it.unical.ea.dtos.common.PageDto<it.unical.ea.dtos.activity.ActivityTemplateDto>()
        override suspend fun searchLocalita(
            query: String, includeExternal: Boolean, page: Int, size: Int
        ) = it.unical.ea.dtos.common.PageDto<it.unical.ea.dtos.location.LocationDto>()
        override suspend fun getLocalitaById(id: String) = it.unical.ea.dtos.location.LocationDto()
        override suspend fun getActivityById(id: String) = it.unical.ea.dtos.activity.ActivityDto()
        override suspend fun getItineraries()= emptyList<it.unical.ea.dtos.itinerary.ItineraryDto>()
        override suspend fun createItinerary(request: it.unical.ea.dtos.itinerary.CreateItineraryRequest) = it.unical.ea.dtos.itinerary.ItineraryDto().apply {
            title = request.title
            description = request.description
            startDateTime = request.startDateTime
            endDateTime = request.endDateTime
            creatorId = request.creatorId as UUID?
        }
        override suspend fun deleteItinerary(id: String) {}
        override suspend fun bookActivity(id: String) = it.unical.ea.dtos.payment.PaymentIntentResponseDto()
        override suspend fun bookItinerary(id: String) = it.unical.ea.dtos.payment.PaymentIntentResponseDto()
        override suspend fun confirmItineraryBooking(bookingId: String): retrofit2.Response<Unit> = retrofit2.Response.success(Unit)
        override suspend fun uploadDocument(file: okhttp3.MultipartBody.Part) = "mock"
        override suspend fun getPendingCompanies() = emptyList<it.unical.ea.dtos.user.UserDTO>()
        override suspend fun approveCompany(id: String) {}
        override suspend fun rejectCompany(id: String) {}
        override suspend fun getPendingActivities() = emptyList<it.unical.ea.dtos.activity.ActivityDto>()
        override suspend fun approveActivity(id: String) {}
        override suspend fun rejectActivity(id: String) {}
        override suspend fun getAllCompanies() = emptyList<it.unical.ea.dtos.user.UserDTO>()
        override suspend fun blockCompany(id: String) {}
        override suspend fun unblockCompany(id: String) {}
    }
    val mockRepo = UserRepositoryImpl(mockApiService) { error("Not used in preview") }
    val mockViewModel = LoginViewModel(mockRepo)
    TravelTheme {
        LoginScreen(viewModel = mockViewModel, onNavigateToRegister = {}, onNavigateToForgotPassword = {}, onLoginSuccess = {})
    }
}
