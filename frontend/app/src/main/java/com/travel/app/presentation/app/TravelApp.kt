package com.travel.app.presentation.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.travel.app.data.AppContainer
import com.travel.app.presentation.auth.AuthViewModel
import com.travel.app.presentation.auth.LoginScreen
import com.travel.app.presentation.auth.RegisterScreen
import com.travel.app.presentation.home.HomeScreen
import com.travel.app.presentation.navigation.Screen

@Composable
fun TravelApp() {
    val userRepository = AppContainer.userRepository
    val authViewModel = remember { AuthViewModel(userRepository) }
    var currentScreen by remember { mutableStateOf(Screen.LOGIN) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
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
