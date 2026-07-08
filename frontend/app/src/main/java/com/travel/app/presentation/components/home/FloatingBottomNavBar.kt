package com.travel.app.presentation.components.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.travel.app.presentation.home.HomeTab

@Composable
fun FloatingBottomNavBar(
    selectedTab: HomeTab,
    isSocieta: Boolean,
    onTabSelected: (HomeTab) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(32.dp),
                    clip = false
                )
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(32.dp)
                )
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            if (isSocieta) {
                HomeTabItem(
                    icon = Icons.Default.Dashboard,
                    label = "Dashboard",
                    isSelected = selectedTab == HomeTab.HOME,
                    onClick = { onTabSelected(HomeTab.HOME) },
                    modifier = Modifier.weight(1f)
                )
                HomeTabItem(
                    icon = Icons.Default.Add,
                    label = "Aggiungi",
                    isSelected = selectedTab == HomeTab.PREFERITI,
                    onClick = { onTabSelected(HomeTab.PREFERITI) },
                    modifier = Modifier.weight(1f)
                )
            } else {
                HomeTabItem(
                    icon = Icons.Default.Home,
                    label = "Home",
                    isSelected = selectedTab == HomeTab.HOME,
                    onClick = { onTabSelected(HomeTab.HOME) },
                    modifier = Modifier.weight(1f)
                )
                HomeTabItem(
                    icon = Icons.Default.Search,
                    label = "Cerca",
                    isSelected = selectedTab == HomeTab.CERCA,
                    onClick = { onTabSelected(HomeTab.CERCA) },
                    modifier = Modifier.weight(1f)
                )
                
                // Pulsante '+' centrale di colore blu per creare itinerari
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .shadow(4.dp, CircleShape)
                        .background(
                            color = if (selectedTab == HomeTab.CREA_ITINERARIO) MaterialTheme.colorScheme.primary else Color(0xFF3B82F6),
                            shape = CircleShape
                        )
                        .clickable { onTabSelected(HomeTab.CREA_ITINERARIO) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Crea Itinerario",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                HomeTabItem(
                    icon = Icons.Default.Favorite,
                    label = "Preferiti",
                    isSelected = selectedTab == HomeTab.PREFERITI,
                    onClick = { onTabSelected(HomeTab.PREFERITI) },
                    modifier = Modifier.weight(1f)
                )
            }
            HomeTabItem(
                icon = Icons.Default.Menu,
                label = "Menù",
                isSelected = selectedTab == HomeTab.MENU,
                onClick = { onTabSelected(HomeTab.MENU) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun HomeTabItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val contentColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
    val backgroundColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent

     Box(
        modifier = modifier
            .padding(vertical = 6.dp, horizontal = 4.dp)
            .height(48.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = contentColor,
            modifier = Modifier.size(24.dp)
        )
    }
}
