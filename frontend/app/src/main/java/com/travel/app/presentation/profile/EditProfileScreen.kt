package com.travel.app.presentation.profile

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.travel.app.domain.model.User
import com.travel.app.presentation.theme.TravelTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    user: User?,
    viewModel: EditProfileViewModel,
    onBack: () -> Unit,
    onSaveSuccess: (User) -> Unit,
    modifier: Modifier = Modifier
) {
    val initialUser = user ?: User(
        email = "johnkinggraphics@gmail.com",
        username = "johnkinggraphics",
        userType = "VIAGGIATORE",
        phone = "6895312",
        name = "Charlotte king",
        password = "password123"
    )

    LaunchedEffect(initialUser) {
        viewModel.initialize(initialUser)
    }

    val isSocieta = initialUser.userType == "SOCIETA"

    // Country code prefix and phone number (read-only)
    val phoneNum = initialUser.phone.orEmpty()
    val selectedCountryPrefix = "+39" 

    EditProfileForm(
        initialUser = initialUser,
        name = viewModel.name,
        onNameChange = { viewModel.name = it },
        email = viewModel.email,
        username = viewModel.username,
        onUsernameChange = { viewModel.username = it },
        vatNumber = viewModel.vatNumber,
        onVatNumberChange = { viewModel.vatNumber = it },
        selectedCountryPrefix = selectedCountryPrefix,
        phoneNum = phoneNum,
        isLoading = viewModel.isLoading,
        errorMessage = viewModel.errorMessage,
        onBack = onBack,
        onSaveClick = {
            viewModel.saveProfile(onSaveSuccess)
        },
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileForm(
    initialUser: User,
    name: String,
    onNameChange: (String) -> Unit,
    email: String,
    username: String,
    onUsernameChange: (String) -> Unit,
    vatNumber: String,
    onVatNumberChange: (String) -> Unit,
    selectedCountryPrefix: String,
    phoneNum: String,
    isLoading: Boolean,
    errorMessage: String?,
    onBack: () -> Unit,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isSocieta = initialUser.userType == "SOCIETA"
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC)) // Consistent soft slate-grey background
            .statusBarsPadding()
    ) {
        // TOP NAVIGATION HEADER (consistent style)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.White, CircleShape)
                    .border(1.dp, Color(0xFFE2E8F0), CircleShape),
                enabled = !isLoading
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Indietro",
                    tint = Color(0xFF334155),
                    modifier = Modifier.size(20.dp)
                )
            }

            Text(
                text = "Modifica Profilo",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF0F172A)
            )

            IconButton(
                onClick = onSaveClick,
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.White, CircleShape)
                    .border(1.dp, Color(0xFFE2E8F0), CircleShape),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color(0xFF22C55E),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Salva",
                        tint = Color(0xFF22C55E),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Mostra un banner di errore se presente
            errorMessage?.let { msg ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = msg,
                        color = Color(0xFFDC2626),
                        modifier = Modifier.padding(12.dp),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            // AVATAR AREA WITH DYNAMIC INITIALS AND CAMERA OVERLAY
            val initials = getInitials(name)
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .padding(bottom = 8.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                // Main profile initials container (cohesive gradient)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0xFF8FA4A6), Color(0xFF6B7F82))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initials,
                        color = Color.White,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Camera icon overlay
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.dp, Color(0xFFE2E8F0), CircleShape)
                        .clickable { /* Photo upload logic */ },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoCamera,
                        contentDescription = "Cambia foto",
                        tint = Color(0xFF475569),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // SECTION 1: DETTAGLI ACCOUNT
            Text(
                text = "Dettagli Account",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A),
                modifier = Modifier.align(Alignment.Start).padding(top = 8.dp)
            )

            ProfileInputField(
                label = if (isSocieta) "Nome Società" else "Nome e Cognome",
                value = name,
                onValueChange = onNameChange
            )

            ProfileInputField(
                label = if (isSocieta) "Username Azienda" else "Username",
                value = username,
                onValueChange = onUsernameChange
            )

            ProfileInputField(
                label = "Indirizzo Email",
                value = email,
                onValueChange = {},
                enabled = false
            )

            if (isSocieta) {
                ProfileInputField(
                    label = "Partita IVA",
                    value = vatNumber,
                    onValueChange = onVatNumberChange
                )
            }

            // Phone Number field with Country prefix selector (Disabled/Read-only)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Numero di Telefono",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF334155)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box {
                        Row(
                            modifier = Modifier
                                .height(56.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFFF1F5F9))
                                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
                                .padding(horizontal = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = selectedCountryPrefix,
                                fontSize = 15.sp,
                                color = Color(0xFF64748B),
                                fontWeight = FontWeight.Medium
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = Color(0xFF94A3B8)
                            )
                        }
                    }

                    OutlinedTextField(
                        value = phoneNum,
                        onValueChange = {},
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        enabled = false,
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledContainerColor = Color(0xFFF1F5F9),
                            disabledBorderColor = Color(0xFFE2E8F0),
                            disabledTextColor = Color(0xFF64748B)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action: Deactivate Account (outlined button style)
            OutlinedButton(
                onClick = { Toast.makeText(context, "Funzionalità non attiva", Toast.LENGTH_SHORT).show() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDC2626)),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFFDC2626),
                    containerColor = Color.Transparent
                )
            ) {
                Text(
                    text = "Disattiva Account",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
fun ProfileInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = label,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF334155) // Slate 700
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            enabled = enabled,
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                disabledContainerColor = Color(0xFFF1F5F9), // Soft slate for disabled
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = Color(0xFFE2E8F0),
                disabledBorderColor = Color(0xFFE2E8F0),
                focusedTextColor = Color(0xFF0F172A),
                unfocusedTextColor = Color(0xFF334155),
                disabledTextColor = Color(0xFF64748B)
            )
        )
    }
}

