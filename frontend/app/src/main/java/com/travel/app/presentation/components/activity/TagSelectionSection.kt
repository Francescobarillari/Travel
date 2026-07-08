package com.travel.app.presentation.components.activity

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.unical.ea.enums.TravelTag

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TagSelectionSection(
    selectedTags: Set<TravelTag>,
    onTagToggle: (TravelTag) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Seleziona uno o più tag che descrivono l'attività per aiutare i viaggiatori a trovarla:",
            fontSize = 13.sp,
            color = Color(0xFF64748B),
            lineHeight = 18.sp
        )

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TravelTag.values().forEach { tag ->
                val isSelected = selectedTags.contains(tag)
                
                val bgColor = try {
                    Color(android.graphics.Color.parseColor(tag.bgColorHex))
                } catch (e: Exception) {
                    Color(0xFFF1F5F9)
                }
                
                val textColor = try {
                    Color(android.graphics.Color.parseColor(tag.textColorHex))
                } catch (e: Exception) {
                    Color(0xFF475569)
                }

                Surface(
                    onClick = { onTagToggle(tag) },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) bgColor else Color(0xFFF8FAFC),
                    border = if (isSelected) {
                        androidx.compose.foundation.BorderStroke(1.dp, textColor.copy(alpha = 0.5f))
                    } else {
                        androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                    },
                    modifier = Modifier.height(36.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = textColor,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            text = tag.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() },
                            color = if (isSelected) textColor else Color(0xFF475569),
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
