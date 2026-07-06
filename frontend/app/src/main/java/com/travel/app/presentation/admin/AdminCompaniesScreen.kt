package com.travel.app.presentation.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.travel.app.BuildConfig
import com.travel.app.data.AppContainer
import com.travel.app.domain.model.User
import com.travel.app.presentation.admin.components.EmptyPlaceholder
import com.travel.app.presentation.admin.components.CompanyManagementCard
import com.travel.app.presentation.admin.components.ZoomableImageDialog

@Composable
fun AdminCompaniesScreen(
    viewModel: AdminViewModel
) {
    var filterState by remember { mutableStateOf(0) } // 0 = Approvate, 1 = Bloccate
    var activeImageForZoom by remember { mutableStateOf<String?>(null) }
    var companyToBlock by remember { mutableStateOf<User?>(null) }
    var companyToUnblock by remember { mutableStateOf<User?>(null) }

    val isPreview = androidx.compose.ui.platform.LocalInspectionMode.current
    LaunchedEffect(Unit) {
        if (!isPreview) {
            viewModel.loadData()
        }
    }

    val filteredList = remember(viewModel.allCompanies, filterState) {
        viewModel.allCompanies.filter { company ->
            when (filterState) {
                0 -> company.approved && !company.blocked
                1 -> company.blocked
                else -> true
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(bottom = 80.dp) // Lascia spazio per la navbar fluttuante
    ) {
        // Top Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Gestione Società",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                // Skyscanner-style Segments
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val segments = listOf("Approvate", "Bloccate")
                    segments.forEachIndexed { index, text ->
                        val isSelected = filterState == index
                        val bg = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        val tc = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .clip(RoundedCornerShape(19.dp))
                                .background(bg)
                                .clickable { filterState = index },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = text,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = tc
                            )
                        }
                    }
                }
            }
        }

        // Content List
        Box(modifier = Modifier.fillMaxSize()) {
            if (viewModel.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.primary
                )
            } else if (filteredList.isEmpty()) {
                val subtitleText = when (filterState) {
                    0 -> "Nessuna società è ancora stata approvata."
                    1 -> "Nessuna società risulta bloccata."
                    else -> ""
                }
                EmptyPlaceholder(
                    icon = Icons.Default.Business,
                    title = "Nessuna società trovata",
                    subtitle = subtitleText
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(filteredList) { company ->
                        CompanyManagementCard(
                            company = company,
                            onApprove = { company.id?.let { viewModel.approveCompany(it) } },
                            onReject = { company.id?.let { viewModel.rejectCompany(it) } },
                             onBlock = { companyToBlock = company },
                             onUnblock = { companyToUnblock = company },
                             onImageClick = { activeImageForZoom = it }
                        )
                    }
                }
            }
        }
    }

    // Zoom Dialog per visualizzare i documenti a schermo intero
    activeImageForZoom?.let { path ->
        ZoomableImageDialog(
            imagePath = path,
            onDismiss = { activeImageForZoom = null }
        )
    }

    // Dialog di conferma blocco
    companyToBlock?.let { company ->
        AlertDialog(
            onDismissRequest = { companyToBlock = null },
            title = { Text("Conferma Blocco") },
            text = { Text("Sei sicuro di voler bloccare la società \"${company.name ?: "società"}\"? Non potrà più accedere all'applicazione o pubblicare attività.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        company.id?.let { viewModel.blockCompany(it) }
                        companyToBlock = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Blocca")
                }
            },
            dismissButton = {
                TextButton(onClick = { companyToBlock = null }) {
                    Text("Annulla")
                }
            }
        )
    }

    // Dialog di conferma sblocco
    companyToUnblock?.let { company ->
        AlertDialog(
            onDismissRequest = { companyToUnblock = null },
            title = { Text("Conferma Sblocco") },
            text = { Text("Sei sicuro di voler sbloccare la società \"${company.name ?: "società"}\"? Verrà ripristinato il suo accesso e la visibilità delle sue attività.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        company.id?.let { viewModel.unblockCompany(it) }
                        companyToUnblock = null
                    }
                ) {
                    Text("Sblocca")
                }
            },
            dismissButton = {
                TextButton(onClick = { companyToUnblock = null }) {
                    Text("Annulla")
                }
            }
        )
    }
}
