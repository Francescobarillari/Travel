package com.travel.app.presentation.profile

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.travel.app.domain.model.User
import com.travel.app.presentation.components.HeaderBackButton
import com.travel.app.presentation.components.HeaderConfirmButton
import com.travel.app.presentation.theme.TravelTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurityScreen(
    user: User?,
    viewModel: SecurityViewModel,
    onBack: () -> Unit,
    onSaveSuccess: (User) -> Unit,
    modifier: Modifier = Modifier
) {
    val initialUser = user ?: User(
        email = "johnkinggraphics@gmail.com",
        userType = "VIAGGIATORE",
        phone = "6895312",
        name = "Charlotte king"
    )

    LaunchedEffect(initialUser) {
        viewModel.initialize(initialUser)
    }

    val isSocieta = initialUser.userType == "SOCIETA"
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()
    var showConfirmationDialog by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    LaunchedEffect(scrollState.isScrollInProgress) {
        if (scrollState.isScrollInProgress) {
            focusManager.clearFocus()
        }
    }

    Box(
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                focusManager.clearFocus()
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .statusBarsPadding()
        ) {
            // TOP NAVIGATION HEADER (consistent style with purple accents for Security)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                HeaderBackButton(
                    onClick = onBack,
                    enabled = !viewModel.isLoading
                )

                Text(
                    text = "Sicurezza",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                HeaderConfirmButton(
                    onClick = {
                        if (viewModel.newPassword.isNotEmpty()) {
                            showConfirmationDialog = true
                        } else {
                            viewModel.saveSecurity { savedUser ->
                                onSaveSuccess(savedUser)
                                Toast.makeText(context, "Modifiche salvate con successo!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    enabled = !viewModel.isLoading,
                    isLoading = viewModel.isLoading,
                    iconColor = if (isDark) Color(0xFFA78BFA) else Color(0xFF7C3AED)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // AVATAR AREA WITH LOCK ICON BACKGROUND
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .padding(bottom = 8.dp),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    val purpleGradients = if (isDark) {
                        listOf(Color(0xFF4C1D95), Color(0xFF2E1065))
                    } else {
                        listOf(Color(0xFFEDE9FE), Color(0xFFDDD6FE))
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = purpleGradients
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "Security Status",
                            tint = if (isDark) Color(0xFFA78BFA) else Color(0xFF7C3AED),
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }

                // Error Message Card
                viewModel.errorMessage?.let { msg ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = msg,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(12.dp),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Success Message Card
                viewModel.successMessage?.let { msg ->
                    val successBg = if (isDark) Color(0xFF064E3B) else Color(0xFFF0FDF4)
                    val successText = if (isDark) Color(0xFF6EE7B7) else Color(0xFF15803D)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = successBg),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = msg,
                            color = successText,
                            modifier = Modifier.padding(12.dp),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Gestione Account removed

                // SECTION 2: MODIFICA PASSWORD
                Text(
                    text = "Modifica Password",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.align(Alignment.Start)
                )

                // Old Password Field
                PasswordInputField(
                    label = "Vecchia Password",
                    value = viewModel.oldPassword,
                    onValueChange = { viewModel.oldPassword = it }
                )

                // New Password Field
                PasswordInputField(
                    label = "Nuova Password",
                    value = viewModel.newPassword,
                    onValueChange = { viewModel.newPassword = it }
                )

                Spacer(modifier = Modifier.height(30.dp))
            }
        }

        if (showConfirmationDialog) {
            AlertDialog(
                onDismissRequest = { showConfirmationDialog = false },
                title = { Text(text = "Conferma cambio password") },
                text = { Text(text = "Sei sicuro di voler modificare la tua password di accesso?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showConfirmationDialog = false
                            viewModel.saveSecurity { savedUser ->
                                onSaveSuccess(savedUser)
                                Toast.makeText(context, "Modifiche salvate con successo!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) {
                        Text(text = "Conferma", color = if (isDark) Color(0xFFA78BFA) else Color(0xFF7C3AED))
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showConfirmationDialog = false }
                    ) {
                        Text(text = "Annulla", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                shape = RoundedCornerShape(28.dp),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            )
        }
    }
}

@Composable
fun PasswordInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var passwordVisible by remember { mutableStateOf(false) }
    val isDark = isSystemInDarkTheme()

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = label,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                val description = if (passwordVisible) "Nascondi password" else "Mostra password"
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = image, contentDescription = description, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedBorderColor = if (isDark) Color(0xFFA78BFA) else Color(0xFF7C3AED), // Accent color matching security (purple)
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
            )
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SecurityScreenPreview() {
    TravelTheme {
        val mockViewModel = remember {
            SecurityViewModel(
                userRepository = object : com.travel.app.domain.repository.UserRepository {
                    override fun getSessionUser(): User? = null
                    override fun saveSession(user: User, token: String) {}
                    override fun logout() {}
                    override suspend fun login(email: String, password: String, captchaToken: String?) = Result.failure<User>(Exception())
                    override suspend fun registerViaggiatoreUser(email: String, firstName: String, lastName: String, password: String, phone: String?, captchaToken: String?) = Result.failure<User>(Exception())
                    override suspend fun registerSocietaUser(email: String, companyName: String, vatNumber: String, password: String, phone: String?, captchaToken: String?, documentPhotos: List<String>) = Result.failure<User>(Exception())
                    override suspend fun getMe() = Result.failure<User>(Exception())
                    override suspend fun updateMe(user: User) = Result.success(user)
                    override suspend fun uploadDocument(fileBytes: ByteArray, filename: String) = Result.success("mock")
                    override suspend fun getAllCompanies() = Result.success(emptyList<User>())
                    override suspend fun blockCompany(id: String) = Result.success(Unit)
                    override suspend fun unblockCompany(id: String) = Result.success(Unit)
                    override suspend fun deleteAccount(userId: String) = Result.success(Unit)
                    override suspend fun uploadAvatar(userId: String, imageBytes: ByteArray, mimeType: String, fileName: String) = Result.success(User(email="mock@travel.com"))
                }
            )
        }
        SecurityScreen(
            user = User(
                email = "johnkinggraphics@gmail.com",
                userType = "VIAGGIATORE",
                phone = "6895312",
                name = "Charlotte king"
            ),
            viewModel = mockViewModel,
            onBack = {},
            onSaveSuccess = {}
        )
    }
}
