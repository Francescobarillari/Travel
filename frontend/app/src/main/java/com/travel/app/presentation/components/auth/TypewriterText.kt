package com.travel.app.presentation.components.auth

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import com.travel.app.presentation.theme.provider
import kotlinx.coroutines.delay

val typewriterFontFamily = FontFamily(
    Font(
        googleFont = GoogleFont("Playfair Display"),
        fontProvider = provider
    )
)

@Composable
fun TypewriterText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = TextStyle.Default,
    delayMillis: Long = 180L
) {
    var textToDisplay by remember { mutableStateOf("") }
    var cursorVisible by remember { mutableStateOf(true) }

    // Typewriter animation
    LaunchedEffect(text) {
        textToDisplay = ""
        text.forEachIndexed { index, _ ->
            textToDisplay = text.substring(0, index + 1)
            delay(delayMillis)
        }
    }

    // Blinking cursor animation
    LaunchedEffect(Unit) {
        while (true) {
            cursorVisible = !cursorVisible
            delay(500L)
        }
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val mergedStyle = style.copy(
            fontFamily = style.fontFamily ?: typewriterFontFamily
        )
        Text(
            text = textToDisplay,
            style = mergedStyle
        )
        Text(
            text = "|",
            style = mergedStyle.copy(
                color = if (cursorVisible) mergedStyle.color else Color.Transparent
            )
        )
    }
}
