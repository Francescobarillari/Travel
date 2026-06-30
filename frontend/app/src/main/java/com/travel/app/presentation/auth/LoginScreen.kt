package com.travel.app.presentation.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.travel.app.R
import com.travel.app.data.repository.UserRepositoryImpl
import com.travel.app.domain.model.User
import com.travel.app.presentation.components.auth.ErrorBanner
import com.travel.app.presentation.components.auth.PasswordField
import com.travel.app.presentation.components.auth.TravelTextField
import com.travel.app.presentation.theme.TravelTheme
import com.travel.app.service.ApiService

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: (User) -> Unit,
) {
    TravelTheme(darkTheme = false) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f), MaterialTheme.colorScheme.background))),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "Logo",
                    modifier = Modifier.size(240.dp).clip(RoundedCornerShape(16.dp))
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
                            onClick = { viewModel.login(onLoginSuccess) },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = Color.White),
                            enabled = !viewModel.isLoading
                        ) {
                            if (viewModel.isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                            else Text("Accedi", style = MaterialTheme.typography.labelLarge)
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
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LoginScreenPreview() {
    val mockApiService = object : ApiService {
        override suspend fun login(request: it.unical.ea.dtos.authDto.LoginRequest) = "mock_token"
        override suspend fun register(request: it.unical.ea.dtos.authDto.SignupRequest) = "mock_user_id"
        override suspend fun getMe() = it.unical.ea.dtos.user.UserDTO().apply { email = "test@travel.com" }
        override suspend fun updateMe(request: it.unical.ea.dtos.user.UserDTO) = request
        override suspend fun createActivity(request: it.unical.ea.dtos.activity.ActivityDto) = request
        override suspend fun getActivities() = emptyList<it.unical.ea.dtos.activity.ActivityDto>()
    }
    TravelTheme {
        LoginScreen(
            viewModel = AuthViewModel(UserRepositoryImpl(mockApiService) { error("Not used in preview") }),
            onNavigateToRegister = {},
            onLoginSuccess = {}
        )
    }
}
