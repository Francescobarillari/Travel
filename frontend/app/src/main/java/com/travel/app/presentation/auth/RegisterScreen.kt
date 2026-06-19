package com.travel.app.presentation.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.travel.app.R
import com.travel.app.domain.model.User
import com.travel.app.presentation.theme.TravelBgEnd
import com.travel.app.presentation.theme.TravelBgMid
import com.travel.app.presentation.theme.TravelBgStart
import com.travel.app.presentation.theme.TravelPrimary
import com.travel.app.presentation.theme.TravelSecondary
import androidx.compose.ui.tooling.preview.Preview
import com.travel.app.presentation.theme.TravelTheme
import com.travel.app.data.repository.UserRepositoryImpl
import com.travel.app.service.ApiService

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    viewModel: AuthViewModel,
    onNavigateToLogin: () -> Unit,
    onRegisterSuccess: (User) -> Unit
) {
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(TravelBgStart, TravelBgMid, TravelBgEnd)
                )
            ),
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
                modifier = Modifier
                    .size(240.dp)
                    .clip(RoundedCornerShape(16.dp))
            )

            // Card del Form
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Inserisci i tuoi dati",
                        style = MaterialTheme.typography.titleMedium,
                        color = TravelPrimary
                    )

                    // Campo Nome
                    OutlinedTextField(
                        value = viewModel.registerFirstName,
                        onValueChange = { viewModel.registerFirstName = it },
                        label = { Text("Nome") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = TravelSecondary) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TravelSecondary,
                            focusedLabelColor = TravelSecondary
                        )
                    )

                    // Campo Cognome
                    OutlinedTextField(
                        value = viewModel.registerLastName,
                        onValueChange = { viewModel.registerLastName = it },
                        label = { Text("Cognome") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = TravelSecondary) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TravelSecondary,
                            focusedLabelColor = TravelSecondary
                        )
                    )

                    // Campo Email
                    OutlinedTextField(
                        value = viewModel.registerEmail,
                        onValueChange = { viewModel.registerEmail = it },
                        label = { Text("Indirizzo Email") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = TravelSecondary) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TravelSecondary,
                            focusedLabelColor = TravelSecondary
                        )
                    )

                    // Campo Password
                    OutlinedTextField(
                        value = viewModel.registerPassword,
                        onValueChange = { viewModel.registerPassword = it },
                        label = { Text("Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = TravelSecondary) },
                        trailingIcon = {
                            val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(imageVector = image, contentDescription = null, tint = Color.Gray)
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TravelSecondary,
                            focusedLabelColor = TravelSecondary
                        )
                    )

                    // Campo Conferma Password
                    OutlinedTextField(
                        value = viewModel.registerConfirmPassword,
                        onValueChange = { viewModel.registerConfirmPassword = it },
                        label = { Text("Conferma Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = TravelSecondary) },
                        trailingIcon = {
                            val image = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                            IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                Icon(imageVector = image, contentDescription = null, tint = Color.Gray)
                            }
                        },
                        visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TravelSecondary,
                            focusedLabelColor = TravelSecondary
                        )
                    )

                    // Banner Errore (se presente)
                    viewModel.registerError?.let { error ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = error,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Bottone Registrati
                    Button(
                        onClick = { viewModel.register(onRegisterSuccess) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .padding(top = 8.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = TravelPrimary,
                            contentColor = Color.White
                        ),
                        enabled = !viewModel.isLoading
                    ) {
                        if (viewModel.isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text(
                                "Registrati",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }

            // Link Torna al Login
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Hai già un account? ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
                TextButton(onClick = onNavigateToLogin) {
                    Text(
                        text = "Accedi",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = TravelSecondary
                    )
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
    val mockRepo = UserRepositoryImpl(mockApiService)
    val mockViewModel = AuthViewModel(mockRepo)
    TravelTheme {
        RegisterScreen(viewModel = mockViewModel, onNavigateToLogin = {}, onRegisterSuccess = {})
    }
}
