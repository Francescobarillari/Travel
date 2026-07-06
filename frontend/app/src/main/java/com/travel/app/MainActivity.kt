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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LoginScreenPreview() {
    val mockApiService = object : ApiService {
        override suspend fun login(request: it.unical.ea.dtos.authDto.LoginRequest) = it.unical.ea.dtos.authDto.JwtResponse("mock_token", "mock_refresh")
        override suspend fun register(request: it.unical.ea.dtos.authDto.SignupRequest) = "mock_user_id"
        override suspend fun getMe() = it.unical.ea.dtos.user.UserDTO().apply { email = "test@travel.com" }
        override suspend fun updateMe(request: it.unical.ea.dtos.user.UserDTO) = request
        override suspend fun createActivity(request: it.unical.ea.dtos.activity.ActivityDto) = request
        override suspend fun getActivities() = emptyList<it.unical.ea.dtos.activity.ActivityDto>()
        override suspend fun searchActivities(
            query: String, minPrice: Double?, maxPrice: Double?, page: Int, size: Int
        ) = it.unical.ea.dtos.common.PageDto<it.unical.ea.dtos.activity.ActivityDto>()
        override suspend fun searchLocalita(
            query: String, page: Int, size: Int
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
        LoginScreen(viewModel = mockViewModel, onNavigateToRegister = {}, onLoginSuccess = {})
    }
}
