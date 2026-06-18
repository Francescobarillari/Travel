package com.travel.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.travel.app.data.AppContainer
import com.travel.app.data.repository.UserRepositoryImpl
import com.travel.app.presentation.auth.AuthViewModel
import com.travel.app.presentation.auth.LoginScreen
import com.travel.app.presentation.auth.RegisterScreen
import com.travel.app.presentation.home.HomeScreen
import com.travel.app.presentation.navigation.Screen
import com.travel.app.presentation.theme.TravelTheme
import com.travel.app.service.ApiService

class MainActivity : ComponentActivity() {

    private val userRepository = AppContainer.userRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val authViewModel = remember { AuthViewModel(userRepository) }

            TravelTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var currentScreen by remember { mutableStateOf(Screen.LOGIN) }

                    when (currentScreen) {
                        Screen.LOGIN -> {
                            LoginScreen(
                                viewModel = authViewModel,
                                onNavigateToRegister = { currentScreen = Screen.REGISTER },
                                onLoginSuccess = { currentScreen = Screen.HOME }
                            )
                        }
                        Screen.REGISTER -> {
                            RegisterScreen(
                                viewModel = authViewModel,
                                onNavigateToLogin = { currentScreen = Screen.LOGIN },
                                onRegisterSuccess = { currentScreen = Screen.HOME }
                            )
                        }
                        Screen.HOME -> {
                            HomeScreen()
                        }
                    }
                }
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
    }
    val mockRepo = UserRepositoryImpl(mockApiService)
    val mockViewModel = AuthViewModel(mockRepo)
    TravelTheme {
        LoginScreen(viewModel = mockViewModel, onNavigateToRegister = {}, onLoginSuccess = {})
    }
}
