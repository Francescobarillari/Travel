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
import com.travel.app.presentation.navigation.Screen
import com.travel.app.presentation.theme.TravelTheme

class MainActivity : ComponentActivity() {

    private val userRepository: UserRepository = UserRepositoryImpl()

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
                                onLoginSuccess = {}
                            )
                        }
                        Screen.REGISTER -> {
                            RegisterScreen(
                                viewModel = authViewModel,
                                onNavigateToLogin = { currentScreen = Screen.LOGIN },
                                onRegisterSuccess = {}
                            )
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
    val mockRepo = UserRepositoryImpl()
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
    val mockRepo = UserRepositoryImpl()
    val mockViewModel = AuthViewModel(mockRepo)
    TravelTheme {
        RegisterScreen(
            viewModel = mockViewModel,
            onNavigateToLogin = {},
            onRegisterSuccess = {}
        )
    }
}
