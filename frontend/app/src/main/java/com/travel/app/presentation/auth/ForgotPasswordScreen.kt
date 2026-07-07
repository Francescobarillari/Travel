package com.travel.app.presentation.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.travel.app.R
import com.travel.app.presentation.components.auth.ErrorBanner
import com.travel.app.presentation.components.auth.PasswordField
import com.travel.app.presentation.components.auth.TravelTextField
import com.travel.app.presentation.theme.TravelTheme

@Composable
fun ForgotPasswordScreen(
    viewModel: ForgotPasswordViewModel,
    onNavigateToLogin: () -> Unit
) {
    TravelTheme(darkTheme = false) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                            MaterialTheme.colorScheme.background
                        )
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
                        .size(200.dp)
                        .clip(RoundedCornerShape(16.dp))
                )

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
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = if (!viewModel.otpSent) "Recupera la tua password" else "Imposta nuova password",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )

                        if (!viewModel.otpSent) {
                            Text(
                                text = "Inserisci l'indirizzo email associato al tuo account. Ti invieremo un codice OTP per poter reimpostare la tua password.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )

                            TravelTextField(
                                value = viewModel.email,
                                onValueChange = { viewModel.email = it },
                                label = "Indirizzo Email",
                                leadingIcon = Icons.Default.Email,
                                keyboardType = KeyboardType.Email,
                            )
                        } else {
                            Text(
                                text = "Abbiamo inviato un codice OTP di 6 cifre all'indirizzo ${viewModel.email}. Inseriscilo qui sotto insieme alla tua nuova password.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )

                            TravelTextField(
                                value = viewModel.otp,
                                onValueChange = { if (it.length <= 6) viewModel.otp = it },
                                label = "Codice OTP (6 cifre)",
                                leadingIcon = Icons.Default.Info,
                                keyboardType = KeyboardType.Number,
                            )

                            PasswordField(
                                value = viewModel.newPassword,
                                onValueChange = { viewModel.newPassword = it },
                                label = "Nuova Password",
                            )

                            PasswordField(
                                value = viewModel.confirmPassword,
                                onValueChange = { viewModel.confirmPassword = it },
                                label = "Conferma Password",
                            )
                        }

                        // Banner Errori
                        viewModel.error?.let { ErrorBanner(message = it) }

                        // Banner Successo
                        viewModel.successMessage?.let {
                            SuccessBanner(message = it)
                        }

                        // Pulsante di invio
                        Button(
                            onClick = {
                                if (!viewModel.otpSent) {
                                    viewModel.sendOtp()
                                } else {
                                    viewModel.resetPassword(onSuccess = {
                                        // Puoi mostrare il successo e poi tornare al login
                                    })
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = Color.White
                            ),
                            enabled = !viewModel.isLoading
                        ) {
                            if (viewModel.isLoading) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                            } else {
                                Text(
                                    text = if (!viewModel.otpSent) "Invia codice OTP" else "Reimposta Password",
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = {
                        viewModel.clearState()
                        onNavigateToLogin()
                    }) {
                        Text(
                            text = "Torna al Login",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SuccessBanner(
    message: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFE8F5E9)) // light green
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = Color(0xFF2E7D32) // dark green
        )
        Text(
            text = message,
            color = Color(0xFF2E7D32),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}
