package com.travel.app.presentation.profile

import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.travel.app.R
import com.travel.app.domain.model.User
import com.travel.app.presentation.theme.TravelTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    user: User?,
    onBack: () -> Unit,
    onSave: suspend (User) -> Result<User>,
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

    val isSocieta = initialUser.userType == "SOCIETA"

    // Form states
    var name by remember { mutableStateOf(initialUser.name.orEmpty()) }
    val email by remember { mutableStateOf(initialUser.email) }
    var username by remember { mutableStateOf(initialUser.username) }
    var password by remember { mutableStateOf(initialUser.password.orEmpty()) }
    var phoneNum by remember { mutableStateOf(initialUser.phone.orEmpty()) }
    var vatNumber by remember { mutableStateOf(initialUser.vatNumber.orEmpty()) }
    
    // Country code prefix state
    var selectedCountryPrefix by remember { mutableStateOf("+39") }
    var isPrefixDropdownExpanded by remember { mutableStateOf(false) }

    var isPasswordVisible by remember { mutableStateOf(false) }
    
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    EditProfileForm(
        initialUser = initialUser,
        name = name,
        onNameChange = { name = it },
        email = email,
        username = username,
        onUsernameChange = { username = it },
        password = password,
        onPasswordChange = { password = it },
        phoneNum = phoneNum,
        onPhoneNumChange = { phoneNum = it },
        vatNumber = vatNumber,
        onVatNumberChange = { vatNumber = it },
        selectedCountryPrefix = selectedCountryPrefix,
        onSelectedCountryPrefixChange = { selectedCountryPrefix = it },
        isPrefixDropdownExpanded = isPrefixDropdownExpanded,
        onPrefixDropdownExpandedChange = { isPrefixDropdownExpanded = it },
        isPasswordVisible = isPasswordVisible,
        onPasswordVisibleChange = { isPasswordVisible = it },
        isLoading = isLoading,
        errorMessage = errorMessage,
        onBack = onBack,
        onSaveClick = {
            val updatedUser = initialUser.copy(
                name = name,
                username = username,
                password = password,
                phone = phoneNum,
                vatNumber = if (isSocieta) vatNumber else null
            )
            coroutineScope.launch {
                isLoading = true
                errorMessage = null
                onSave(updatedUser).fold(
                    onSuccess = {
                        isLoading = false
                    },
                    onFailure = { error ->
                        isLoading = false
                        errorMessage = error.message ?: "Errore di salvataggio"
                    }
                )
            }
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
    password: String,
    onPasswordChange: (String) -> Unit,
    phoneNum: String,
    onPhoneNumChange: (String) -> Unit,
    vatNumber: String,
    onVatNumberChange: (String) -> Unit,
    selectedCountryPrefix: String,
    onSelectedCountryPrefixChange: (String) -> Unit,
    isPrefixDropdownExpanded: Boolean,
    onPrefixDropdownExpandedChange: (Boolean) -> Unit,
    isPasswordVisible: Boolean,
    onPasswordVisibleChange: (Boolean) -> Unit,
    isLoading: Boolean,
    errorMessage: String?,
    onBack: () -> Unit,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isSocieta = initialUser.userType == "SOCIETA"
    val countryPrefixes = listOf("+39", "+1", "+44", "+33", "+49")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
    ) {
        // TOP NAVIGATION HEADER
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.size(44.dp),
                enabled = !isLoading
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Indietro",
                    tint = Color.Black
                )
            }

            Text(
                text = "Modifica Profilo",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            IconButton(
                onClick = onSaveClick,
                modifier = Modifier.size(44.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color(0xFF22C55E),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Salva",
                        tint = Color(0xFF22C55E)
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
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
            
            // AVATAR AREA WITH CAMERA BADGE
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .padding(bottom = 8.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                // Main profile picture
                Image(
                    painter = painterResource(id = R.drawable.profile_avatar),
                    contentDescription = "Foto profilo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .border(1.dp, Color.LightGray.copy(alpha = 0.5f), CircleShape)
                )

                // Camera icon overlay
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.dp, Color.LightGray.copy(alpha = 0.5f), CircleShape)
                        .clickable { /* Photo upload logic */ },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoCamera,
                        contentDescription = "Cambia foto",
                        tint = Color.Black,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            ProfileInputField(
                label = if (isSocieta) "Nome Società" else "Nome e Cognome",
                value = name,
                onValueChange = onNameChange
            )

            ProfileInputField(
                label = "Indirizzo Email",
                value = email,
                onValueChange = {},
                enabled = false
            )

            ProfileInputField(
                label = if (isSocieta) "Username Azienda" else "Username",
                value = username,
                onValueChange = onUsernameChange
            )

            if (isSocieta) {
                ProfileInputField(
                    label = "Partita IVA",
                    value = vatNumber,
                    onValueChange = onVatNumberChange
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Password",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                TextField(
                    value = password,
                    onValueChange = onPasswordChange,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { onPasswordVisibleChange(!isPasswordVisible) }) {
                            Icon(
                                imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (isPasswordVisible) "Nascondi password" else "Mostra password",
                                tint = Color.Gray
                            )
                        }
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF8FAFC),
                        unfocusedContainerColor = Color(0xFFF8FAFC),
                        disabledContainerColor = Color(0xFFF8FAFC).copy(alpha = 0.6f),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    ),
                    singleLine = true
                )
            }

            // Phone Number field with Country prefix
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Numero di Telefono",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
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
                                .background(Color(0xFFF8FAFC))
                                .clickable { onPrefixDropdownExpandedChange(true) }
                                .padding(horizontal = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = selectedCountryPrefix,
                                fontSize = 15.sp,
                                color = Color.Gray
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Prefissi",
                                tint = Color.Gray
                            )
                        }

                        DropdownMenu(
                            expanded = isPrefixDropdownExpanded,
                            onDismissRequest = { onPrefixDropdownExpandedChange(false) },
                            modifier = Modifier.background(Color.White)
                        ) {
                            countryPrefixes.forEach { prefix ->
                                DropdownMenuItem(
                                    text = { Text(prefix) },
                                    onClick = {
                                        onSelectedCountryPrefixChange(prefix)
                                        onPrefixDropdownExpandedChange(false)
                                    }
                                )
                            }
                        }
                    }

                    TextField(
                        value = phoneNum,
                        onValueChange = onPhoneNumChange,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF8FAFC),
                            unfocusedContainerColor = Color(0xFFF8FAFC),
                            disabledContainerColor = Color(0xFFF8FAFC).copy(alpha = 0.6f),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent
                        ),
                        singleLine = true
                    )
                }
            }

            Spacer(modifier = Modifier.height(60.dp))
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
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            enabled = enabled,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFF8FAFC),
                unfocusedContainerColor = Color(0xFFF8FAFC),
                disabledContainerColor = Color(0xFFF8FAFC).copy(alpha = 0.6f),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                disabledTextColor = Color.Gray.copy(alpha = 0.7f)
            ),
            singleLine = true
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ProfileScreenViaggiatorePreview() {
    TravelTheme {
        ProfileScreen(
            user = User(
                email = "johnkinggraphics@gmail.com",
                username = "johnkinggraphics",
                userType = "VIAGGIATORE",
                phone = "6895312",
                name = "Charlotte king"
            ),
            onBack = {},
            onSave = { Result.success(it) }
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ProfileScreenSocietaPreview() {
    TravelTheme {
        ProfileScreen(
            user = User(
                email = "societa@travel.com",
                username = "travel_agency",
                userType = "SOCIETA",
                phone = "081765432",
                name = "Agenzia Viaggi Italia S.r.l.",
                vatNumber = "01234567890"
            ),
            onBack = {},
            onSave = { Result.success(it) }
        )
    }
}
