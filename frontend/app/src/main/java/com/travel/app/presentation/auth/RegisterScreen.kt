package com.travel.app.presentation.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
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
import com.travel.app.presentation.components.ErrorBanner
import com.travel.app.presentation.components.PasswordField
import com.travel.app.presentation.components.TravelTextField
import com.travel.app.presentation.theme.*
import com.travel.app.service.ApiService

@Composable
fun RegisterScreen(
    viewModel: AuthViewModel,
    onNavigateToLogin: () -> Unit,
    onRegisterSuccess: (User) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(TravelBgStart, TravelBgMid, TravelBgEnd))),
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
                modifier = Modifier.size(200.dp).clip(RoundedCornerShape(16.dp))
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text("Crea il tuo account", style = MaterialTheme.typography.titleMedium, color = TravelPrimary)

                    //tipo Utente
                    UserTypeSelectorRow(
                        selected = viewModel.registerUserType,
                        onSelect = { viewModel.registerUserType = it }
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    //Campi per VIAGGIATORE
                    AnimatedVisibility(visible = viewModel.registerUserType == UserType.VIAGGIATORE, enter = fadeIn(), exit = fadeOut()) {
                        ViaggiatoreFields(
                            firstName = viewModel.registerFirstName,
                            onFirstNameChange = { viewModel.registerFirstName = it },
                            lastName = viewModel.registerLastName,
                            onLastNameChange = { viewModel.registerLastName = it },
                        )
                    }

                    //cmpi SOCIETÀ
                    AnimatedVisibility(visible = viewModel.registerUserType == UserType.SOCIETA, enter = fadeIn(), exit = fadeOut()) {
                        SocietaFields(
                            companyName = viewModel.registerCompanyName,
                            onCompanyNameChange = { viewModel.registerCompanyName = it },
                            vatNumber = viewModel.registerVatNumber,
                            onVatNumberChange = { viewModel.registerVatNumber = it },
                        )
                    }

                    // Campi in comune
                    TravelTextField(value = viewModel.registerEmail, onValueChange = { viewModel.registerEmail = it }, label = "Indirizzo Email *", leadingIcon = Icons.Default.Email, keyboardType = KeyboardType.Email)
                    TravelTextField(value = viewModel.registerPhone, onValueChange = { viewModel.registerPhone = it }, label = "Telefono (opzionale)", leadingIcon = Icons.Default.Phone, keyboardType = KeyboardType.Phone)
                    PasswordField(value = viewModel.registerPassword, onValueChange = { viewModel.registerPassword = it }, label = "Password *")
                    PasswordField(value = viewModel.registerConfirmPassword, onValueChange = { viewModel.registerConfirmPassword = it }, label = "Conferma Password *")

                    viewModel.registerError?.let { ErrorBanner(message = it) }

                    Button(
                        onClick = { viewModel.register(onRegisterSuccess) },
                        modifier = Modifier.fillMaxWidth().height(52.dp).padding(top = 8.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TravelPrimary, contentColor = Color.White),
                        enabled = !viewModel.isLoading
                    ) {
                        if (viewModel.isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        else Text("Registrati", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                Text("Hai già un account? ", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                TextButton(onClick = onNavigateToLogin) {
                    Text("Accedi", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = TravelSecondary)
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun RegisterScreenPreview() {
    val mockApiService = object : ApiService {
        override suspend fun login(request: com.travel.app.data.dto.LoginRequestDto) = "mock_token"
        override suspend fun register(request: com.travel.app.data.dto.SignUpRequestDto) = "mock_user_id"
    }
    TravelTheme {
        RegisterScreen(
            viewModel = AuthViewModel(UserRepositoryImpl(mockApiService)),
            onNavigateToLogin = {},
            onRegisterSuccess = {}
        )
    }
}
