package com.travel.app.presentation.admin.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Visibility
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
import it.unical.ea.dtos.user.UserDTO

@Composable
fun CompanyCard(
    company: UserDTO,
    onApprove: () -> Unit,
    onReject: () -> Unit,
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = company.companyName ?: "Ragione Sociale Non Specificata",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Surface(
                    color = Color(0xFFFFF3E0),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "DA APPROVARE",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE65100)
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // Info Grid
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                InfoRow(icon = Icons.Default.Email, label = "Email:", value = company.email ?: "-")
                InfoRow(icon = Icons.Default.Badge, label = "Partita IVA:", value = company.vatNumber ?: "-")
                InfoRow(icon = Icons.Default.Phone, label = "Telefono:", value = company.phone ?: "-")
            }

            // Sezione Foto Documenti
            if (!company.documentPhotos.isNullOrEmpty()) {
                Text(
                    text = "Documenti caricati:",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    company.documentPhotos.forEachIndexed { index, path ->
                        AssistChip(
                            onClick = { onImageClick(path) },
                            label = { Text("Documento ${index + 1}") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Visibility,
                                    contentDescription = "Visualizza documento",
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                labelColor = MaterialTheme.colorScheme.primary,
                                leadingIconContentColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
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
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = Color.White)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Approva", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}
