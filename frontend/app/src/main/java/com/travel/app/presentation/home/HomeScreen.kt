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
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import com.travel.app.domain.model.review.ReviewDto
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreen(
    user: User? = null,
    isDarkMode: Boolean = false,
    onDarkModeChange: (Boolean) -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedTab by remember { mutableStateOf(HomeTab.HOME) }
    var selectedItemId by remember { mutableStateOf<String?>(null) }
    var selectedItemIsTrip by remember { mutableStateOf(true) }
    var selectedActivityIdForBookings by remember { mutableStateOf<String?>(null) }
    var selectedItinerary by remember { mutableStateOf<ItineraryDto?>(null) }
    var personalizingItinerary by remember { mutableStateOf<ItineraryDto?>(null) }
    var selectedProfileUser by remember { mutableStateOf<User?>(null) }
    var itinerariesRefreshTrigger by remember { mutableStateOf(0) }
    var favoritesTrigger by remember { mutableStateOf(0) }

    var currentUser by remember(user) { 
        mutableStateOf(user ?: User(
            id = "550e8400-e29b-41d4-a716-446655440000",
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
            userRepository = AppContainer.userRepository,
            localitaRepository = AppContainer.localitaRepository
        )
    }

    val companyDashboardViewModel = remember {
        CompanyDashboardViewModel(
            activityRepository = AppContainer.activityRepository,
            userRepository = AppContainer.userRepository
        )
    }

    val cercaViewModel = remember {
        CercaViewModel(
            activityRepository = AppContainer.activityRepository,
            localitaRepository = AppContainer.localitaRepository,
            userRepository = AppContainer.userRepository,
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
        // Always compose the main content to preserve state (like scroll position)
        Box(modifier = Modifier.fillMaxSize()) {
            when (selectedTab) {
                HomeTab.HOME -> {
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
                                }
                            )
                        }
                    } else {
                        HomeFeedScreen(
                            onLocalitaClick = { localitaName ->
                                AppContainer.sessionManager.incrementLocationScore(localitaName, 1)
                                cercaViewModel.onSearchQueryChanged(localitaName, saveToHistory = false)
                                selectedTab = HomeTab.CERCA
                            },
                            onActivityClick = { activityId ->
                                selectedItemId = activityId
                                selectedItemIsTrip = false
                            },
                            onItineraryClick = { itinerary ->
                                selectedItinerary = itinerary
                            },
                            favoritesTrigger = favoritesTrigger
                        )
                    }
                }
                
                HomeTab.CERCA -> {
                    if (!isSocieta) {
                        CercaScreen(
                            viewModel = cercaViewModel,
                            favoritesTrigger = favoritesTrigger,
                            onItemClick = { id, isTrip ->
                                selectedItemId = id
                                selectedItemIsTrip = isTrip
                                if (isTrip) {
                                    val found = cercaViewModel.filteredItineraries.find { it.id?.toString() == id }
                                    if (found != null) {
                                        selectedItinerary = found
                                    }
                                }
                            },
                            onUserClick = { u ->
                                selectedProfileUser = u
                            }
                        )
                    }
                }

                HomeTab.PREFERITI -> {
                    if (isSocieta) {
                        CompanyAddOfferScreen(
                            viewModel = companyAddOfferViewModel,
                            onNavigateBack = { selectedTab = HomeTab.HOME }
                        )
                    } else {
                        PreferitiScreen(
                            onActivityClick = { activityId -> 
                                selectedItemId = activityId
                                selectedItemIsTrip = false
                            },
                            onItineraryClick = { selectedItinerary = it }
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
                        },
                        onDeactivated = onLogout
                    )
                }
                HomeTab.MENU -> MenuScreen(
                    user = currentUser,
                    isDarkMode = isDarkMode,
                    onDarkModeChange = onDarkModeChange,
                    onBack = { selectedTab = HomeTab.HOME },
                    onNavigateToProfile = { selectedTab = HomeTab.PROFILO },
                    onNavigateToSecurity = { selectedTab = HomeTab.SICUREZZA },
                    onLogout = onLogout,
                    onNavigateToMyItineraries = { selectedTab = HomeTab.I_MIEI_ITINERARI }
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
                HomeTab.I_MIEI_ITINERARI -> {
                    MyItinerariesScreen(
                        user = currentUser,
                        onBack = { selectedTab = HomeTab.MENU },
                        onItineraryClick = { itinerary ->
                            selectedItinerary = itinerary
                        },
                        refreshTrigger = itinerariesRefreshTrigger
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

        // Overlay detail screens on top of the main content
        
        // 1. User Profile Overlay (composed first, so details render on top of it)
        if (selectedProfileUser != null) {
            UserProfileScreen(
                user = selectedProfileUser!!,
                onBack = { selectedProfileUser = null },
                onItineraryClick = { itinerary ->
                    selectedItinerary = itinerary
                },
                onReviewClick = { review ->
                    if (review.itineraryId != null) {
                        scope.launch {
                            val res = AppContainer.itineraryRepository.getItineraryById(review.itineraryId.toString())
                            res.fold(
                                onSuccess = { itinerary ->
                                    selectedItinerary = itinerary
                                },
                                onFailure = { err ->
                                    Toast.makeText(context, "Errore: ${err.message}", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    } else if (review.activityId != null) {
                        selectedItemId = review.activityId.toString()
                        selectedItemIsTrip = false
                    }
                }
            )
        }

        // 2. Itinerary Detail Overlay
        if (selectedItinerary != null) {
            val itineraryId = selectedItinerary!!.id?.toString() ?: ""
            var isFav by remember(itineraryId, favoritesTrigger) { 
                mutableStateOf(AppContainer.sessionManager.isFavoriteItinerary(itineraryId)) 
            }
            LaunchedEffect(itineraryId) {
                selectedItinerary!!.activities?.mapNotNull { it.location?.split(",")?.firstOrNull()?.trim() }
                    ?.distinct()
                    ?.forEach { city ->
                        AppContainer.sessionManager.incrementLocationScore(city, 1)
                    }
            }
            ItineraryDetailScreen(
                itinerary = selectedItinerary!!,
                onNavigateBack = { selectedItinerary = null },
                onActivityClick = { activityId ->
                    selectedItinerary = null
                    selectedItemId = activityId
                    selectedItemIsTrip = false
                },
                isFavorite = isFav,
                onFavoriteClick = {
                    AppContainer.sessionManager.toggleFavoriteItinerary(itineraryId)
                    isFav = !isFav
                    favoritesTrigger++
                },
                onPersonalizeClick = { itinerary ->
                    personalizingItinerary = itinerary
                },
                onDeleteSuccess = {
                    selectedItinerary = null
                    itinerariesRefreshTrigger++
                }
            )
        }

        // 3. Activity Detail Overlay
        if (selectedItemId != null && !selectedItemIsTrip) {
            val activityId = selectedItemId!!
            var isFav by remember(activityId, favoritesTrigger) { 
                mutableStateOf(AppContainer.sessionManager.isFavoriteActivity(activityId)) 
            }
            ActivityDetailScreen(
                activityId = activityId,
                onNavigateBack = { selectedItemId = null },
                isFavorite = isFav,
                onFavoriteClick = {
                    AppContainer.sessionManager.toggleFavoriteActivity(activityId)
                    isFav = !isFav
                    favoritesTrigger++
                }
            )
        }

        // 4. Personalize Overlay
        if (personalizingItinerary != null) {
            PersonalizeItineraryScreen(
                itinerary = personalizingItinerary!!,
                onNavigateBack = { personalizingItinerary = null },
                onPersonalizeSuccess = {
                    personalizingItinerary = null
                    selectedItinerary = null
                }
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
                name = "Dèrive Agenzia S.r.l."
            )
        )
    }
}
