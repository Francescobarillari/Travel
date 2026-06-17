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
import com.travel.app.data.repository.UserRepositoryImpl
import com.travel.app.domain.repository.UserRepository
import com.travel.app.presentation.auth.AuthViewModel
import com.travel.app.presentation.auth.LoginScreen
import com.travel.app.presentation.auth.RegisterScreen
import com.travel.app.presentation.home.HomeScreen
import com.travel.app.presentation.navigation.Screen
import com.travel.app.presentation.theme.TravelTheme

import com.travel.app.service.ApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : ComponentActivity() {

    private val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl("http://172.20.10.2:8080/") // URL per emulatore Android (punta al localhost del PC)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    private val userRepository: UserRepository by lazy { 
        UserRepositoryImpl(apiService) 
    }

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
                                onRegisterSuccess = {}
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

// Anteprime per la visualizzazione all'interno dell'IDE (Compose Preview)

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LoginScreenPreview() {
    // Mock per la preview che non fa chiamate di rete reali
    val mockApiService = object : ApiService {
        override suspend fun login(request: com.travel.app.data.dto.LoginRequestDto) = com.travel.app.data.dto.LoginResponseDto("token")
        override suspend fun register(request: com.travel.app.data.dto.SignUpRequestDto) = com.travel.app.data.dto.UserDto("id", "email", "first", "last")
    }
    val mockRepo = UserRepositoryImpl(mockApiService)
    val mockViewModel = AuthViewModel(mockRepo)
    TravelTheme {
        LoginScreen(
            viewModel = mockViewModel,
            onNavigateToRegister = {},
            onLoginSuccess = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun RegisterScreenPreview() {
    // Mock per la preview
    val mockApiService = object : ApiService {
        override suspend fun login(request: com.travel.app.data.dto.LoginRequestDto) = com.travel.app.data.dto.LoginResponseDto("token")
        override suspend fun register(request: com.travel.app.data.dto.SignUpRequestDto) = com.travel.app.data.dto.UserDto("id", "email", "first", "last")
    }
    val mockRepo = UserRepositoryImpl(mockApiService)
    val mockViewModel = AuthViewModel(mockRepo)
    TravelTheme {
        RegisterScreen(
            viewModel = mockViewModel,
            onNavigateToLogin = {},
            onRegisterSuccess = {}
        )
    }
}
