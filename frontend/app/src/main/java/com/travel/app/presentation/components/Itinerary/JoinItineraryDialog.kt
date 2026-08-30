package com.travel.app.presentation.components.itinerary

import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Key
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.travel.app.data.AppContainer
import it.unical.ea.dtos.itinerary.ItineraryJoinRequestDto
import kotlinx.coroutines.launch

@Composable
fun JoinItineraryDialog(
    onDismiss: () -> Unit,
    onJoinSuccess: (ItineraryJoinRequestDto) -> Unit = {}
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()

    var shareCode by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun submitCode() {
        val trimmedCode = shareCode.trim().uppercase()
        if (trimmedCode.isBlank()) {
            errorMessage = "Inserisci un codice valido"
            return
        }
        isLoading = true
        errorMessage = null
        focusManager.clearFocus()

        scope.launch {
            val res = AppContainer.itineraryRepository.joinItineraryByCode(trimmedCode)
            isLoading = false
            res.fold(
                onSuccess = { req ->
                    Toast.makeText(
                        context,
                        "Richiesta di partecipazione inviata all'organizzatore!",
                        Toast.LENGTH_LONG
                    ).show()
                    onJoinSuccess(req)
                    onDismiss()
                },
                onFailure = { err ->
                    val msg = when {
                        err.message?.contains("codeNotFound") == true || err.message?.contains("404") == true ->
                            "Codice itinerario non valido o inesistente."
                        err.message?.contains("isCreator") == true ->
                            "Sei già l'organizzatore di questo itinerario."
                        err.message?.contains("alreadyMember") == true ->
                            "Sei già un partecipante accettato per questo itinerario!"
                        err.message?.contains("alreadyPending") == true ->
                            "Hai già inviato una richiesta di partecipazione in attesa di approvazione."
                        err.message?.contains("notShared") == true ->
                            "Questo itinerario non è più condivisibile."
                        else -> err.message ?: "Errore durante l'invio della richiesta."
                    }
                    errorMessage = msg
                }
            )
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Icon
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.GroupAdd,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Title and subtitle
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Unisciti con Codice",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Inserisci il codice di condivisione fornito dall'organizzatore per inviare la richiesta di partecipazione.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }

                // Code Input Field
                OutlinedTextField(
                    value = shareCode,
                    onValueChange = { newValue ->
                        if (newValue.length <= 10) {
                            shareCode = newValue.uppercase().filter { it.isLetterOrDigit() }
                            errorMessage = null
                        }
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Key,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                if (clipboard != null && clipboard.hasPrimaryClip()) {
                                    val item = clipboard.primaryClip?.getItemAt(0)
                                    val text = item?.text?.toString()?.trim()?.uppercase()
                                    if (!text.isNullOrBlank()) {
                                        shareCode = text.filter { it.isLetterOrDigit() }.take(10)
                                        errorMessage = null
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentPaste,
                                contentDescription = "Incolla",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    placeholder = {
                        Text(
                            text = "Es. TRV8K2",
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    },
                    singleLine = true,
                    isError = errorMessage != null,
                    textStyle = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        textAlign = TextAlign.Center
                    ),
                    shape = RoundedCornerShape(14.dp),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Characters,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { submitCode() }
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Error Message if any
                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Actions Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isLoading
                    ) {
                        Text("Annulla")
                    }

                    Button(
                        onClick = { submitCode() },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isLoading && shareCode.isNotBlank()
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                "Invia",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
