package com.travel.app.presentation.components.review

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.travel.app.domain.model.review.ReviewDto

@Composable
fun ReviewCard(
    review: ReviewDto,
    showActivityName: Boolean = false,
    onUpdate: (rating: Int, comment: String) -> Unit = { _, _ -> },
    onDelete: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isEditing by remember { mutableStateOf(false) }

    if (isEditing) {
        // Mostra il form di modifica inline all'interno della card
        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Modifica la tua recensione", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                AddReviewInline(
                    initialRating = review.rating,
                    initialComment = review.comment ?: "",
                    onSubmit = { rating, comment ->
                        onUpdate(rating, comment)
                        isEditing = false
                    },
                    onCancel = { isEditing = false }
                )
            }
        }
    } else {
        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = review.authorName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        repeat(5) { index ->
                            Icon(
                                imageVector = if (index < review.rating) Icons.Filled.Star else Icons.Outlined.Star,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    if (review.isEditable) {
                        var expanded by remember { mutableStateOf(false) }
                        Box {
                            IconButton(onClick = { expanded = true }) {
                                Icon(Icons.Default.MoreVert, contentDescription = "Opzioni recensione")
                            }
                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Modifica") },
                                    leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                                    onClick = {
                                        expanded = false
                                        isEditing = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Elimina") },
                                    leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                                    onClick = {
                                        expanded = false
                                        onDelete()
                                    }
                                )
                            }
                        }
                    }
                }

                if (showActivityName && review.activityName != null) {
                    Text(
                        text = "Per: ${review.activityName}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                if (!review.comment.isNullOrBlank()) {
                    Text(
                        text = review.comment,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
fun AddReviewInline(
    initialRating: Int = 5,
    initialComment: String = "",
    onSubmit: (rating: Int, comment: String) -> Unit,
    onCancel: (() -> Unit)? = null
) {
    var rating by remember { mutableStateOf(initialRating) }
    var comment by remember { mutableStateOf(initialComment) }
    var isFocused by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Selettore stelle
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            repeat(5) { index ->
                IconButton(
                    onClick = { rating = index + 1 },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (index < rating) Icons.Filled.Star else Icons.Outlined.Star,
                        contentDescription = "Voto ${index + 1}",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }

        OutlinedTextField(
            value = comment,
            onValueChange = { 
                comment = it 
                isFocused = true
            },
            placeholder = { Text("Aggiungi una recensione pubblica...") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 1,
            maxLines = 5,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                unfocusedIndicatorColor = MaterialTheme.colorScheme.outlineVariant
            )
        )

        if (isFocused || comment.isNotEmpty() || onCancel != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (onCancel != null) {
                    TextButton(onClick = onCancel) {
                        Text("Annulla")
                    }
                } else {
                    TextButton(onClick = { 
                        comment = ""
                        rating = 5
                        isFocused = false
                    }) {
                        Text("Annulla")
                    }
                }
                
                Button(
                    onClick = { 
                        onSubmit(rating, comment) 
                        if (onCancel == null) {
                            comment = ""
                            rating = 5
                            isFocused = false
                        }
                    },
                    enabled = rating > 0
                ) {
                    Text(if (onCancel != null) "Salva" else "Pubblica")
                }
            }
        }
    }
}
