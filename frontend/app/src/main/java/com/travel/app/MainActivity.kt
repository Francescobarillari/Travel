package com.travel.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import com.travel.app.data.AppContainer
import com.travel.app.data.repository.UserRepositoryImpl
import com.travel.app.presentation.app.TravelApp
import com.travel.app.presentation.auth.AuthViewModel
import com.travel.app.presentation.auth.LoginScreen
import com.travel.app.presentation.auth.RegisterScreen
import com.travel.app.presentation.theme.TravelTheme
import com.travel.app.service.ApiService

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TravelTheme {
                TravelApp()
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LoginScreenPreview() {
    val mockApiService = object : ApiService {
        override suspend fun login(request: com.travel.app.data.dto.LoginRequest) = "mock_token"
        override suspend fun register(request: com.travel.app.data.dto.SignupRequest) = "mock_user_id"
        override suspend fun getMe() = com.travel.app.data.dto.UserDTO(email = "test@travel.com")
        override suspend fun updateMe(request: com.travel.app.data.dto.UserDTO) = request
    }
    val mockRepo = UserRepositoryImpl(mockApiService) { error("Not used in preview") }
    val mockViewModel = AuthViewModel(mockRepo)
    TravelTheme {
        LoginScreen(viewModel = mockViewModel, onNavigateToRegister = {}, onLoginSuccess = {})
    }
}
