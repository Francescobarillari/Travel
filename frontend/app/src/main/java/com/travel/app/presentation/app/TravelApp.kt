package com.travel.app.presentation.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.travel.app.domain.model.User
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
    var currentUser by remember { mutableStateOf<User?>(null) }

    // Flusso di Auto-Login all'avvio dell'applicazione
    LaunchedEffect(Unit) {
        val cachedUser = userRepository.getSessionUser()
        if (cachedUser != null) {
            currentUser = cachedUser
            currentScreen = Screen.HOME

            // Sicurezza: Rinfresca i dati dal backend in background per verificare la validità del token
            userRepository.getMe().fold(
                onSuccess = { freshUser ->
                    currentUser = freshUser
                },
                onFailure = {
                    // Se il token è scaduto o non valido, effettua il logout per sicurezza
                    userRepository.logout()
                    currentUser = null
                    currentScreen = Screen.LOGIN
                }
            )
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        when (currentScreen) {
            Screen.LOGIN -> {
                LoginScreen(
                    viewModel = authViewModel,
                    onNavigateToRegister = { currentScreen = Screen.REGISTER },
                    onLoginSuccess = { user ->
                        currentUser = user
                        currentScreen = Screen.HOME
                    }
                )
            }
            Screen.REGISTER -> {
                RegisterScreen(
                    viewModel = authViewModel,
                    onNavigateToLogin = { currentScreen = Screen.LOGIN },
                    onRegisterSuccess = { user ->
                        currentUser = user
                        currentScreen = Screen.HOME
                    }
                )
            }
            Screen.HOME -> {
                HomeScreen(
                    user = currentUser,
                    onLogout = {
                        userRepository.logout()
                        currentUser = null
                        currentScreen = Screen.LOGIN
                    }
                )
            }
        }
    }
}
