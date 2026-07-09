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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.travel.app.domain.model.User
import com.travel.app.presentation.components.HeaderBackButton
import com.travel.app.presentation.components.HeaderConfirmButton
import com.travel.app.presentation.theme.TravelTheme
import android.net.Uri
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    user: User?,
    viewModel: EditProfileViewModel,
    onBack: () -> Unit,
    onSaveSuccess: (User) -> Unit,
    onDeactivated: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val initialUser = user ?: User(
        email = "johnkinggraphics@gmail.com",
        userType = "VIAGGIATORE",
        phone = "6895312",
        name = "Charlotte king",
        password = "password123"
    )

    LaunchedEffect(initialUser) {
        viewModel.initialize(initialUser)
    }

    val isSocieta = initialUser.userType == "SOCIETA"
    var showConfirmationDialog by remember { mutableStateOf(false) }
    var showDeactivateDialog by remember { mutableStateOf(false) }
    var showAvatarBottomSheet by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var cameraUri by remember { mutableStateOf<Uri?>(null) }
    
    val isNameChanged = remember(initialUser.name, viewModel.name) {
        viewModel.name.trim() != initialUser.name.orEmpty().trim()
    }

    // Country code prefix and phone number (read-only)
    val phoneNum = initialUser.phone.orEmpty()
    val selectedCountryPrefix = "+39" 

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { viewModel.uploadAvatar(context, it, onSaveSuccess) }
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            cameraUri?.let { viewModel.uploadAvatar(context, it, onSaveSuccess) }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            val file = File(context.cacheDir, "images")
            file.mkdirs()
            val tempFile = File.createTempFile("avatar_", ".jpg", file)
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", tempFile)
            cameraUri = uri
            cameraLauncher.launch(uri)
        } else {
            Toast.makeText(context, "Permesso fotocamera negato", Toast.LENGTH_SHORT).show()
        }
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    if (showAvatarBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAvatarBottomSheet = false },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Cambia foto profilo", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showAvatarBottomSheet = false
                            galleryLauncher.launch("image/*")
                        }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Scegli dalla galleria")
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showAvatarBottomSheet = false
                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                                val file = File(context.cacheDir, "images")
                                file.mkdirs()
                                val tempFile = File.createTempFile("avatar_", ".jpg", file)
                                val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", tempFile)
                                cameraUri = uri
                                cameraLauncher.launch(uri)
                            } else {
                                permissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Scatta foto")
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    val focusManager = LocalFocusManager.current

    Box(
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                focusManager.clearFocus()
            }
    ) {
        EditProfileForm(
            initialUser = initialUser,
            name = viewModel.name,
            onNameChange = { viewModel.name = it },
            email = viewModel.email,
            vatNumber = viewModel.vatNumber,
            onVatNumberChange = { viewModel.vatNumber = it },
            selectedCountryPrefix = selectedCountryPrefix,
            phoneNum = phoneNum,
            isLoading = viewModel.isLoading,
            errorMessage = viewModel.errorMessage,
            onBack = onBack,
            onSaveClick = {
                if (isNameChanged) {
                    showConfirmationDialog = true
                } else {
                    viewModel.saveProfile(onSaveSuccess)
                }
            },
            onAvatarClick = { showAvatarBottomSheet = true },
            onDeactivateClick = { showDeactivateDialog = true }
        )

        if (showConfirmationDialog) {
            AlertDialog(
                onDismissRequest = { showConfirmationDialog = false },
                title = { Text(text = "Conferma modifica") },
                text = {
                    val message = if (isSocieta) {
                        "Sei sicuro di voler modificare la ragione sociale in \"${viewModel.name}\"?"
                    } else {
                        "Sei sicuro di voler modificare il tuo nome e cognome in \"${viewModel.name}\"?"
                    }
                    Text(text = message)
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showConfirmationDialog = false
                            viewModel.saveProfile(onSaveSuccess)
                        }
                    ) {
                        Text(text = "Conferma", color = MaterialTheme.colorScheme.primary)
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

        if (showDeactivateDialog) {
            AlertDialog(
                onDismissRequest = { showDeactivateDialog = false },
                title = { Text(text = "Disattiva account") },
                text = {
                    Text(text = "Sei sicuro di voler disattivare il tuo account? Questa azione è irreversibile.")
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDeactivateDialog = false
                            viewModel.deactivateAccount(onDeactivated)
                        }
                    ) {
                        Text(text = "Disattiva", color = Color(0xFFDC2626))
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showDeactivateDialog = false }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileForm(
    initialUser: User,
    name: String,
    onNameChange: (String) -> Unit,
    email: String,
    vatNumber: String,
    onVatNumberChange: (String) -> Unit,
    selectedCountryPrefix: String,
    phoneNum: String,
    isLoading: Boolean,
    errorMessage: String?,
    onBack: () -> Unit,
    onSaveClick: () -> Unit,
    onAvatarClick: () -> Unit,
    onDeactivateClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isSocieta = initialUser.userType == "SOCIETA"
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    LaunchedEffect(scrollState.isScrollInProgress) {
        if (scrollState.isScrollInProgress) {
            focusManager.clearFocus()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background) // Use theme background
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
            HeaderBackButton(
                onClick = onBack,
                enabled = !isLoading
            )

            Text(
                text = "Modifica Profilo",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )

            HeaderConfirmButton(
                onClick = onSaveClick,
                enabled = !isLoading,
                isLoading = isLoading
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
                    .padding(bottom = 8.dp)
                    .clickable { onAvatarClick() },
                contentAlignment = Alignment.BottomEnd
            ) {
                // Main profile avatar container
                if (!initialUser.avatarUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = initialUser.avatarUrl,
                        contentDescription = "User Avatar",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
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
                }

                // Camera icon overlay
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.dp, Color(0xFFE2E8F0), CircleShape),
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
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.align(Alignment.Start).padding(top = 8.dp)
            )

            if (!isSocieta) {
                ProfileInputField(
                    label = "Nome e Cognome",
                    value = name,
                    onValueChange = onNameChange
                )
            }

            // Username field removed

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
                    onValueChange = {},
                    enabled = false
                )
            }

            // Phone Number field with Country prefix selector (Disabled/Read-only)
            // Le agenzie non hanno un numero di telefono associato
            if (!isSocieta) {
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
            }

            Spacer(modifier = Modifier.height(16.dp))

            // SECTION 4: ACTION DISATTIVA
            OutlinedButton(
                onClick = onDeactivateClick,
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
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            enabled = enabled,
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                disabledBorderColor = MaterialTheme.colorScheme.outlineVariant,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
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
                    override suspend fun registerSocietaUser(email: String, companyName: String, vatNumber: String, password: String, phone: String?, captchaToken: String?, documentPhotos: List<String>) = Result.failure<User>(Exception())
                    override suspend fun getMe() = Result.failure<User>(Exception())
                    override suspend fun updateMe(user: User) = Result.success(user)
                    override suspend fun deleteAccount(userId: String) = Result.success(Unit)
                    override suspend fun uploadAvatar(userId: String, imageBytes: ByteArray, mimeType: String, fileName: String) = Result.success(User(email="mock@travel.com"))
                    override suspend fun uploadDocument(fileBytes: ByteArray, filename: String) = Result.success("mock")
                    override suspend fun getAllCompanies() = Result.success(emptyList<User>())
                    override suspend fun blockCompany(id: String) = Result.success(Unit)
                    override suspend fun unblockCompany(id: String) = Result.success(Unit)
                }
            )
        }
        EditProfileScreen(
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
                    override suspend fun registerSocietaUser(email: String, companyName: String, vatNumber: String, password: String, phone: String?, captchaToken: String?, documentPhotos: List<String>) = Result.failure<User>(Exception())
                    override suspend fun getMe() = Result.failure<User>(Exception())
                    override suspend fun updateMe(user: User) = Result.success(user)
                    override suspend fun deleteAccount(userId: String) = Result.success(Unit)
                    override suspend fun uploadAvatar(userId: String, imageBytes: ByteArray, mimeType: String, fileName: String) = Result.success(User(email="mock@travel.com"))
                    override suspend fun uploadDocument(fileBytes: ByteArray, filename: String) = Result.success("mock")
                    override suspend fun getAllCompanies() = Result.success(emptyList<User>())
                    override suspend fun blockCompany(id: String) = Result.success(Unit)
                    override suspend fun unblockCompany(id: String) = Result.success(Unit)
                }
            )
        }
        EditProfileScreen(
            user = User(
                email = "societa@travel.com",
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
