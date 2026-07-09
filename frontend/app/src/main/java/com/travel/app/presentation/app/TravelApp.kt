package com.travel.app.presentation.app

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
    val isOnline by AppContainer.networkMonitor.isOnline.collectAsState(initial = true)

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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedVisibility(visible = !isOnline) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp, horizontal = 16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudOff,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Sei offline.",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                color = MaterialTheme.colorScheme.background
            ) {
                if (isCheckingSession) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                } else {
                    when (currentScreen) {
                        Screen.LOGIN -> {
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
}
