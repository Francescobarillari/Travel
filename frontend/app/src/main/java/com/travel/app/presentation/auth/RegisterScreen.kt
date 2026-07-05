package com.travel.app.presentation.auth

import androidx.compose.animation.Crossfade
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
import androidx.compose.runtime.*
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
import com.travel.app.presentation.components.auth.ReCaptchaDialog
import com.travel.app.presentation.theme.TravelTheme
import com.travel.app.service.ApiService
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import android.content.Context

@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel,
    onNavigateToLogin: () -> Unit,
    onRegisterSuccess: (User) -> Unit,
) {
    var showCaptcha by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val contentResolver = context.contentResolver
                val inputStream = contentResolver.openInputStream(uri)
                val bytes = inputStream?.readBytes()
                val filename = getFileName(context, uri) ?: "document.jpg"
                if (bytes != null) {
                    viewModel.uploadDocumentFile(bytes, filename)
                }
            } catch (e: Exception) {
                viewModel.registerError = "Errore durante la lettura del file: ${e.localizedMessage}"
            }
        }
    }

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
                        Text("Crea il tuo account", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)

                        //tipo Utente
                        UserTypeSelectorRow(
                            selected = viewModel.registerUserType,
                            onSelect = { viewModel.registerUserType = it }
                        )

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                        // Transizione fluida dei campi senza sbalzi di altezza
                        Crossfade(
                            targetState = viewModel.registerUserType,
                            label = "UserTypeFieldsTransition"
                        ) { userType ->
                            when (userType) {
                                UserType.VIAGGIATORE -> {
                                    ViaggiatoreFields(
                                        firstName = viewModel.registerFirstName,
                                        onFirstNameChange = { viewModel.registerFirstName = it },
                                        lastName = viewModel.registerLastName,
                                        onLastNameChange = { viewModel.registerLastName = it },
                                    )
                                }
                                UserType.SOCIETA -> {
                                    SocietaFields(
                                        companyName = viewModel.registerCompanyName,
                                        onCompanyNameChange = { viewModel.registerCompanyName = it },
                                        vatNumber = viewModel.registerVatNumber,
                                        onVatNumberChange = { viewModel.registerVatNumber = it },
                                        documentPhotos = viewModel.registerDocumentPhotos,
                                        isUploading = viewModel.isUploadingDocument,
                                        onAddDocumentClick = { filePickerLauncher.launch("image/*") }
                                    )
                                }
                            }
                        }

                        // Campi in comune
                        TravelTextField(value = viewModel.registerEmail, onValueChange = { viewModel.registerEmail = it }, label = "Indirizzo Email *", leadingIcon = Icons.Default.Email, keyboardType = KeyboardType.Email)
                        TravelTextField(value = viewModel.registerPhone, onValueChange = { viewModel.registerPhone = it }, label = "Telefono (opzionale)", leadingIcon = Icons.Default.Phone, keyboardType = KeyboardType.Phone)
                        PasswordField(value = viewModel.registerPassword, onValueChange = { viewModel.registerPassword = it }, label = "Password *")
                        PasswordField(value = viewModel.registerConfirmPassword, onValueChange = { viewModel.registerConfirmPassword = it }, label = "Conferma Password *")

                        viewModel.registerError?.let { ErrorBanner(message = it) }

                        Button(
                            onClick = {
                                if (viewModel.registerEmail.isBlank() || viewModel.registerPassword.isBlank()) {
                                    viewModel.registerError = "Email e password sono obbligatorie"
                                } else if (viewModel.registerPassword != viewModel.registerConfirmPassword) {
                                    viewModel.registerError = "Le password non coincidono"
                                } else if (viewModel.registerUserType == UserType.VIAGGIATORE && (viewModel.registerFirstName.isBlank() || viewModel.registerLastName.isBlank())) {
                                    viewModel.registerError = "Nome e cognome sono obbligatori"
                                } else if (viewModel.registerUserType == UserType.SOCIETA && (viewModel.registerCompanyName.isBlank() || viewModel.registerVatNumber.isBlank())) {
                                    viewModel.registerError = "Ragione sociale e Partita IVA sono obbligatorie"
                                } else if (viewModel.registerUserType == UserType.SOCIETA && viewModel.registerDocumentPhotos.isEmpty()) {
                                    viewModel.registerError = "È necessario caricare almeno un documento per la verifica"
                                } else {
                                    showCaptcha = true
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(52.dp).padding(top = 8.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = Color.White),
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
                        Text("Accedi", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                    }
                }
            }

            if (showCaptcha) {
                ReCaptchaDialog(
                    onDismiss = { showCaptcha = false },
                    onSuccess = { token ->
                        showCaptcha = false
                        viewModel.register(token, onRegisterSuccess)
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun RegisterScreenPreview() {
    val mockApiService = object : ApiService {
        override suspend fun login(request: it.unical.ea.dtos.authDto.LoginRequest) = it.unical.ea.dtos.authDto.JwtResponse("mock_token", "mock_refresh")
        override suspend fun register(request: it.unical.ea.dtos.authDto.SignupRequest) = "mock_user_id"
        override suspend fun getMe() = it.unical.ea.dtos.user.UserDTO().apply { email = "test@travel.com" }
        override suspend fun updateMe(request: it.unical.ea.dtos.user.UserDTO) = request
        override suspend fun createActivity(request: it.unical.ea.dtos.activity.ActivityDto) = request
        override suspend fun getActivities() = emptyList<it.unical.ea.dtos.activity.ActivityDto>()
        override suspend fun searchActivities(query: String, minPrice: Double?, maxPrice: Double?, page: Int, size: Int) = it.unical.ea.dtos.common.PageDto<it.unical.ea.dtos.activity.ActivityDto>()
        override suspend fun searchLocalita(query: String, page: Int, size: Int) = it.unical.ea.dtos.common.PageDto<it.unical.ea.dtos.localita.LocalitaDto>()
        override suspend fun getLocalitaById(id: String) = it.unical.ea.dtos.localita.LocalitaDto()
        override suspend fun getActivityById(id: String) = it.unical.ea.dtos.activity.ActivityDto()
        override suspend fun getItineraries() = emptyList<it.unical.ea.dtos.itinerary.ItineraryDto>()
        override suspend fun createItinerary(request: it.unical.ea.dtos.itinerary.CreateItineraryRequest) = it.unical.ea.dtos.itinerary.ItineraryDto()
        override suspend fun deleteItinerary(id: String) {}
        override suspend fun uploadDocument(file: okhttp3.MultipartBody.Part) = "mock_document_path"
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
        RegisterScreen(
            viewModel = RegisterViewModel(UserRepositoryImpl(mockApiService) { error("Not used in preview") }),
            onNavigateToLogin = {},
            onRegisterSuccess = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun RegisterSocietaScreenPreview() {
    val mockApiService = object : ApiService {
        override suspend fun login(request: it.unical.ea.dtos.authDto.LoginRequest) = it.unical.ea.dtos.authDto.JwtResponse("mock_token", "mock_refresh")
        override suspend fun register(request: it.unical.ea.dtos.authDto.SignupRequest) = "mock_user_id"
        override suspend fun getMe() = it.unical.ea.dtos.user.UserDTO().apply { email = "test@travel.com" }
        override suspend fun updateMe(request: it.unical.ea.dtos.user.UserDTO) = request
        override suspend fun createActivity(request: it.unical.ea.dtos.activity.ActivityDto) = request
        override suspend fun getActivities() = emptyList<it.unical.ea.dtos.activity.ActivityDto>()
        override suspend fun searchActivities(query: String, minPrice: Double?, maxPrice: Double?, page: Int, size: Int) = it.unical.ea.dtos.common.PageDto<it.unical.ea.dtos.activity.ActivityDto>()
        override suspend fun searchLocalita(query: String, page: Int, size: Int) = it.unical.ea.dtos.common.PageDto<it.unical.ea.dtos.localita.LocalitaDto>()
        override suspend fun getLocalitaById(id: String) = it.unical.ea.dtos.localita.LocalitaDto()
        override suspend fun getActivityById(id: String) = it.unical.ea.dtos.activity.ActivityDto()
        override suspend fun getItineraries() = emptyList<it.unical.ea.dtos.itinerary.ItineraryDto>()
        override suspend fun createItinerary(request: it.unical.ea.dtos.itinerary.CreateItineraryRequest) = it.unical.ea.dtos.itinerary.ItineraryDto()
        override suspend fun deleteItinerary(id: String) {}
        override suspend fun uploadDocument(file: okhttp3.MultipartBody.Part) = "mock_document_path"
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
    val viewModel = RegisterViewModel(UserRepositoryImpl(mockApiService) { error("Not used in preview") }).apply {
        registerUserType = UserType.SOCIETA
        registerDocumentPhotos = listOf("companies/documents/visura_camerale.jpg")
    }
    TravelTheme {
        RegisterScreen(
            viewModel = viewModel,
            onNavigateToLogin = {},
            onRegisterSuccess = {}
        )
    }
}

private fun getFileName(context: Context, uri: Uri): String? {
    var name: String? = null
    if (uri.scheme == "content") {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val index = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (index != -1) {
                    name = it.getString(index)
                }
            }
        }
    }
    if (name == null) {
        name = uri.path
        val cut = name?.lastIndexOf('/')
        if (cut != null && cut != -1) {
            name = name?.substring(cut + 1)
        }
    }
    return name
}
