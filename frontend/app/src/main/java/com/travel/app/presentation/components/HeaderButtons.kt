package com.travel.app.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun HeaderBackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(44.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), androidx.compose.foundation.shape.CircleShape),
        enabled = enabled
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Indietro",
            tint = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun HeaderConfirmButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    iconColor: Color = MaterialTheme.colorScheme.primary
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(44.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), androidx.compose.foundation.shape.CircleShape),
        enabled = enabled && !isLoading
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = iconColor,
                strokeWidth = 2.dp
            )
        } else {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Conferma",
                tint = iconColor
            )
        }
    }
}
