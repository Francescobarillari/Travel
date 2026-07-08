package com.travel.app.presentation.components.itinerary

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.paypal.checkout.PayPalCheckout
import com.paypal.checkout.approve.OnApprove
import com.paypal.checkout.cancel.OnCancel
import com.paypal.checkout.error.OnError
import it.unical.ea.dtos.activity.ActivityDto
import it.unical.ea.dtos.itinerary.ItineraryDto
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import android.widget.Toast
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.travel.app.BuildConfig
import com.travel.app.data.AppContainer
import com.travel.app.domain.model.review.ReviewDto
import com.travel.app.domain.model.review.CreateReviewDto
import com.travel.app.presentation.components.review.ReviewCard
import com.travel.app.presentation.components.review.AddReviewInline
import kotlinx.coroutines.launch
import com.travel.app.utils.CalendarExportUtil
import androidx.compose.material.icons.filled.Handyman
import com.travel.app.presentation.components.checkout.CheckoutSummaryScreen
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.material.icons.filled.Check
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ItineraryDetailScreen(
    itinerary: ItineraryDto,
    onNavigateBack: () -> Unit,
    viewModel: ItineraryDetailViewModel = viewModel(key = itinerary.getId()?.toString()),
    onActivityClick: (String) -> Unit = {},
    isFavorite: Boolean = false,
    onFavoriteClick: () -> Unit = {},
    onPersonalizeClick: (ItineraryDto) -> Unit = {},
    onDeleteSuccess: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val totalPrice = itinerary.getActivities()?.sumOf { it.getPrice()?.toDouble() ?: 0.0 } ?: 0.0
    val uniqueTags = itinerary.getActivities()?.flatMap { it.getTags() ?: emptySet() }?.toSet() ?: emptySet()
    val uniqueLocations = itinerary.getActivities()?.map { it.getLocation() }?.filter { !it.isNullOrBlank() }?.distinct() ?: emptyList()

    var reviews by remember { mutableStateOf<List<ReviewDto>>(emptyList()) }
    val scope = rememberCoroutineScope()

    val isPreview = androidx.compose.ui.platform.LocalInspectionMode.current
    val currentUser = remember { if (isPreview) null else AppContainer.sessionManager.getSessionUser() }
    val isViaggiatore = currentUser?.userType == "VIAGGIATORE"

    LaunchedEffect(itinerary.id) {
        val id = itinerary.id?.toString()
        if (id != null) {
            val reviewsResult = AppContainer.reviewRepository.getReviewsForItinerary(id)
            if (reviewsResult.isSuccess) {
                reviews = reviewsResult.getOrNull() ?: emptyList()
            }
        }
    }

    val clientSecret by viewModel.paymentClientSecret.collectAsState()
    val showCheckoutSummary by viewModel.showCheckoutSummary.collectAsState()
    val bookingId by viewModel.bookingId.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val isBooked by viewModel.isBooked.collectAsState()
    val showSummaryDialog by viewModel.showSummaryDialog.collectAsState()
    val showCancelSuccessDialog by viewModel.showCancelSuccessDialog.collectAsState()
    var showCancelConfirmationDialog by remember { mutableStateOf(false) }

    LaunchedEffect(itinerary.getId()) {
        itinerary.getId()?.let {
            viewModel.checkIsBooked(it.toString())
        }
    }

    LaunchedEffect(Unit) {
        PayPalCheckout.registerCallbacks(
            onApprove = OnApprove { approval ->
                Toast.makeText(context, "Pagamento completato!", Toast.LENGTH_SHORT).show()
                viewModel.confirmPaymentSuccess()
            },
            onCancel = OnCancel {
                Toast.makeText(context, "Pagamento annullato", Toast.LENGTH_SHORT).show()
                viewModel.clearClientSecret()
            },
            onError = OnError { errorInfo ->
                Toast.makeText(context, "Errore nel pagamento: ${errorInfo.error.message}", Toast.LENGTH_LONG).show()
                viewModel.clearClientSecret()
            }
        )
    }

    var startPayPalCheckout by remember { mutableStateOf(false) }
    LaunchedEffect(startPayPalCheckout, clientSecret) {
        if (startPayPalCheckout && clientSecret != null) {
            PayPalCheckout.startCheckout(com.paypal.checkout.createorder.CreateOrder { createOrderActions ->
                createOrderActions.set(clientSecret!!)
            })
            startPayPalCheckout = false
        }
    }

    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        }
    }

    if (showSummaryDialog) {
        SuccessAnimationScreen(
            title = itinerary.getTitle() ?: "N/D",
            onDismiss = { viewModel.onSummaryDialogDismissed() }
        )
    }

    if (showCancelConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { showCancelConfirmationDialog = false },
            title = { Text("Annulla Prenotazione", fontWeight = FontWeight.Bold) },
            text = { Text("Sei sicuro di voler annullare la prenotazione per l'itinerario \"${itinerary.getTitle()}\" e tutte le attività collegate? L'azione non è reversibile.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showCancelConfirmationDialog = false
                        itinerary.getId()?.toString()?.let {
                            viewModel.cancelBooking(it)
                        }
                    }
                ) {
                    Text("Sì, annulla", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelConfirmationDialog = false }) {
                    Text("No, mantieni")
                }
            }
        )
    }

    if (showCancelSuccessDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissCancelSuccess() },
            title = { Text("Prenotazione Annullata", fontWeight = FontWeight.Bold) },
            text = { Text("La tua prenotazione è stata annullata con successo.") },
            confirmButton = {
                Button(
                    onClick = { viewModel.dismissCancelSuccess() }
                ) {
                    Text("Chiudi")
                }
            }
        )
    }

    Scaffold(
        floatingActionButton = {
            if (isViaggiatore && itinerary.creatorId?.toString() != currentUser?.id) {
                FloatingActionButton(
                    onClick = { onPersonalizeClick(itinerary) },
                    shape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Handyman,
                        contentDescription = "Personalizza itinerario"
                    )
                }
            }
        },
        bottomBar = {
            Surface(
                shadowElevation = 12.dp,
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .navigationBarsPadding()
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Prezzo Totale",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "€${String.format("%.2f", totalPrice)}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    if (isBooked) {
                        Button(
                            onClick = {
                                showCancelConfirmationDialog = true
                            },
                            enabled = !isLoading,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer
                            ),
                            modifier = Modifier
                                .width(210.dp)
                                .height(52.dp),
                            shape = RoundedCornerShape(26.dp)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onErrorContainer)
                            } else {
                                Text("Annulla Prenotazione", fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        Button(
                            onClick = { 
                                itinerary.getId()?.toString()?.let {
                                    viewModel.bookItinerary(it)
                                }
                            },
                            enabled = !isLoading,
                            modifier = Modifier
                                .width(160.dp)
                                .height(52.dp),
                            shape = RoundedCornerShape(26.dp)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                            } else {
                                Text("Prenota", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Hero Image Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
            ) {
                AsyncImage(
                    model = itinerary.getImageUrl() ?: "https://via.placeholder.com/800x600?text=No+Image",
                    contentDescription = itinerary.getTitle(),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Overlay Gradient and Back Button
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            androidx.compose.ui.graphics.Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.4f),
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.7f)
                                )
                            )
                        )
                )

                // Floating Circular Back Button (Glassmorphism look)
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .padding(top = 16.dp, start = 16.dp)
                        .statusBarsPadding()
                        .size(44.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Indietro",
                        tint = Color.White
                    )
                }

                // Top Right Action Buttons
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 16.dp, end = 16.dp)
                        .statusBarsPadding(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Delete Button (only if current user is creator)
                    if (currentUser != null && itinerary.creatorId?.toString() == currentUser.id) {
                        IconButton(
                            onClick = {
                                scope.launch {
                                    val idStr = itinerary.id?.toString() ?: ""
                                    val res = AppContainer.itineraryRepository.deleteItinerary(idStr)
                                    res.fold(
                                        onSuccess = {
                                            android.widget.Toast.makeText(context, "Itinerario eliminato con successo", android.widget.Toast.LENGTH_SHORT).show()
                                            onDeleteSuccess()
                                        },
                                        onFailure = { err ->
                                            android.widget.Toast.makeText(context, "Errore: ${err.message}", android.widget.Toast.LENGTH_LONG).show()
                                        }
                                    )
                                }
                            },
                            modifier = Modifier
                                .size(44.dp)
                                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Elimina Itinerario",
                                tint = Color.White
                            )
                        }
                    }

                    // Calendar Export Button
                    IconButton(
                        onClick = {
                            val locs = itinerary.getActivities()?.mapNotNull { it.getLocation() }?.filter { it.isNotBlank() }?.distinct()?.joinToString(", ")
                            CalendarExportUtil.addToCalendar(
                                context = context,
                                title = itinerary.getTitle() ?: "Itinerario",
                                description = itinerary.getDescription(),
                                location = locs,
                                startTime = itinerary.getStartDateTime(),
                                endTime = itinerary.getEndDateTime()
                            )
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = "Esporta Calendario",
                            tint = Color.White
                        )
                    }

                    // Floating Circular Favorite Button
                    val isMyItinerary = itinerary.creatorId?.toString() == currentUser?.id
                    if (!isMyItinerary) {
                        IconButton(
                            onClick = onFavoriteClick,
                            modifier = Modifier
                                .size(44.dp)
                                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        ) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Preferito",
                                tint = if (isFavorite) Color.Red else Color.White
                            )
                        }
                    }
                }

                // Title Overlay at Bottom of Image
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(horizontal = 24.dp, vertical = 20.dp)
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "ITINERARIO",
                            color = MaterialTheme.colorScheme.onPrimary,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = itinerary.getTitle() ?: "Senza Titolo",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    itinerary.getCreatorName()?.let { creatorName ->
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Organizzato da: $creatorName",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
            }

            // Itinerary Details Card
            Column(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Info block: Dates & Locations
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Date Card
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text(
                                    text = "Periodo",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                val start = itinerary.getStartDateTime()
                                val end = itinerary.getEndDateTime()
                                val dateText = if (start != null && end != null) {
                                    "${formatDate(start)} - ${formatDate(end)}"
                                } else {
                                    "Date non specificate"
                                }
                                Text(
                                    text = dateText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    // Location Card
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text(
                                    text = "Destinazioni",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                val locationText = if (uniqueLocations.isNotEmpty()) {
                                    uniqueLocations.joinToString(", ")
                                } else {
                                    "Non specificata"
                                }
                                Text(
                                    text = locationText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                // Tags aggregate
                if (uniqueTags.isNotEmpty()) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        uniqueTags.forEach { tag ->
                            val formattedTag = tag.name.lowercase().replaceFirstChar { 
                                if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() 
                            }
                            val bgColor = try {
                                Color(android.graphics.Color.parseColor(tag.getBgColorHex()))
                            } catch (e: Exception) {
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                            }
                            val textColor = try {
                                Color(android.graphics.Color.parseColor(tag.getTextColorHex()))
                            } catch (e: Exception) {
                                MaterialTheme.colorScheme.primary
                            }
                            Surface(
                                color = bgColor,
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, textColor.copy(alpha = 0.2f))
                            ) {
                                Text(
                                    text = formattedTag,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = textColor,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                // Description section
                if (!itinerary.getDescription().isNullOrBlank()) {
                    Text(
                        text = "Descrizione",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = itinerary.getDescription() ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.3f
                    )
                }

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                // Timeline header
                Text(
                    text = "Programma e Attività",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                // Timeline View of activities
                val activitiesList = itinerary.getActivities() ?: emptyList()
                if (activitiesList.isEmpty()) {
                    Text(
                        text = "Nessuna attività programmata per questo itinerario.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    activitiesList.forEachIndexed { index, activity ->
                        ActivityTimelineRow(
                            index = index + 1,
                            activity = activity,
                            isLast = index == activitiesList.lastIndex,
                            onClick = { activity.id?.toString()?.let(onActivityClick) }
                        )
                    }
                }

                // REVIEWS SECTION
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    modifier = Modifier.padding(vertical = 16.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recensioni",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                // Form inline per aggiungere una nuova recensione
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        AddReviewInline(
                            onSubmit = { rating, comment ->
                                scope.launch {
                                    val id = itinerary.id?.toString()
                                    if (id != null) {
                                        val newReview = CreateReviewDto(
                                            itineraryId = id,
                                            rating = rating,
                                            comment = comment
                                        )
                                        AppContainer.reviewRepository.createReview(newReview)
                                        val reviewsResult = AppContainer.reviewRepository.getReviewsForItinerary(id)
                                        if (reviewsResult.isSuccess) {
                                            reviews = reviewsResult.getOrNull() ?: emptyList()
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
                
                if (reviews.isEmpty()) {
                    Text(
                        text = "Ancora nessuna recensione. Sii il primo!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        reviews.forEach { review ->
                            // Here showActivityName is true to distinguish which activity the review is for
                            ReviewCard(
                                review = review, 
                                showActivityName = true,
                                onUpdate = { updatedRating, updatedComment ->
                                    scope.launch {
                                        val id = itinerary.id?.toString()
                                        if (id != null) {
                                            val updateDto = CreateReviewDto(
                                                itineraryId = id,
                                                rating = updatedRating,
                                                comment = updatedComment
                                            )
                                            review.id?.let { reviewId ->
                                                AppContainer.reviewRepository.updateReview(reviewId, updateDto)
                                                val reviewsResult = AppContainer.reviewRepository.getReviewsForItinerary(id)
                                                if (reviewsResult.isSuccess) {
                                                    reviews = reviewsResult.getOrNull() ?: emptyList()
                                                }
                                            }
                                        }
                                    }
                                },
                                onDelete = {
                                    scope.launch {
                                        val id = itinerary.id?.toString()
                                        if (id != null) {
                                            review.id?.let { reviewId ->
                                                AppContainer.reviewRepository.deleteReview(reviewId)
                                                val reviewsResult = AppContainer.reviewRepository.getReviewsForItinerary(id)
                                                if (reviewsResult.isSuccess) {
                                                    reviews = reviewsResult.getOrNull() ?: emptyList()
                                                }
                                            }
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showCheckoutSummary && bookingId != null) {
        CheckoutSummaryScreen(
            bookingId = bookingId!!,
            title = itinerary.getTitle() ?: "Itinerario",
            totalPrice = totalPrice,
            isItinerary = true,
            activities = itinerary.getActivities() ?: emptyList(),
            userEmail = currentUser?.email.orEmpty(),
            isConfirming = isLoading,
            onConfirm = {
                if (clientSecret != null) {
                    startPayPalCheckout = true
                } else {
                    viewModel.confirmBooking()
                }
            },
            onCancel = {
                itinerary.getId()?.toString()?.let {
                    viewModel.cancelBooking(it)
                }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ActivityTimelineRow(
    index: Int,
    activity: ActivityDto,
    isLast: Boolean,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min) // essential to allow the vertical line to stretch to fill height
    ) {
        // Timeline node indicator on left
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(36.dp)
        ) {
            // Node circle
            Surface(
                modifier = Modifier.size(28.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = index.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // Connector line (only if it's not the last element)
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(2.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Activity detail card on right
        Card(
            onClick = onClick,
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Header (Name & Price)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = activity.getName() ?: "Attività Senza Nome",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = activity.getPrice()?.let { "€$it" } ?: "Gratis",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Info: Location
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = activity.getLocation() ?: "N/D",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Description
                if (!activity.getDescription().isNullOrBlank()) {
                    Text(
                        text = activity.getDescription() ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f)
                    )
                }

                // Activity tags
                val tags = activity.getTags() ?: emptySet()
                if (tags.isNotEmpty()) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        tags.forEach { tag ->
                            val formattedTag = tag.name.lowercase().replaceFirstChar { 
                                if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() 
                            }
                            val bgColor = try {
                                Color(android.graphics.Color.parseColor(tag.getBgColorHex()))
                            } catch (e: Exception) {
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                            }
                            val textColor = try {
                                Color(android.graphics.Color.parseColor(tag.getTextColorHex()))
                            } catch (e: Exception) {
                                MaterialTheme.colorScheme.primary
                            }
                            Surface(
                                color = bgColor,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = formattedTag,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = textColor,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun formatDate(dateTime: LocalDateTime): String {
    return try {
        val formatter = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ITALIAN)
        dateTime.format(formatter)
    } catch (e: Exception) {
        dateTime.toString()
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun SuccessAnimationScreen(
    title: String,
    onDismiss: () -> Unit
) {
    var checkmarkScale by remember { mutableStateOf(0f) }
    var cardAlpha by remember { mutableStateOf(0f) }
    var textAlpha by remember { mutableStateOf(0f) }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(100)
        cardAlpha = 1f
        kotlinx.coroutines.delay(200)
        checkmarkScale = 1.2f
        kotlinx.coroutines.delay(150)
        checkmarkScale = 1.0f
        kotlinx.coroutines.delay(100)
        textAlpha = 1f
    }

    val scale by androidx.compose.animation.core.animateFloatAsState(
        targetValue = checkmarkScale,
        animationSpec = androidx.compose.animation.core.spring(
            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
            stiffness = androidx.compose.animation.core.Spring.StiffnessMedium
        ),
        label = "scale"
    )

    val alphaCard by androidx.compose.animation.core.animateFloatAsState(
        targetValue = cardAlpha,
        animationSpec = androidx.compose.animation.core.tween(500),
        label = "cardAlpha"
    )

    val alphaText by androidx.compose.animation.core.animateFloatAsState(
        targetValue = textAlpha,
        animationSpec = androidx.compose.animation.core.tween(400),
        label = "textAlpha"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f * alphaCard)),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .width(320.dp)
                    .graphicsLayer(alpha = alphaCard, scaleX = alphaCard, scaleY = alphaCard),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .graphicsLayer(scaleX = scale, scaleY = scale)
                            .background(
                                MaterialTheme.colorScheme.primaryContainer,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Success",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Prenotazione Confermata!",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.graphicsLayer(alpha = alphaText)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Hai prenotato con successo l'itinerario:\n$title",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.graphicsLayer(alpha = alphaText)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .graphicsLayer(alpha = alphaText),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text("Chiudi", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
