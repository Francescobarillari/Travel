package com.travel.app.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.travel.app.domain.model.User
import com.travel.app.data.AppContainer
import com.travel.app.presentation.menu.MenuScreen
import com.travel.app.presentation.profile.ProfileScreen
import com.travel.app.presentation.components.home.FloatingBottomNavBar
import com.travel.app.presentation.theme.TravelTheme

@Composable
fun HomeScreen(user: User? = null, onLogout: () -> Unit = {}) {
    var selectedTab by remember { mutableStateOf(HomeTab.ESPLORA) }
    var currentUser by remember(user) { 
        mutableStateOf(user ?: User(
            email = "johnkinggraphics@gmail.com", 
            username = "johnkinggraphics", 
            userType = "VIAGGIATORE",
            phone = "6895312",
            name = "Charlotte king",
            password = "password123"
        )) 
    }

    val isSocieta = currentUser.userType == "SOCIETA"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {

        when (selectedTab) {
            HomeTab.ESPLORA -> {
                if (isSocieta) {
                    CompanyDashboardScreen()
                } else {
                    EsploraScreen()
                }
            }
            HomeTab.PREFERITI -> {
                if (isSocieta) {
                    CompanyAddOfferScreen()
                } else {
                    SimplePlaceholderScreen(title = "Preferiti")
                }
            }
            HomeTab.PROFILO -> {
                val userRepository = AppContainer.userRepository
                val isMockUser = currentUser.email in listOf("test@travel.com", "societa@travel.com", "johnkinggraphics@gmail.com")
                ProfileScreen(
                    user = currentUser,
                    onBack = { selectedTab = HomeTab.MENU },
                    onSave = { updatedUser ->
                        if (isMockUser) {
                            currentUser = updatedUser
                            selectedTab = HomeTab.MENU
                            Result.success(updatedUser)
                        } else {
                            val result = userRepository.updateMe(updatedUser)
                            result.onSuccess { savedUser ->
                                currentUser = savedUser
                                selectedTab = HomeTab.MENU
                            }
                            result
                        }
                    }
                )
            }
            HomeTab.MENU -> MenuScreen(
                user = currentUser,
                onBack = { selectedTab = HomeTab.ESPLORA },
                onNavigateToProfile = { selectedTab = HomeTab.PROFILO },
                onLogout = onLogout
            )
        }


        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            FloatingBottomNavBar(
                selectedTab = selectedTab,
                isSocieta = isSocieta,
                onTabSelected = { selectedTab = it }
            )
        }
    }
}

@Composable
fun EsploraScreen() {
    var searchQuery by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = 16.dp)
    ) {

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Text(
                text = "Travel",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            )
        }

        // Modern, premium SearchBar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            placeholder = { 
                Text(
                    text = "Cerca attività, viaggi, mete...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                ) 
            },
            leadingIcon = { 
                Icon(
                    imageVector = Icons.Default.Search, 
                    contentDescription = "Cerca", 
                    tint = MaterialTheme.colorScheme.primary 
                ) 
            },
            singleLine = true,
            shape = RoundedCornerShape(32.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                focusedLabelColor = MaterialTheme.colorScheme.primary
            )
        )

        Spacer(modifier = Modifier.height(110.dp))
    }
}

@Composable
fun SimplePlaceholderScreen(title: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeScreenViaggiatorePreview() {
    TravelTheme {
        HomeScreen(
            user = User(
                email = "viaggiatore@travel.com",
                username = "viaggiatore123",
                userType = "VIAGGIATORE",
                name = "Marco Rossi"
            )
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeScreenSocietaPreview() {
    TravelTheme {
        HomeScreen(
            user = User(
                email = "societa@travel.com",
                username = "travel_agency",
                userType = "SOCIETA",
                name = "Agenzia Viaggi Italia S.r.l."
            )
        )
    }
}
