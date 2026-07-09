package com.travel.app.presentation.components.activity

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.travel.app.data.AppContainer
import it.unical.ea.dtos.activity.ActivityDto
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import com.travel.app.domain.model.review.ReviewDto
import com.travel.app.domain.model.review.CreateReviewDto
import com.travel.app.presentation.components.review.ReviewCard
import com.travel.app.presentation.components.review.AddReviewInline
import kotlinx.coroutines.launch
import com.travel.app.utils.CalendarExportUtil
import android.widget.Toast
import com.paypal.checkout.PayPalCheckout
import com.paypal.checkout.approve.OnApprove
import com.paypal.checkout.cancel.OnCancel
import com.paypal.checkout.error.OnError
import com.travel.app.presentation.components.checkout.CheckoutSummaryScreen
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.material.icons.filled.Check
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun ActivityDetailScreen(
    activityId: String,
    onNavigateBack: () -> Unit,
    isFavorite: Boolean = false,
    onFavoriteClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var activity by remember { mutableStateOf<ActivityDto?>(null) }
    var selectedSession by remember { mutableStateOf<ActivityDto?>(null) }
    var showDateSelectorDialog by remember { mutableStateOf(false) }
    val currentSession = selectedSession ?: activity
    var isLoading by remember { mutableStateOf(true) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    
    var isBooked by remember { mutableStateOf(false) }
    var paymentClientSecret by remember { mutableStateOf<String?>(null) }
    var currentBookingId by remember { mutableStateOf<String?>(null) }
    var showCheckoutSummary by remember { mutableStateOf(false) }
    var currentUserEmail by remember { mutableStateOf("") }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showCancelConfirmationDialog by remember { mutableStateOf(false) }
    var showCancelSuccessDialog by remember { mutableStateOf(false) }
    var showReceiptDialog by remember { mutableStateOf(false) }
    
    var reviews by remember { mutableStateOf<List<ReviewDto>>(emptyList()) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(activityId) {
        isLoading = true
        errorMsg = null
        val result = AppContainer.activityRepository.getActivityById(activityId)
        result.fold(
            onSuccess = {
                activity = it
                selectedSession = it
                isLoading = false
                // Increment location score when viewed
                it.location?.split(",")?.firstOrNull()?.trim()?.let { city ->
                    AppContainer.sessionManager.incrementLocationScore(city, 1)
                }
                scope.launch {
                    val targetTemplateId = it.templateId?.toString() ?: activityId
                    val reviewsResult = AppContainer.reviewRepository.getReviewsForActivity(targetTemplateId)
                    if (reviewsResult.isSuccess) {
                        reviews = reviewsResult.getOrNull() ?: emptyList()
                    }
                }
            },
            onFailure = {
                errorMsg = it.message ?: "Impossibile caricare i dettagli dell'attività"
                isLoading = false
            }
        )
        currentUserEmail = AppContainer.sessionManager.getSessionUser()?.email.orEmpty()
    }

    LaunchedEffect(selectedSession) {
        val currentId = selectedSession?.id?.toString() ?: return@LaunchedEffect
        val bookedResult = AppContainer.activityRepository.isActivityBooked(currentId)
        if (bookedResult.isSuccess) {
            isBooked = bookedResult.getOrDefault(false)
        }
    }

    val context = androidx.compose.ui.platform.LocalContext.current
    val isOnline by AppContainer.networkMonitor.isOnline.collectAsState(initial = true)

    LaunchedEffect(Unit) {
        PayPalCheckout.registerCallbacks(
            onApprove = OnApprove { approval ->
                Toast.makeText(context, "Pagamento completato!", Toast.LENGTH_SHORT).show()
                scope.launch {
                    val bId = currentBookingId
                    if (bId != null) {
                        isLoading = true
                        val confirmRes = AppContainer.activityRepository.confirmActivityBooking(bId)
                        isLoading = false
                        if (confirmRes.isSuccess) {
                            isBooked = true
                            showCheckoutSummary = false
                            showSuccessDialog = true
                        } else {
                            Toast.makeText(context, "Errore nella conferma: ${confirmRes.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                    paymentClientSecret = null
                    currentBookingId = null
                }
            },
            onCancel = OnCancel {
                Toast.makeText(context, "Pagamento annullato", Toast.LENGTH_SHORT).show()
                paymentClientSecret = null
                currentBookingId = null
            },
            onError = OnError { errorInfo ->
                Toast.makeText(context, "Errore nel pagamento: ${errorInfo.error.message}", Toast.LENGTH_LONG).show()
                paymentClientSecret = null
                currentBookingId = null
            }
        )
    }

    var startPayPalCheckout by remember { mutableStateOf(false) }
    LaunchedEffect(startPayPalCheckout, paymentClientSecret) {
        if (startPayPalCheckout && paymentClientSecret != null) {
            PayPalCheckout.startCheckout(com.paypal.checkout.createorder.CreateOrder { createOrderActions ->
                createOrderActions.set(paymentClientSecret!!)
            })
            startPayPalCheckout = false
        }
    }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            if (activity != null) {
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
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Prezzo dell'attività",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            val priceVal = currentSession!!.price?.toDouble() ?: 0.0
                            Text(
                                text = if (priceVal == 0.0) "Gratis" else "€${String.format(Locale.getDefault(), "%.2f", priceVal)}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val hasStarted = currentSession!!.startTime?.isBefore(LocalDateTime.now()) == true
                            val isFull = currentSession.participants != null && (currentSession.currentParticipants ?: 0) >= currentSession.participants

                            if (isBooked) {
                                IconButton(
                                    onClick = { showReceiptDialog = true },
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Receipt,
                                        contentDescription = "Ricevuta",
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                                Button(
                                    onClick = { showCancelConfirmationDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                    contentPadding = PaddingValues(horizontal = 12.dp),
                                    modifier = Modifier.height(48.dp),
                                    shape = RoundedCornerShape(24.dp)
                                ) {
                                    Text(
                                        text = "Annulla Prenotazione",
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                            } else if (hasStarted) {
                                Button(
                                    onClick = {},
                                    enabled = false,
                                    contentPadding = PaddingValues(horizontal = 12.dp),
                                    modifier = Modifier.height(48.dp),
                                    shape = RoundedCornerShape(24.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                        disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                ) {
                                    Text(
                                        text = "Evento terminato",
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                            } else if (isFull) {
                                Button(
                                    onClick = {},
                                    enabled = false,
                                    contentPadding = PaddingValues(horizontal = 12.dp),
                                    modifier = Modifier.height(48.dp),
                                    shape = RoundedCornerShape(24.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                        disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                ) {
                                    Text(
                                        text = "Posti esauriti",
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                            } else {
                                Button(
                                    onClick = {
                                        if (!isOnline) {
                                            Toast.makeText(context, "Sei offline. Questa azione richiede una connessione internet attiva.", Toast.LENGTH_SHORT).show()
                                            return@Button
                                        }
                                        scope.launch {
                                            isLoading = true
                                            val bookRes = AppContainer.activityRepository.bookActivity(currentSession.id.toString())
                                            isLoading = false
                                            bookRes.fold(
                                                onSuccess = { response ->
                                                    currentBookingId = response.bookingId
                                                    paymentClientSecret = response.clientSecret
                                                    showCheckoutSummary = true
                                                },
                                                onFailure = {
                                                    Toast.makeText(context, "Errore nella prenotazione: ${it.message}", Toast.LENGTH_LONG).show()
                                                }
                                            )
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    contentPadding = PaddingValues(horizontal = 12.dp),
                                    modifier = Modifier.height(48.dp),
                                    shape = RoundedCornerShape(24.dp)
                                ) {
                                    Text(
                                        text = "Prenota",
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator()
            } else if (errorMsg != null) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(text = errorMsg!!, color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onNavigateBack) {
                        Text("Torna Indietro")
                    }
                }
            } else if (activity != null) {
                val act = activity!!
                val scrollState = rememberScrollState()
                
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                ) {
                    // Hero Image Header
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                    ) {
                        val imagesList = act.images.orEmpty().ifEmpty {
                            listOf("https://images.unsplash.com/photo-1488646953014-85cb44e25828?ixlib=rb-1.2.1&auto=format&fit=crop&w=800&q=80")
                        }
                        val pagerState = rememberPagerState(pageCount = { imagesList.size })

                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier.fillMaxSize()
                        ) { page ->
                            val imageUrl = imagesList[page]
                            AsyncImage(
                                model = imageUrl,
                                contentDescription = act.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        // Overlay Gradient
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

                        // Pager Indicator (dot indicators)
                        if (imagesList.size > 1) {
                            Row(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                repeat(imagesList.size) { index ->
                                    val isSelected = pagerState.currentPage == index
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(
                                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.5f),
                                                shape = CircleShape
                                            )
                                    )
                                }
                            }
                        }

                        // Floating Circular Back Button
                        IconButton(
                            onClick = onNavigateBack,
                            modifier = Modifier
                                .padding(top = 16.dp, start = 16.dp)
                                .statusBarsPadding()
                                .size(44.dp)
                                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
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
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Calendar Export Button
                            IconButton(
                                onClick = {
                                    CalendarExportUtil.addToCalendar(
                                        context = context,
                                        title = act.name ?: "Attività",
                                        description = act.description,
                                        location = act.location,
                                        startTime = currentSession!!.startTime,
                                        endTime = currentSession!!.endTime
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
                            
                            // Favorite Button
                            if (!isBooked) {
                                IconButton(
                                    onClick = {
                                        onFavoriteClick()
                                        if (!isFavorite) {
                                            act.location?.split(",")?.firstOrNull()?.trim()?.let { city ->
                                                AppContainer.sessionManager.incrementLocationScore(city, 3)
                                            }
                                        }
                                    },
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

                        // Title Overlay
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
                                    text = "ATTIVITÀ",
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = act.name ?: "Nessun nome",
                                color = Color.White,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Content Details
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Info block: Dates & Locations
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Date Card
                            val hasMultipleSessions = (act.sessions?.size ?: 0) > 1
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    if (hasMultipleSessions) {
                                        showDateSelectorDialog = true
                                    }
                                }
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
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "Data",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            if (hasMultipleSessions) {
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "(Cambia)",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                        val start = currentSession!!.startTime
                                        val dateText = if (start != null) formatDate(start) else "Non specificata"
                                        Text(
                                            text = dateText,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }

                            // Time Card
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
                                        imageVector = Icons.Default.Schedule,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Column {
                                        Text(
                                            text = "Orario",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        val startVal = currentSession!!.startTime
                                        val endVal = currentSession!!.endTime
                                        val timeText = if (startVal != null && endVal != null) {
                                            "${formatTime(startVal)} - ${formatTime(endVal)}"
                                        } else {
                                            "Non specificato"
                                        }
                                        Text(
                                            text = timeText,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }

                        // Organizer and Location Info
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = "Posizione",
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = act.location ?: "Nessuna località",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.People,
                                        contentDescription = "Posti",
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    val current = currentSession!!.currentParticipants ?: 0
                                    val total = currentSession!!.participants ?: 0
                                    Text(
                                        text = "Partecipanti: $current / $total posti",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }

                        Text(
                            text = "Organizzato da: ${act.organizer ?: "Operatore Locale"}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )

                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                            modifier = Modifier.padding(vertical = 8.dp)
                        )

                        // Description
                        Text(
                            text = "Descrizione",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = act.description ?: "Nessuna descrizione disponibile.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.3f
                        )

                        // Tags
                        val tags = act.tags ?: emptySet()
                        if (tags.isNotEmpty()) {
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                modifier = Modifier.padding(vertical = 8.dp)
                            )

                            Text(
                                text = "Tag / Categorie",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
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
                                            val targetId = activity?.templateId?.toString() ?: activityId
                                            val newReview = CreateReviewDto(
                                                activityId = targetId,
                                                rating = rating,
                                                comment = comment
                                            )
                                            AppContainer.reviewRepository.createReview(newReview)
                                            val reviewsResult = AppContainer.reviewRepository.getReviewsForActivity(targetId)
                                            if (reviewsResult.isSuccess) {
                                                reviews = reviewsResult.getOrNull() ?: emptyList()
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
                                    ReviewCard(
                                        review = review, 
                                        showActivityName = false,
                                        onUpdate = { updatedRating, updatedComment ->
                                            scope.launch {
                                                val updateDto = CreateReviewDto(
                                                    activityId = activityId,
                                                    rating = updatedRating,
                                                    comment = updatedComment
                                                )
                                                review.id?.let {
                                                    AppContainer.reviewRepository.updateReview(it, updateDto)
                                                    val reviewsResult = AppContainer.reviewRepository.getReviewsForActivity(activityId)
                                                    if (reviewsResult.isSuccess) {
                                                        reviews = reviewsResult.getOrNull() ?: emptyList()
                                                    }
                                                }
                                            }
                                        },
                                        onDelete = {
                                            scope.launch {
                                                review.id?.let {
                                                    AppContainer.reviewRepository.deleteReview(it)
                                                    val reviewsResult = AppContainer.reviewRepository.getReviewsForActivity(activityId)
                                                    if (reviewsResult.isSuccess) {
                                                        reviews = reviewsResult.getOrNull() ?: emptyList()
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
        }
    }

    if (showCheckoutSummary && currentBookingId != null && activity != null && currentSession != null) {
        CheckoutSummaryScreen(
            bookingId = currentBookingId!!,
            title = activity?.name ?: "Attività",
            totalPrice = currentSession!!.price?.toDouble() ?: 0.0,
            isItinerary = false,
            activities = listOf(currentSession!!),
            userEmail = currentUserEmail,
            isConfirming = isLoading,
            onConfirm = {
                if (paymentClientSecret != null) {
                    startPayPalCheckout = true
                } else {
                    scope.launch {
                        val bId = currentBookingId
                        if (bId != null) {
                            isLoading = true
                            val confirmRes = AppContainer.activityRepository.confirmActivityBooking(bId)
                            isLoading = false
                            if (confirmRes.isSuccess) {
                                isBooked = true
                                showCheckoutSummary = false
                                showSuccessDialog = true
                            } else {
                                Toast.makeText(context, "Errore nella conferma: ${confirmRes.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }
            },
            onCancel = {
                scope.launch {
                    isLoading = true
                    try {
                        AppContainer.activityRepository.cancelActivityBooking(currentSession!!.id.toString())
                    } catch (_: Exception) {
                        // Ignora errori, chiudi comunque
                    }
                    showCheckoutSummary = false
                    currentBookingId = null
                    paymentClientSecret = null
                    isLoading = false
                }
            }
        )
    }

    if (showSuccessDialog && activity != null) {
        SuccessAnimationScreen(
            title = activity!!.name ?: "Attività",
            onDismiss = { showSuccessDialog = false }
        )
    }

    if (showCancelConfirmationDialog && currentSession != null) {
        AlertDialog(
            onDismissRequest = { showCancelConfirmationDialog = false },
            title = { Text("Annulla Prenotazione", fontWeight = FontWeight.Bold) },
            text = { Text("Sei sicuro di voler annullare la prenotazione per l'attività \"${activity?.name}\"? L'azione non è reversibile.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (!isOnline) {
                            Toast.makeText(context, "Sei offline. Questa azione richiede una connessione internet attiva.", Toast.LENGTH_SHORT).show()
                            showCancelConfirmationDialog = false
                            return@TextButton
                        }
                        showCancelConfirmationDialog = false
                        scope.launch {
                            isLoading = true
                            val cancelRes = AppContainer.activityRepository.cancelActivityBooking(currentSession!!.id.toString())
                            isLoading = false
                            cancelRes.fold(
                                onSuccess = {
                                    isBooked = false
                                    showCancelSuccessDialog = true
                                },
                                onFailure = {
                                    Toast.makeText(context, "Errore nella cancellazione: ${it.message}", Toast.LENGTH_LONG).show()
                                }
                            )
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
            onDismissRequest = { showCancelSuccessDialog = false },
            title = { Text("Prenotazione Annullata", fontWeight = FontWeight.Bold) },
            text = { Text("La tua prenotazione è stata annullata con successo ed è stato emesso il relativo rimborso.") },
            confirmButton = {
                TextButton(onClick = { showCancelSuccessDialog = false }) {
                    Text("OK", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    if (showReceiptDialog && activity != null && currentSession != null) {
        CheckoutSummaryScreen(
            bookingId = currentSession.id?.toString() ?: "N/A",
            title = activity?.name ?: "Attività",
            totalPrice = currentSession.price?.toDouble() ?: 0.0,
            isItinerary = false,
            activities = listOf(currentSession),
            userEmail = currentUserEmail,
            isConfirming = false,
            onConfirm = {},
            onCancel = { showReceiptDialog = false },
            isReadOnly = true
        )
    }

    if (showDateSelectorDialog && activity != null && currentSession != null) {
        val sessionsList = activity?.sessions.orEmpty()
        AlertDialog(
            onDismissRequest = { showDateSelectorDialog = false },
            title = { Text("Seleziona una data", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (session in sessionsList) {
                        val start = session.startTime
                        val end = session.endTime
                        val isSelected = session.id == currentSession!!.id
                        
                        val formattedDate = if (start != null) formatDate(start) else "N/D"
                        val formattedTime = if (start != null && end != null) "${formatTime(start)} - ${formatTime(end)}" else "N/D"
                        val current = session.currentParticipants ?: 0
                        val total = session.participants ?: 0
                        val spotsLeft = total - current

                        Surface(
                            onClick = {
                                selectedSession = session
                                showDateSelectorDialog = false
                            },
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(
                                width = 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = formattedDate,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = formattedTime,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Posti disponibili: $spotsLeft / $total",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selezionata",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDateSelectorDialog = false }) {
                    Text("Annulla")
                }
            }
        )
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
private fun formatTime(dateTime: LocalDateTime): String {
    return try {
        val formatter = DateTimeFormatter.ofPattern("HH:mm", Locale.ITALIAN)
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
                        text = "Hai prenotato con successo l'attività:\n$title",
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
                        Text("Fantastico", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
