package com.travel.app.presentation.menu

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.travel.app.domain.model.User
import com.travel.app.presentation.theme.TravelTheme

@Composable
fun MenuScreen(
    user: User?,
    isDarkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit,
    onBack: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToSecurity: () -> Unit,
    onLogout: () -> Unit,
    onNavigateToFavorites: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val isSocieta = user?.userType == "SOCIETA"
    val context = LocalContext.current

    // Initials Avatar calculation
    val initials = user?.name?.let { name ->
        if (name.isNotBlank()) {
            name.split(" ").take(2).map { it.firstOrNull() ?: "" }.joinToString("").uppercase()
        } else null
    } ?: "U"

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        // TOP HEADER (User info and Avatar aligned on the same horizontal plane)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = user?.name ?: "Utente Ospite",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 28.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    ),
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(16.dp))

                // Initials Avatar
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0xFF8FA4A6), Color(0xFF6B7F82))
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initials,
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Text(
                text = if (isSocieta) "Account Agenzia Partner" else "Account Viaggiatore",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            )
        }

        // SECTION: GESTISCI ACCOUNT (Skyscanner Style 2x2 Grid)
        Text(
            text = if (isSocieta) "Servizi Azienda" else "Gestisci account",
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onBackground
            ),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (isSocieta) {
            // COMPANY SPECIFIC 2x2 GRID
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                MenuGridCard(
                    title = "Profilo Azienda",
                    icon = Icons.Default.Business,
                    iconColor = Color(0xFF2563EB),
                    iconBg = Color(0xFFEFF6FF),
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToProfile
                )
                MenuGridCard(
                    title = "I Miei Annunci",
                    icon = Icons.Default.LocalOffer,
                    iconColor = Color(0xFF059669),
                    iconBg = Color(0xFFECFDF5),
                    modifier = Modifier.weight(1f),
                    onClick = { Toast.makeText(context, "I Miei Annunci: Funzionalità in arrivo!", Toast.LENGTH_SHORT).show() }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                MenuGridCard(
                    title = "Fatturazione",
                    icon = Icons.Default.Receipt,
                    iconColor = Color(0xFFD97706),
                    iconBg = Color(0xFFFFFBEB),
                    modifier = Modifier.weight(1f),
                    onClick = { Toast.makeText(context, "Fatturazione: Funzionalità in arrivo!", Toast.LENGTH_SHORT).show() }
                )
                MenuGridCard(
                    title = "Sicurezza",
                    icon = Icons.Default.Lock,
                    iconColor = Color(0xFF7C3AED),
                    iconBg = Color(0xFFF5F3FF),
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToSecurity
                )
            }
        } else {
            // TRAVELER SPECIFIC 2x2 GRID
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                MenuGridCard(
                    title = "Dettagli Profilo",
                    icon = Icons.Default.Person,
                    iconColor = Color(0xFF2563EB),
                    iconBg = Color(0xFFEFF6FF),
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToProfile
                )
                MenuGridCard(
                    title = "Preferiti",
                    icon = Icons.Default.Favorite,
                    iconColor = Color(0xFFDC2626),
                    iconBg = Color(0xFFFEF2F2),
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToFavorites
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                MenuGridCard(
                    title = "Pagamenti",
                    icon = Icons.Default.CreditCard,
                    iconColor = Color(0xFFD97706),
                    iconBg = Color(0xFFFFFBEB),
                    modifier = Modifier.weight(1f),
                    onClick = { Toast.makeText(context, "Metodi di Pagamento: Funzionalità in arrivo!", Toast.LENGTH_SHORT).show() }
                )
                MenuGridCard(
                    title = "Sicurezza",
                    icon = Icons.Default.Lock,
                    iconColor = Color(0xFF7C3AED),
                    iconBg = Color(0xFFF5F3FF),
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToSecurity
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))


        Text(
            text = "Impostazioni",
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onBackground
            ),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        MenuCardContainer {
            MenuItemRow(
                icon = Icons.Default.Notifications,
                text = "Notifiche",
                onClick = { Toast.makeText(context, "Notifiche: Funzionalità in arrivo!", Toast.LENGTH_SHORT).show() }
            )
            MenuDivider()
            MenuItemRow(
                icon = Icons.Default.DarkMode,
                text = "Modalità Scura",
                onClick = { onDarkModeChange(!isDarkMode) },
                trailingComponent = {
                    Switch(
                        checked = isDarkMode,
                        onCheckedChange = { onDarkModeChange(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                            checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                        )
                    )
                }
            )
            MenuDivider()
            MenuItemRow(
                icon = Icons.AutoMirrored.Filled.ExitToApp,
                text = "Disconnetti",
                textColor = Color(0xFFDC2626),
                iconColor = Color(0xFFDC2626),
                onClick = onLogout
            )
        }

        Spacer(modifier = Modifier.height(120.dp)) // Extra space to scroll past bottom navigation
    }
}

@Composable
fun MenuGridCard(
    title: String,
    icon: ImageVector,
    iconColor: Color,
    iconBg: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(150.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(iconBg, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconColor,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun MenuCardContainer(
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        content = content
    )
}

@Composable
fun MenuItemRow(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    iconColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    trailingComponent: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(22.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = text,
            color = textColor,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp
            ),
            modifier = Modifier.weight(1f)
        )

        if (trailingComponent != null) {
            trailingComponent()
        } else {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun MenuDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 20.dp),
        color = MaterialTheme.colorScheme.outlineVariant
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MenuScreenViaggiatorePreview() {
    TravelTheme {
        MenuScreen(
            user = User(email = "viaggiatore@travel.com", name = "Marco Rossi", userType = "VIAGGIATORE"),
            isDarkMode = false,
            onDarkModeChange = {},
            onBack = {},
            onNavigateToProfile = {},
            onNavigateToSecurity = {},
            onLogout = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MenuScreenSocietaPreview() {
    TravelTheme {
        MenuScreen(
            user = User(email = "societa@travel.com", name = "Travel Agenzia S.p.A.", userType = "SOCIETA"),
            isDarkMode = false,
            onDarkModeChange = {},
            onBack = {},
            onNavigateToProfile = {},
            onNavigateToSecurity = {},
            onLogout = {}
        )
    }
}
