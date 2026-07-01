package com.travel.app.presentation.home

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.travel.app.presentation.components.itinerary.ItineraryCard
import com.travel.app.presentation.theme.TravelTheme
import it.unical.ea.dtos.itinerary.ItineraryDto

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CompanyDashboardScreen(
    viewModel: CompanyDashboardViewModel,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(Unit) {
        viewModel.fetchItineraries()
    }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        Text(
            text = "I Tuoi Itinerari",
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp),
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        )

        if (viewModel.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (viewModel.errorMessage != null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.width(8.dp))
                        Text(viewModel.errorMessage ?: "", color = MaterialTheme.colorScheme.onErrorContainer)
                    }
                }
            }
        } else if (viewModel.itineraries.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Non ci sono ancora itinerari disponibili.", style = MaterialTheme.typography.bodyMedium)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                items(viewModel.itineraries) { itinerary ->
                    ItineraryCard(
                        itinerary = itinerary,
                        actions = {
                            TextButton(onClick = { /* TODO */ }) {
                                Text("Modifica")
                            }
                        }
                    )
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@androidx.compose.ui.tooling.preview.Preview(showBackground = true, showSystemUi = true)
@Composable
fun CompanyDashboardScreenPreview() {
    TravelTheme {
        val mockViewModel = remember {
            CompanyDashboardViewModel(
                itineraryRepository = object : com.travel.app.domain.repository.ItineraryRepository {
                    override suspend fun getItineraries() = Result.success(emptyList<ItineraryDto>())
                }
            )
        }
        CompanyDashboardScreen(viewModel = mockViewModel)
    }
}
