package com.travel.app.presentation.components.common

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

/**
 *
 * @param onBack invocato quando lo swipe supera la soglia.
 * @param enabled se false il gesto è disabilitato.
 * @param edgeWidth larghezza della zona sensibile a partire dal bordo sinistro.
 * @param activationFraction frazione della larghezza oltre la quale lo swipe attiva il back.
 */
fun Modifier.swipeToGoBack(
    enabled: Boolean = true,
    edgeWidth: Dp = 24.dp,
    activationFraction: Float = 0.35f,
    onBack: () -> Unit
): Modifier = composed {
    if (!enabled) return@composed this

    val density = LocalDensity.current
    val edgeWidthPx = with(density) { edgeWidth.toPx() }
    val offsetX = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val currentOnBack by rememberUpdatedState(onBack)

    this
        .pointerInput(enabled) {
            var startedFromEdge = false
            detectHorizontalDragGestures(
                onDragStart = { startOffset ->
                    startedFromEdge = startOffset.x <= edgeWidthPx
                },
                onHorizontalDrag = { change, dragAmount ->
                    if (startedFromEdge) {
                        change.consume()
                        val newValue = (offsetX.value + dragAmount)
                            .coerceIn(0f, size.width.toFloat())
                        scope.launch { offsetX.snapTo(newValue) }
                    }
                },
                onDragEnd = {
                    if (startedFromEdge) {
                        val threshold = size.width * activationFraction
                        if (offsetX.value >= threshold) {
                            scope.launch {
                                offsetX.animateTo(size.width.toFloat())
                                currentOnBack()
                                offsetX.snapTo(0f)
                            }
                        } else {
                            scope.launch { offsetX.animateTo(0f) }
                        }
                    }
                    startedFromEdge = false
                },
                onDragCancel = {
                    if (startedFromEdge) {
                        scope.launch { offsetX.animateTo(0f) }
                    }
                    startedFromEdge = false
                }
            )
        }
        .graphicsLayer {
            translationX = offsetX.value
        }
}
