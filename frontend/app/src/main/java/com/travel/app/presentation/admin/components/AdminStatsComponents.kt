package com.travel.app.presentation.admin.components

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

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
            if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
        ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
                if (onClick != null) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Dettagli",
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun CompanyStatusDonutChart(
    approvedCount: Int,
    pendingCount: Int,
    blockedCount: Int,
    modifier: Modifier = Modifier
) {
    val total = approvedCount + pendingCount + blockedCount
    if (total == 0) {
        Box(
            modifier = modifier.fillMaxWidth().height(160.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Nessuna società registrata nel sistema",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }
        return
    }

    val approvedAngle = 360f * approvedCount / total
    val pendingAngle = 360f * pendingCount / total
    val blockedAngle = 360f * blockedCount / total

    val approvedPct = (100 * approvedCount / total)
    val pendingPct = (100 * pendingCount / total)
    val blockedPct = 100 - approvedPct - pendingPct // evita errori di arrotolamento

    Row(
        modifier = modifier
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
                val strokeWidth = 24.dp.toPx()
                val radius = (size.minDimension - strokeWidth) / 2
                val center = androidx.compose.ui.geometry.Offset(size.width / 2, size.height / 2)
                val rect = androidx.compose.ui.geometry.Rect(center, radius)

                var startAngle = -90f

                // Approved (Green)
                if (approvedAngle > 0) {
                    drawArc(
                        color = Color(0xFF2E7D32),
                        startAngle = startAngle,
                        sweepAngle = approvedAngle,
                        useCenter = false,
                        topLeft = rect.topLeft,
                        size = rect.size,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(
                            width = strokeWidth,
                            cap = androidx.compose.ui.graphics.StrokeCap.Round
                        )
                    )
                    startAngle += approvedAngle
                }

                // Pending (Orange)
                if (pendingAngle > 0) {
                    drawArc(
                        color = Color(0xFFE65100),
                        startAngle = startAngle,
                        sweepAngle = pendingAngle,
                        useCenter = false,
                        topLeft = rect.topLeft,
                        size = rect.size,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(
                            width = strokeWidth,
                            cap = androidx.compose.ui.graphics.StrokeCap.Round
                        )
                    )
                    startAngle += pendingAngle
                }

                // Blocked (Red)
                if (blockedAngle > 0) {
                    drawArc(
                        color = Color(0xFFC62828),
                        startAngle = startAngle,
                        sweepAngle = blockedAngle,
                        useCenter = false,
                        topLeft = rect.topLeft,
                        size = rect.size,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(
                            width = strokeWidth,
                            cap = androidx.compose.ui.graphics.StrokeCap.Round
                        )
                    )
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
                    color = Color.Gray
                )
            }
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            LegendItem(color = Color(0xFF2E7D32), label = "Approvate", count = approvedCount, percentage = approvedPct)
            LegendItem(color = Color(0xFFE65100), label = "In Attesa", count = pendingCount, percentage = pendingPct)
            LegendItem(color = Color(0xFFC62828), label = "Bloccate", count = blockedCount, percentage = blockedPct)
        }
    }
}

@Composable
fun ActivityStatusDonutChart(
    approvedCount: Int,
    pendingCount: Int,
    modifier: Modifier = Modifier
) {
    val total = approvedCount + pendingCount
    if (total == 0) {
        Box(
            modifier = modifier.fillMaxWidth().height(160.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Nessuna attività registrata nel sistema",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }
        return
    }

    val approvedAngle = 360f * approvedCount / total
    val pendingAngle = 360f * pendingCount / total

    val approvedPct = (100 * approvedCount / total)
    val pendingPct = 100 - approvedPct

    Row(
        modifier = modifier
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
                val strokeWidth = 24.dp.toPx()
                val radius = (size.minDimension - strokeWidth) / 2
                val center = androidx.compose.ui.geometry.Offset(size.width / 2, size.height / 2)
                val rect = androidx.compose.ui.geometry.Rect(center, radius)

                var startAngle = -90f

                // Approved (Green)
                if (approvedAngle > 0) {
                    drawArc(
                        color = Color(0xFF2E7D32),
                        startAngle = startAngle,
                        sweepAngle = approvedAngle,
                        useCenter = false,
                        topLeft = rect.topLeft,
                        size = rect.size,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(
                            width = strokeWidth,
                            cap = androidx.compose.ui.graphics.StrokeCap.Round
                        )
                    )
                    startAngle += approvedAngle
                }

                // Pending (Orange)
                if (pendingAngle > 0) {
                    drawArc(
                        color = Color(0xFFE65100),
                        startAngle = startAngle,
                        sweepAngle = pendingAngle,
                        useCenter = false,
                        topLeft = rect.topLeft,
                        size = rect.size,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(
                            width = strokeWidth,
                            cap = androidx.compose.ui.graphics.StrokeCap.Round
                        )
                    )
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
                    color = Color.Gray
                )
            }
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            LegendItem(color = Color(0xFF2E7D32), label = "Approvate", count = approvedCount, percentage = approvedPct)
            LegendItem(color = Color(0xFFE65100), label = "In Attesa", count = pendingCount, percentage = pendingPct)
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
                color = Color.Gray
            )
        }
    }
}
