package com.travel.app.presentation.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.travel.app.domain.model.User
import com.travel.app.presentation.theme.TravelTheme

@Composable
fun MenuScreen(
    user: User?,
    onBack: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val isSocieta = user?.userType == "SOCIETA"
    var isDarkModeEnabled by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        // TOP NAVIGATION HEADER
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Circle back button like in the mockup
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Torna indietro",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "Impostazioni",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
            )

            Spacer(modifier = Modifier.weight(1.4f)) // Adjust spacing to center title
        }

        Spacer(modifier = Modifier.height(24.dp))

        // PROFILE HEADER CARD
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onNavigateToProfile),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circular Avatar Placeholder with Initials
                val initials = user?.username?.let { name ->
                    if (name.isNotBlank()) {
                        name.split(" ").take(2).map { it.firstOrNull() ?: "" }.joinToString("").uppercase()
                    } else null
                } ?: "U"

                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initials,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 20.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = user?.username ?: "Utente Ospite",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (isSocieta) "Account Società Partner" else "Account Viaggiatore",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    )
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Dettagli Account",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // SECTION: IMPOSTAZIONI ACCOUNT / SERVIZI
        Text(
            text = if (isSocieta) "Servizi Azienda" else "Impostazioni Account",
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            ),
            modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
        )

        // Custom White Box containing item list
        MenuCardContainer {
            if (isSocieta) {
                // COMPANY SPECIFIC MENU ITEMS
                MenuItemRow(
                    icon = Icons.Default.Business,
                    text = "Profilo Azienda",
                    onClick = onNavigateToProfile
                )
                MenuDivider()
                MenuItemRow(
                    icon = Icons.Default.LocalOffer,
                    text = "I Miei Annunci e Offerte",
                    onClick = {}
                )
                MenuDivider()
                MenuItemRow(
                    icon = Icons.Default.Receipt,
                    text = "Dati di Fatturazione & Pagamenti",
                    onClick = {}
                )
            } else {
                // TRAVELER SPECIFIC MENU ITEMS
                MenuItemRow(
                    icon = Icons.Default.Person,
                    text = "Dettagli Profilo",
                    onClick = onNavigateToProfile
                )
                MenuDivider()
                MenuItemRow(
                    icon = Icons.Default.CardTravel,
                    text = "I Miei Viaggi e Prenotazioni",
                    onClick = {}
                )
                MenuDivider()
                MenuItemRow(
                    icon = Icons.Default.CreditCard,
                    text = "Metodi di Pagamento",
                    onClick = {}
                )
            }

            MenuDivider()
            MenuItemRow(
                icon = Icons.Default.Lock,
                text = "Sicurezza e Password",
                onClick = {}
            )
            MenuDivider()
            MenuItemRow(
                icon = Icons.Default.Notifications,
                text = "Impostazioni Notifiche",
                onClick = {}
            )
            MenuDivider()
            // Switch row for Dark Mode
            MenuItemRow(
                icon = Icons.Default.DarkMode,
                text = "Modalità Scura",
                onClick = {},
                trailingComponent = {
                    Switch(
                        checked = isDarkModeEnabled,
                        onCheckedChange = { isDarkModeEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                            checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                        )
                    )
                }
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // SECTION: SUPPORTO & GENERALI
        Text(
            text = "Altro",
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            ),
            modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
        )

        MenuCardContainer {
            MenuItemRow(
                icon = Icons.Default.Info,
                text = "Informazioni sull'Applicazione",
                onClick = {}
            )
            MenuDivider()
            MenuItemRow(
                icon = Icons.Default.QuestionAnswer,
                text = "Centro Assistenza & FAQ",
                onClick = {}
            )
            MenuDivider()
            MenuItemRow(
                icon = Icons.Default.ExitToApp,
                text = "Disconnetti",
                onClick = onLogout
            )
            MenuDivider()
            // Dangerous action item (red colored)
            MenuItemRow(
                icon = Icons.Default.Delete,
                text = "Disattiva il mio Account",
                textColor = Color(0xFFDC2626),
                iconColor = Color(0xFFDC2626),
                onClick = {}
            )
        }

        Spacer(modifier = Modifier.height(120.dp)) // Extra space to scroll past bottom navigation
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
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        content = content
    )
}

@Composable
fun MenuItemRow(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    iconColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
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
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            )
        }
    }
}

@Composable
fun MenuDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 20.dp),
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MenuScreenViaggiatorePreview() {
    TravelTheme {
        MenuScreen(
            user = User(email = "viaggiatore@travel.com", username = "Marco Rossi", userType = "VIAGGIATORE"),
            onBack = {},
            onNavigateToProfile = {},
            onLogout = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MenuScreenSocietaPreview() {
    TravelTheme {
        MenuScreen(
            user = User(email = "societa@travel.com", username = "Travel Agenzia S.p.A.", userType = "SOCIETA"),
            onBack = {},
            onNavigateToProfile = {},
            onLogout = {}
        )
    }
}
