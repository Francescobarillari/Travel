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
import com.travel.app.presentation.profile.SecurityScreen
import com.travel.app.presentation.profile.SecurityViewModel
import com.travel.app.presentation.components.home.FloatingBottomNavBar
import com.travel.app.presentation.theme.TravelTheme
import com.travel.app.presentation.components.itinerary.ItineraryDetailScreen
import com.travel.app.presentation.components.activity.ActivityDetailScreen
import it.unical.ea.dtos.itinerary.ItineraryDto

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreen(
    user: User? = null,
    isDarkMode: Boolean = false,
    onDarkModeChange: (Boolean) -> Unit = {},
    onLogout: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(HomeTab.ESPLORA) }
    var selectedItemId by remember { mutableStateOf<String?>(null) }
    var selectedItemIsTrip by remember { mutableStateOf(true) }
    var selectedActivityIdForBookings by remember { mutableStateOf<String?>(null) }
    var selectedItinerary by remember { mutableStateOf<ItineraryDto?>(null) }

    var currentUser by remember(user) { 
        mutableStateOf(user ?: User(
            email = "johnkinggraphics@gmail.com", 
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
            itineraryRepository = AppContainer.itineraryRepository,
            activityRepository = AppContainer.activityRepository,
            userRepository = AppContainer.userRepository
        )
    }

    val esploraViewModel = remember {
        EsploraViewModel(
            activityRepository = AppContainer.activityRepository,
            localitaRepository = AppContainer.localitaRepository,
            itineraryRepository = AppContainer.itineraryRepository
        )
    }

    val editProfileViewModel = remember {
        EditProfileViewModel(
            userRepository = AppContainer.userRepository
        )
    }

    val securityViewModel = remember {
        SecurityViewModel(
            userRepository = AppContainer.userRepository
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (selectedItinerary != null) {
            ItineraryDetailScreen(
                itinerary = selectedItinerary!!,
                onNavigateBack = { selectedItinerary = null },
                onActivityClick = { activityId ->
                    selectedItinerary = null
                    selectedItemId = activityId
                    selectedItemIsTrip = false
                }
            )
        } else if (selectedItemId != null && !selectedItemIsTrip) {
            ActivityDetailScreen(
                activityId = selectedItemId!!,
                onNavigateBack = { selectedItemId = null }
            )
        } else {
            Box(modifier = Modifier.fillMaxSize()) {
                when (selectedTab) {
            HomeTab.ESPLORA -> {
                if (isSocieta) {
                    if (selectedActivityIdForBookings != null) {
                        CompanyActivityBookingsScreen(
                            viewModel = remember(selectedActivityIdForBookings) {
                                CompanyActivityBookingsViewModel(
                                    activityRepository = AppContainer.activityRepository,
                                    activityId = selectedActivityIdForBookings!!
                                )
                            },
                            onBackClick = { selectedActivityIdForBookings = null }
                        )
                    } else {
                        CompanyDashboardScreen(
                            viewModel = companyDashboardViewModel,
                            onEditActivityClick = { activityId ->
                                companyAddOfferViewModel.loadActivity(activityId)
                                selectedTab = HomeTab.PREFERITI
                            },
                            onViewBookingsClick = { activityId ->
                                selectedActivityIdForBookings = activityId
                            },
                            onItineraryClick = { selectedItinerary = it }
                        )
                    }
                } else {
                    EsploraScreen(
                        viewModel = esploraViewModel,
                        onItemClick = { id, isTrip ->
                            selectedItemId = id
                            selectedItemIsTrip = isTrip
                            if (isTrip) {
                                val found = esploraViewModel.filteredItineraries.find { it.id?.toString() == id }
                                if (found != null) {
                                    selectedItinerary = found
                                }
                            }
                        }
                    )
                }
            }

            HomeTab.PREFERITI -> {
                if (isSocieta) {
                    CompanyAddOfferScreen(
                        viewModel = companyAddOfferViewModel,
                        onNavigateBack = { selectedTab = HomeTab.ESPLORA }
                    )
                } else {
                    PreferitiScreen(
                        onActivityClick = { id ->
                            selectedItemId = id
                            selectedItemIsTrip = false
                        },
                        onItineraryClick = { itinerary ->
                            selectedItinerary = itinerary
                        }
                    )
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
                isDarkMode = isDarkMode,
                onDarkModeChange = onDarkModeChange,
                onBack = { selectedTab = HomeTab.ESPLORA },
                onNavigateToProfile = { selectedTab = HomeTab.PROFILO },
                onNavigateToSecurity = { selectedTab = HomeTab.SICUREZZA },
                onLogout = onLogout,
                onNavigateToFavorites = { selectedTab = HomeTab.PREFERITI }
            )
            HomeTab.SICUREZZA -> {
                SecurityScreen(
                    user = currentUser,
                    viewModel = securityViewModel,
                    onBack = { selectedTab = HomeTab.MENU },
                    onSaveSuccess = { savedUser ->
                        currentUser = savedUser
                        selectedTab = HomeTab.MENU
                    }
                )
            }
        }

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                ) {
                    FloatingBottomNavBar(
                        selectedTab = selectedTab,
                        isSocieta = isSocieta,
                        onTabSelected = { 
                            if (it == HomeTab.PREFERITI && isSocieta) {
                                companyAddOfferViewModel.resetForm()
                            }
                            selectedTab = it 
                        }
                    )
                }
            }
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
                userType = "SOCIETA",
                name = "Travel Agency S.r.l."
            )
        )
    }
}
