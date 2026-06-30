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
import com.travel.app.data.AppContainer
import com.travel.app.presentation.components.itinerary.ItineraryCard
import com.travel.app.presentation.theme.TravelTheme
import it.unical.ea.dtos.itinerary.ItineraryDto

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CompanyDashboardScreen(
    modifier: Modifier = Modifier
) {
    var itineraries by remember { mutableStateOf<List<ItineraryDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        isLoading = true
        errorMessage = null
        try {
            val result = AppContainer.itineraryRepository.getItineraries()
            result.onSuccess { list ->
                itineraries = list
            }.onFailure { e ->
                errorMessage = e.message ?: "Impossibile caricare gli itinerari"
            }
        } catch (e: Exception) {
            errorMessage = e.message ?: "Errore imprevisto"
        } finally {
            isLoading = false
        }
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

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (errorMessage != null) {
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
                        Text(errorMessage ?: "", color = MaterialTheme.colorScheme.onErrorContainer)
                    }
                }
            }
        } else if (itineraries.isEmpty()) {
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
                items(itineraries) { itinerary ->
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
        CompanyDashboardScreen()
    }
}
