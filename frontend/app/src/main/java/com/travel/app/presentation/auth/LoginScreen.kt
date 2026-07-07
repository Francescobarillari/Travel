package com.travel.app.presentation.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.travel.app.R
import com.travel.app.data.repository.UserRepositoryImpl
import com.travel.app.domain.model.User
import com.travel.app.presentation.components.auth.ErrorBanner
import com.travel.app.presentation.components.auth.PasswordField
import com.travel.app.presentation.components.auth.TravelTextField
import com.travel.app.presentation.components.auth.ReCaptchaDialog
import com.travel.app.presentation.components.auth.TypewriterText
import com.travel.app.presentation.theme.TravelTheme
import com.travel.app.service.ApiService

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    onLoginSuccess: (User) -> Unit,
) {
    var showCaptcha by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    LaunchedEffect(scrollState.isScrollInProgress) {
        if (scrollState.isScrollInProgress) {
            focusManager.clearFocus()
        }
    }

    TravelTheme(darkTheme = false) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f), MaterialTheme.colorScheme.background)))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    focusManager.clearFocus()
                },
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                TypewriterText(
                    text = "Dèrive",
                    modifier = Modifier.padding(vertical = 40.dp),
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 4.sp
                    )
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text("Accedi al tuo Account", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)

                        TravelTextField(
                            value = viewModel.loginEmail,
                            onValueChange = { viewModel.loginEmail = it },
                            label = "Indirizzo Email",
                            leadingIcon = Icons.Default.Email,
                            keyboardType = KeyboardType.Email,
                        )

                        PasswordField(
                            value = viewModel.loginPassword,
                            onValueChange = { viewModel.loginPassword = it },
                            label = "Password",
                        )

                        viewModel.loginError?.let { ErrorBanner(message = it) }

                        Button(
                            onClick = {
                                if (viewModel.isCaptchaRequiredState) {
                                    showCaptcha = true
                                } else {
                                    viewModel.login(null, onLoginSuccess)
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = Color.White),
                            enabled = !viewModel.isLoading
                        ) {
                            if (viewModel.isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                            else Text("Accedi", style = MaterialTheme.typography.labelLarge)
                        }

                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            TextButton(
                                onClick = onNavigateToForgotPassword,
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text(
                                    "Password dimenticata?",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.secondary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                    Text("Non hai ancora un account? ", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                    TextButton(onClick = onNavigateToRegister) {
                        Text("Registrati", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                    }
                }
            }

            if (showCaptcha) {
                ReCaptchaDialog(
                    onDismiss = { showCaptcha = false },
                    onSuccess = { token ->
                        showCaptcha = false
                        viewModel.login(token, onSuccess = onLoginSuccess)
                    }
                )
            }
        }
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
        override suspend fun createActivity(request: it.unical.ea.dtos.activity.ActivityDto) = request
        override suspend fun getActivities() = emptyList<it.unical.ea.dtos.activity.ActivityDto>()
        override suspend fun searchActivities(query: String, minPrice: Double?, maxPrice: Double?, page: Int, size: Int) = it.unical.ea.dtos.common.PageDto<it.unical.ea.dtos.activity.ActivityDto>()
        override suspend fun searchLocalita(query: String, includeExternal: Boolean, page: Int, size: Int) = it.unical.ea.dtos.common.PageDto<it.unical.ea.dtos.location.LocationDto>()
        override suspend fun getLocalitaById(id: String) = it.unical.ea.dtos.location.LocationDto()
        override suspend fun getActivityById(id: String) = it.unical.ea.dtos.activity.ActivityDto()
        override suspend fun getItineraries() = emptyList<it.unical.ea.dtos.itinerary.ItineraryDto>()
        override suspend fun createItinerary(request: it.unical.ea.dtos.itinerary.CreateItineraryRequest) = it.unical.ea.dtos.itinerary.ItineraryDto()
        override suspend fun deleteItinerary(id: String) {}
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
    TravelTheme {
        LoginScreen(
            viewModel = LoginViewModel(UserRepositoryImpl(mockApiService) { error("Not used in preview") }),
            onNavigateToRegister = {},
            onNavigateToForgotPassword = {},
            onLoginSuccess = {}
        )
    }
}
