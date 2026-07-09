package com.travel.app.presentation.components.review

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.automirrored.filled.StarHalf
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.travel.app.domain.model.review.ReviewDto

@Composable
fun ReviewCard(
    review: ReviewDto,
    showActivityName: Boolean = false,
    onUpdate: (rating: Double, comment: String) -> Unit = { _, _ -> },
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
                        val ratingValue = review.rating
                        repeat(5) { index ->
                            val isFilled = index + 1.0 <= ratingValue
                            val isHalf = index < ratingValue && index + 1.0 > ratingValue
                            val starIcon = when {
                                isFilled -> Icons.Filled.Star
                                isHalf -> Icons.AutoMirrored.Filled.StarHalf
                                else -> Icons.Outlined.Star
                            }
                            Icon(
                                imageVector = starIcon,
                                contentDescription = null,
                                tint = if (isFilled || isHalf) Color(0xFFFFB300) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
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
                            MaterialTheme(
                                shapes = MaterialTheme.shapes.copy(extraSmall = androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
                            ) {
                                DropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false },
                                    modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f))
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Modifica", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
                                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                        onClick = {
                                            expanded = false
                                            isEditing = true
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Elimina", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error) },
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
                }

                if (showActivityName) {
                    val targetText = when {
                        review.activityName != null -> "Per Attività: ${review.activityName}"
                        review.itineraryName != null -> "Per Itinerario: ${review.itineraryName}"
                        else -> null
                    }
                    if (targetText != null) {
                        Text(
                            text = targetText,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
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
    initialRating: Double = 0.0,
    initialComment: String = "",
    onSubmit: (rating: Double, comment: String) -> Unit,
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
                    onClick = {
                        val targetRatingFull = (index + 1).toDouble()
                        val targetRatingHalf = index + 0.5
                        rating = if (rating == targetRatingFull) {
                            targetRatingHalf
                        } else {
                            targetRatingFull
                        }
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    val isFilled = index + 1.0 <= rating
                    val isHalf = index < rating && index + 1.0 > rating
                    val starIcon = when {
                        isFilled -> Icons.Filled.Star
                        isHalf -> Icons.AutoMirrored.Filled.StarHalf
                        else -> Icons.Outlined.Star
                    }
                    Icon(
                        imageVector = starIcon,
                        contentDescription = "Voto ${index + 1}",
                        tint = if (isFilled || isHalf) Color(0xFFFFB300) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
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

        if (isFocused || comment.isNotEmpty() || rating > 0.0 || onCancel != null) {
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
                        rating = 0.0
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
                            rating = 0.0
                            isFocused = false
                        }
                    },
                    enabled = rating > 0.0
                ) {
                    Text(if (onCancel != null) "Salva" else "Pubblica")
                }
            }
        }
    }
}
