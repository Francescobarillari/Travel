package com.travel.app.presentation.admin.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.travel.app.BuildConfig
import com.travel.app.data.AppContainer
import com.travel.app.domain.model.User

@Composable
fun CompanyManagementCard(
    company: User,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    onBlock: () -> Unit,
    onUnblock: () -> Unit,
    onImageClick: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = company.name ?: "Ragione Sociale Non Specificata",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                // Status Badge
                val (badgeBg, badgeText, badgeColor) = when {
                    company.blocked -> Triple(Color(0xFFFFEBEE), "BLOCCATA", Color(0xFFC62828))
                    !company.approved -> Triple(Color(0xFFFFF3E0), "IN ATTESA", Color(0xFFE65100))
                    else -> Triple(Color(0xFFE8F5E9), "APPROVATA", Color(0xFF2E7D32))
                }
                
                Surface(
                    color = badgeBg,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = badgeText,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = badgeColor
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // Grid Details
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                InfoRow(icon = Icons.Default.Email, label = "Email:", value = company.email)
                InfoRow(icon = Icons.Default.Badge, label = "Partita IVA:", value = company.vatNumber ?: "-")
                InfoRow(icon = Icons.Default.Phone, label = "Telefono:", value = company.phone ?: "-")
            }

            // Documents (if any)
            if (company.documentPhotos.isNotEmpty()) {
                Text(
                    text = "Documenti d'identità caricati:",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val token = if (AppContainer.isInitialized) AppContainer.sessionManager.getSessionToken().orEmpty() else ""
                    company.documentPhotos.forEach { path ->
                        val filename = path.substringAfterLast("/")
                        val imgUrl = "${BuildConfig.BACKEND_URL}api/admin/documents/$filename"
                        val imageRequest = ImageRequest.Builder(LocalContext.current)
                            .data(imgUrl)
                            .addHeader("Authorization", "Bearer $token")
                            .crossfade(true)
                            .build()

                        AsyncImage(
                            model = imageRequest,
                            contentDescription = "Documento Società",
                            modifier = Modifier
                                .size(90.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                                .clickable { onImageClick(path) },
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Context Action Buttons based on status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when {
                    company.blocked -> {
                        Button(
                            onClick = onUnblock,
                            modifier = Modifier.fillMaxWidth().height(44.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2E7D32),
                                contentColor = Color.White
                            )
                        ) {
                            Icon(Icons.Default.LockOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Sblocca Società", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                    !company.approved -> {
                        OutlinedButton(
                            onClick = onReject,
                            modifier = Modifier.weight(1f).height(44.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Rifiuta", style = MaterialTheme.typography.labelMedium)
                        }

                        Button(
                            onClick = onApprove,
                            modifier = Modifier.weight(1f).height(44.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = Color.White
                            )
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Approva", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                    else -> {
                        Button(
                            onClick = onBlock,
                            modifier = Modifier.fillMaxWidth().height(44.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = Color.White
                            )
                        ) {
                            Icon(Icons.Default.Block, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Blocca Società", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }
    }
}
