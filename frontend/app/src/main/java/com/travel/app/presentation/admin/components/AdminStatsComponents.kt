package com.travel.app.presentation.admin.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun KpiCard(
    title: String,
    value: String,
    icon: ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier.then(
            if (onClick != null) Modifier.clip(RoundedCornerShape(16.dp)).clickable(onClick = onClick) else Modifier
        ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(iconColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
                if (onClick != null) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Dettagli",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = value,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = title,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

private data class DonutSlice(
    val label: String,
    val count: Int,
    val color: Color
)

@Composable
fun CompanyStatusDonutChart(
    approvedCount: Int,
    pendingCount: Int,
    blockedCount: Int,
    modifier: Modifier = Modifier
) {
    StatusDonut(
        slices = listOf(
            DonutSlice("Approvate", approvedCount, AdminStatusColors.approved),
            DonutSlice("In attesa", pendingCount, AdminStatusColors.pending),
            DonutSlice("Bloccate", blockedCount, AdminStatusColors.blocked)
        ),
        emptyText = "Nessuna agenzia registrata nel sistema",
        pendingCount = pendingCount,
        modifier = modifier
    )
}

@Composable
fun ActivityStatusDonutChart(
    approvedCount: Int,
    pendingCount: Int,
    modifier: Modifier = Modifier
) {
    StatusDonut(
        slices = listOf(
            DonutSlice("Approvate", approvedCount, AdminStatusColors.approved),
            DonutSlice("In attesa", pendingCount, AdminStatusColors.pending)
        ),
        emptyText = "Nessuna attività registrata nel sistema",
        pendingCount = pendingCount,
        modifier = modifier
    )
}

@Composable
private fun StatusDonut(
    slices: List<DonutSlice>,
    emptyText: String,
    pendingCount: Int,
    modifier: Modifier = Modifier
) {
    val total = slices.sumOf { it.count }
    if (total == 0) {
        Box(
            modifier = modifier.fillMaxWidth().height(160.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = emptyText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    val nonZeroSlices = slices.count { it.count > 0 }
    // Piccolo gap tra le fette solo quando ce n'è più di una visibile
    val gapAngle = if (nonZeroSlices > 1) 2.5f else 0f
    val pendingColor = AdminStatusColors.pending

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Box(
                modifier = Modifier.size(120.dp),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 22.dp.toPx()
                    val radius = (size.minDimension - strokeWidth) / 2
                    val center = androidx.compose.ui.geometry.Offset(size.width / 2, size.height / 2)
                    val rect = androidx.compose.ui.geometry.Rect(center, radius)

                    var startAngle = -90f
                    slices.forEach { slice ->
                        if (slice.count > 0) {
                            val sweep = 360f * slice.count / total
                            drawArc(
                                color = slice.color,
                                startAngle = startAngle + gapAngle / 2,
                                sweepAngle = (sweep - gapAngle).coerceAtLeast(1f),
                                useCenter = false,
                                topLeft = rect.topLeft,
                                size = rect.size,
                                style = androidx.compose.ui.graphics.drawscope.Stroke(
                                    width = strokeWidth,
                                    cap = StrokeCap.Butt
                                )
                            )
                            startAngle += sweep
                        }
                    }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$total",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Totali",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                slices.forEach { slice ->
                    LegendItem(
                        color = slice.color,
                        label = slice.label,
                        count = slice.count,
                        percentage = 100 * slice.count / total
                    )
                }
            }
        }

        if (pendingCount > 0) {
            StatusPill(
                text = "$pendingCount in attesa di revisione",
                color = pendingColor
            )
        }
    }
}

@Composable
fun LegendItem(
    color: Color,
    label: String,
    count: Int,
    percentage: Int
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color, CircleShape)
        )
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "$count ($percentage%)",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
