package com.travel.app.presentation.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.travel.app.domain.model.User
import com.travel.app.data.AppContainer
import com.travel.app.presentation.auth.LoginViewModel
import com.travel.app.presentation.auth.RegisterViewModel
import com.travel.app.presentation.auth.LoginScreen
import com.travel.app.presentation.auth.RegisterScreen
import com.travel.app.presentation.auth.ForgotPasswordViewModel
import com.travel.app.presentation.auth.ForgotPasswordScreen
import com.travel.app.presentation.home.HomeScreen
import com.travel.app.presentation.navigation.Screen
import com.travel.app.presentation.admin.AdminViewModel
import com.travel.app.presentation.admin.AdminHomeScreen
import kotlinx.coroutines.launch

@Composable
fun TravelApp(
    isDarkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit
) {
    val userRepository = AppContainer.userRepository
    var isCheckingSession by remember { mutableStateOf(true) }
    var currentScreen by remember { mutableStateOf(Screen.LOGIN) }
    var currentUser by remember { mutableStateOf<User?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Dalle schermate di registrazione / recupero password il back di sistema riporta
    // al Login invece di uscire dall'app.
    androidx.activity.compose.BackHandler(
        enabled = currentScreen == Screen.REGISTER || currentScreen == Screen.FORGOT_PASSWORD
    ) {
        currentScreen = Screen.LOGIN
    }

    // Flusso di Auto-Login all'avvio dell'applicazione
    LaunchedEffect(Unit) {
        val cachedUser = userRepository.getSessionUser()
        if (cachedUser != null) {
            currentUser = cachedUser
            currentScreen = if (cachedUser.userType == "ADMIN") Screen.ADMIN else Screen.HOME
            isCheckingSession = false // Mostra subito la schermata corretta se abbiamo la sessione in cache

            // Sicurezza: Rinfresca i dati dal backend in background per verificare la validità del token
            userRepository.getMe().fold(
                onSuccess = { freshUser ->
                    currentUser = freshUser
                    currentScreen = if (freshUser.userType == "ADMIN") Screen.ADMIN else Screen.HOME
                },
                onFailure = { error ->
                    // Se il token è scaduto o non valido (401), effettua il logout per sicurezza.
                    // Se è un errore di rete (IOException -> Nessuna connessione internet), manteniamo la sessione.
                    val errorMessage = error.message ?: ""
                    if (errorMessage == "Credenziali non valide" || errorMessage.contains("401")) {
                        userRepository.logout()
                        currentUser = null
                        currentScreen = Screen.LOGIN
                    }
                }
            )
        } else {
            isCheckingSession = false
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Surface(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            color = MaterialTheme.colorScheme.background
        ) {
            if (isCheckingSession) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else {
                when (currentScreen) {
                Screen.LOGIN -> {
                    // Scoped alla schermata, viene distrutto quando l'utente cambia screen (previene memorizzazione email/password passati)
                    val loginViewModel = remember { LoginViewModel(userRepository) }
                    LoginScreen(
                        viewModel = loginViewModel,
                        onNavigateToRegister = { currentScreen = Screen.REGISTER },
                        onNavigateToForgotPassword = { currentScreen = Screen.FORGOT_PASSWORD },
                        onLoginSuccess = { user ->
                            currentUser = user
                            currentScreen = if (user.userType == "ADMIN") Screen.ADMIN else Screen.HOME
                        }
                    )
                }
                Screen.REGISTER -> {
                    // Scoped alla schermata per evitare state leakage
                    val registerViewModel = remember { RegisterViewModel(userRepository) }
                    RegisterScreen(
                        viewModel = registerViewModel,
                        onNavigateToLogin = { currentScreen = Screen.LOGIN },
                        onRegisterSuccess = { user ->
                            currentScreen = Screen.LOGIN
                            coroutineScope.launch {
                                val msg = if (user.userType == "SOCIETA") {
                                    "Registrazione completata! Controlla la posta per validare l'account. Successivamente l'account dovrà essere approvato dall'amministratore."
                                } else {
                                    "Registrazione completata! Controlla la posta per validare l'account."
                                }
                                snackbarHostState.showSnackbar(msg)
                            }
                        }
                    )
                }
                Screen.FORGOT_PASSWORD -> {
                    val forgotPasswordViewModel = remember { ForgotPasswordViewModel(userRepository) }
                    ForgotPasswordScreen(
                        viewModel = forgotPasswordViewModel,
                        onNavigateToLogin = { currentScreen = Screen.LOGIN }
                    )
                }
                Screen.HOME -> {
                    HomeScreen(
                        user = currentUser,
                        isDarkMode = isDarkMode,
                        onDarkModeChange = onDarkModeChange,
                        onLogout = {
                            userRepository.logout()
                            currentUser = null
                            currentScreen = Screen.LOGIN
                        }
                    )
                }
                Screen.ADMIN -> {
                    val adminViewModel = remember { AdminViewModel() }
                    AdminHomeScreen(
                        user = currentUser,
                        viewModel = adminViewModel,
                        isDarkMode = isDarkMode,
                        onDarkModeChange = onDarkModeChange,
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
    }
}