fun getInitials(name: String?): String {
    if (name.isNullOrBlank()) return "U"
    val parts = name.trim().split("\\s+".toRegex())
    return when {
        parts.size >= 2 -> "${parts[0].first().uppercase()}${parts[1].first().uppercase()}"
        parts.isNotEmpty() && parts[0].isNotEmpty() -> parts[0].first().uppercase().toString()
        else -> "U"
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun EditProfileScreenViaggiatorePreview() {
    TravelTheme {
        val mockViewModel = remember {
            EditProfileViewModel(
                userRepository = object : com.travel.app.domain.repository.UserRepository {
                    override fun getSessionUser(): User? = null
                    override fun saveSession(user: User, token: String) {}
                    override fun logout() {}
                    override suspend fun login(email: String, password: String, captchaToken: String?) = Result.failure<User>(Exception())
                    override suspend fun registerViaggiatoreUser(email: String, firstName: String, lastName: String, password: String, phone: String?, captchaToken: String?) = Result.failure<User>(Exception())
                    override suspend fun registerSocietaUser(email: String, companyName: String, vatNumber: String, password: String, phone: String?, captchaToken: String?) = Result.failure<User>(Exception())
                    override suspend fun getMe() = Result.failure<User>(Exception())
                    override suspend fun updateMe(user: User) = Result.success(user)
                }
            )
        }
        EditProfileScreen(
            user = User(
                email = "johnkinggraphics@gmail.com",
                username = "johnkinggraphics",
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

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun EditProfileScreenSocietaPreview() {
    TravelTheme {
        val mockViewModel = remember {
            EditProfileViewModel(
                userRepository = object : com.travel.app.domain.repository.UserRepository {
                    override fun getSessionUser(): User? = null
                    override fun saveSession(user: User, token: String) {}
                    override fun logout() {}
                    override suspend fun login(email: String, password: String, captchaToken: String?) = Result.failure<User>(Exception())
                    override suspend fun registerViaggiatoreUser(email: String, firstName: String, lastName: String, password: String, phone: String?, captchaToken: String?) = Result.failure<User>(Exception())
                    override suspend fun registerSocietaUser(email: String, companyName: String, vatNumber: String, password: String, phone: String?, captchaToken: String?) = Result.failure<User>(Exception())
                    override suspend fun getMe() = Result.failure<User>(Exception())
                    override suspend fun updateMe(user: User) = Result.success(user)
                }
            )
        }
        EditProfileScreen(
            user = User(
                email = "societa@travel.com",
                username = "travel_agency",
                userType = "SOCIETA",
                phone = "081765432",
                name = "Agenzia Viaggi Italia S.r.l.",
                vatNumber = "01234567890"
            ),
            viewModel = mockViewModel,
            onBack = {},
            onSaveSuccess = {}
        )
    }
}
