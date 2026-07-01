package com.travel.app.presentation.home

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.travel.app.domain.model.User
import com.travel.app.data.AppContainer
import com.travel.app.presentation.menu.MenuScreen
import com.travel.app.presentation.profile.EditProfileScreen
import com.travel.app.presentation.profile.EditProfileViewModel
import com.travel.app.presentation.components.home.FloatingBottomNavBar
import com.travel.app.presentation.theme.TravelTheme

@RequiresApi(Build.VERSION_CODES.O)
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

    val companyAddOfferViewModel = remember {
        CompanyAddOfferViewModel(
            activityRepository = AppContainer.activityRepository,
            userRepository = AppContainer.userRepository
        )
    }

    val companyDashboardViewModel = remember {
        CompanyDashboardViewModel(
            itineraryRepository = AppContainer.itineraryRepository
        )
    }

    val esploraViewModel = remember {
        EsploraViewModel(
            activityRepository = AppContainer.activityRepository
        )
    }

    val editProfileViewModel = remember {
        EditProfileViewModel(
            userRepository = AppContainer.userRepository
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {

        when (selectedTab) {
            HomeTab.ESPLORA -> {
                if (isSocieta) {
                    CompanyDashboardScreen(viewModel = companyDashboardViewModel)
                } else {
                    EsploraScreen(viewModel = esploraViewModel)
                }
            }
            HomeTab.PREFERITI -> {
                if (isSocieta) {
                    CompanyAddOfferScreen(viewModel = companyAddOfferViewModel)
                } else {
                    CompanyDashboardScreen(viewModel = companyDashboardViewModel)
                }
            }
            HomeTab.PROFILO -> {
                EditProfileScreen(
                    user = currentUser,
                    viewModel = editProfileViewModel,
                    onBack = { selectedTab = HomeTab.MENU },
                    onSaveSuccess = { savedUser ->
                        currentUser = savedUser
                        selectedTab = HomeTab.MENU
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

@RequiresApi(Build.VERSION_CODES.O)
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

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeScreenSocietaPreview() {
    TravelTheme {
        HomeScreen(
            user = User(
                email = "societa@travel.com",
                username = "travel_agency",
                userType = "SOCIETA",
                name = "Travel Agency S.r.l."
            )
        )
    }
}
