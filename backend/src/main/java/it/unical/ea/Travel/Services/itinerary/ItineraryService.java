package it.unical.ea.Travel.Services.itinerary;

import it.unical.ea.Travel.Config.SecurityUtils;
import it.unical.ea.Travel.Entities.activity.Activity;
import it.unical.ea.Travel.Entities.activity.ActivityBooking;
import it.unical.ea.Travel.Entities.itinerary.Itinerary;
import it.unical.ea.Travel.Entities.itinerary.ItineraryBooking;
import it.unical.ea.Travel.Entities.payment.BookingStatus;
import it.unical.ea.Travel.Entities.user.User;
import it.unical.ea.Travel.Exception.ApiException;
import it.unical.ea.Travel.Repositories.activity.ActivityBookingRepository;
import it.unical.ea.Travel.Repositories.activity.ActivityRepository;
import it.unical.ea.Travel.Repositories.itinerary.ItineraryBookingRepository;
import it.unical.ea.Travel.Repositories.itinerary.ItineraryRepository;
import it.unical.ea.Travel.Repositories.user.UserRepository;
import it.unical.ea.Travel.Services.activity.ActivityService;
import it.unical.ea.Travel.Services.audit.AuditLogService;
import it.unical.ea.Travel.Services.auth.AuthorizationService;
import it.unical.ea.Travel.Services.notification.NotificationService;
import it.unical.ea.Travel.Services.payment.PaymentGateway;
import it.unical.ea.Travel.Services.storage.FileStorageService;
import it.unical.ea.dtos.itinerary.CreateItineraryRequest;
import it.unical.ea.dtos.payment.PaymentIntentResponseDto;
import it.unical.ea.Travel.Entities.itinerary.ItineraryJoinRequest;
import it.unical.ea.Travel.Repositories.itinerary.ItineraryJoinRequestRepository;
import it.unical.ea.dtos.itinerary.ItineraryParticipantDto;
import it.unical.ea.enums.JoinRequestStatus;
import it.unical.ea.enums.NotificationType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ItineraryService {

    private static final String ITINERARIES_SUBDIR = "itineraries";
    private static final String CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int CODE_LENGTH = 6;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final ItineraryRepository itineraryRepository;
    private final UserRepository userRepository;
    private final ActivityRepository activityRepository;
    private final FileStorageService fileStorageService;
    private final ItineraryBookingRepository itineraryBookingRepository;
    private final ActivityBookingRepository activityBookingRepository;
    private final ActivityService activityService;
    private final AuditLogService auditLogService;
    private final PaymentGateway paymentGateway;
    private final NotificationService notificationService;
    private final AuthorizationService authorizationService;
    private final ItineraryJoinRequestRepository itineraryJoinRequestRepository;

    @Value("${payment.mock:true}")
    private boolean paymentMock;

    @Autowired
    public ItineraryService(ItineraryRepository itineraryRepository,
            UserRepository userRepository,
            ActivityRepository activityRepository,
            FileStorageService fileStorageService,
            ItineraryBookingRepository itineraryBookingRepository,
            ActivityBookingRepository activityBookingRepository,
            ActivityService activityService,
            AuditLogService auditLogService,
            PaymentGateway paymentGateway,
            NotificationService notificationService,
            AuthorizationService authorizationService,
            ItineraryJoinRequestRepository itineraryJoinRequestRepository) {
        this.itineraryRepository = itineraryRepository;
        this.userRepository = userRepository;
        this.activityRepository = activityRepository;
        this.fileStorageService = fileStorageService;
        this.itineraryBookingRepository = itineraryBookingRepository;
        this.activityBookingRepository = activityBookingRepository;
        this.activityService = activityService;
        this.auditLogService = auditLogService;
        this.paymentGateway = paymentGateway;
        this.notificationService = notificationService;
        this.authorizationService = authorizationService;
        this.itineraryJoinRequestRepository = itineraryJoinRequestRepository;
    }

    public String generateUniqueShareCode() {
        String code;
        do {
            StringBuilder sb = new StringBuilder(CODE_LENGTH);
            for (int i = 0; i < CODE_LENGTH; i++) {
                sb.append(CODE_CHARS.charAt(RANDOM.nextInt(CODE_CHARS.length())));
            }
            code = sb.toString();
        } while (itineraryRepository.findByShareCode(code).isPresent());
        return code;
    }

    public String ensureShareCode(Itinerary itinerary) {
        if (itinerary != null && "SHARED".equalsIgnoreCase(itinerary.getVisibility()) && (itinerary.getShareCode() == null || itinerary.getShareCode().isBlank())) {
            String code = generateUniqueShareCode();
            itinerary.setShareCode(code);
            itineraryRepository.save(itinerary);
            return code;
        }
        return itinerary != null ? itinerary.getShareCode() : null;
    }

    // Riceve tutti gli itinerari dal database
    public List<Itinerary> getAllItineraries() {
        return getAllItineraries(null, false);
    }

    public List<Itinerary> getAllItineraries(String userEmail, boolean isAdmin) {
        if (isAdmin) {
            return itineraryRepository.findAll();
        }
        if (userEmail != null) {
            User user = userRepository.getUserByEmail(userEmail).orElse(null);
            if (user != null) {
                return itineraryRepository.findPublicOrCreatorOrParticipantItineraries(user.getId());
            }
        }
        return itineraryRepository.findByVisibilityIgnoreCase("PUBLIC");
    }

    public Itinerary getItinerary(String stringId) {
        UUID uuid = UUID.fromString(stringId);
        return itineraryRepository.findById(uuid)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "itinerary.notFound"));
    }

    public Itinerary getItinerary(String stringId, String userEmail, boolean isAdmin) {
        Itinerary itinerary = getItinerary(stringId);
        String visibility = itinerary.getVisibility() != null ? itinerary.getVisibility().trim() : "PRIVATE";
        if ("PRIVATE".equalsIgnoreCase(visibility)) {
            boolean isOwner = userEmail != null && itinerary.getCreator() != null
                    && userEmail.equalsIgnoreCase(itinerary.getCreator().getEmail());
            if (!isAdmin && !isOwner) {
                throw new ApiException(HttpStatus.FORBIDDEN, "error.forbidden");
            }
        } else if ("SHARED".equalsIgnoreCase(visibility)) {
            boolean isOwner = userEmail != null && itinerary.getCreator() != null
                    && userEmail.equalsIgnoreCase(itinerary.getCreator().getEmail());
            boolean isAcceptedParticipant = false;
            if (userEmail != null) {
                User user = userRepository.getUserByEmail(userEmail).orElse(null);
                if (user != null) {
                    isAcceptedParticipant = itineraryJoinRequestRepository.existsByUserIdAndItineraryIdAndStatus(
                            user.getId(), itinerary.getId(), JoinRequestStatus.ACCEPTED);
                }
            }
            if (!isAdmin && !isOwner && !isAcceptedParticipant) {
                throw new ApiException(HttpStatus.FORBIDDEN, "error.forbidden");
            }
        }
        return itinerary;
    }

    public List<Itinerary> getItinerariesByCreator(String creatorStringId) {
        return getItinerariesByCreator(creatorStringId, null, false);
    }

    public List<Itinerary> getItinerariesByCreator(String creatorStringId, String userEmail, boolean isAdmin) {
        UUID creatorId = UUID.fromString(creatorStringId);
        if (isAdmin) {
            return itineraryRepository.findByCreatorId(creatorId);
        }
        if (userEmail != null) {
            User requester = userRepository.getUserByEmail(userEmail).orElse(null);
            if (requester != null && requester.getId().equals(creatorId)) {
                return itineraryRepository.findByCreatorId(creatorId);
            }
        }
        return itineraryRepository.findByCreatorIdAndVisibilityIgnoreCase(creatorId, "PUBLIC");
    }

    public Itinerary createItinerary(Itinerary itinerary, List<String> activityStringIds) {
        String email = SecurityUtils.getCurrentUserEmail();
        User creator = userRepository.getUserByEmail(email)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "user.notFound"));
        itinerary.setCreator(creator);

        if (itinerary.getVisibility() != null && "SHARED".equalsIgnoreCase(itinerary.getVisibility().trim())) {
            if (itinerary.getShareCode() == null || itinerary.getShareCode().isBlank()) {
                itinerary.setShareCode(generateUniqueShareCode());
            }
        }

        linkAndValidateActivities(itinerary, activityStringIds);

        Itinerary saved = itineraryRepository.save(itinerary);
        auditLogService.log("CREATE_ITINERARY", "Itinerary", saved.getId().toString(),
                "Created itinerary: " + saved.getTitle());
        return saved;
    }

    public Itinerary updateItinerary(String stringId, CreateItineraryRequest request) {
        Itinerary itinerary = getItinerary(stringId);
        authorizationService.verifyOwnershipOrAdmin(
                itinerary.getCreator() != null ? itinerary.getCreator().getId() : null, "itinerary");

        itinerary.setTitle(request.getTitle());
        itinerary.setDescription(request.getDescription());
        if (request.getStartDateTime() != null) {
            itinerary.setStartDateTime(request.getStartDateTime());
        }
        if (request.getEndDateTime() != null) {
            itinerary.setEndDateTime(request.getEndDateTime());
        }
        itinerary.setVisibility(request.getVisibility() != null ? request.getVisibility() : "PRIVATE");

        if ("SHARED".equalsIgnoreCase(itinerary.getVisibility().trim())) {
            if (itinerary.getShareCode() == null || itinerary.getShareCode().isBlank()) {
                itinerary.setShareCode(generateUniqueShareCode());
            }
        }

        if (request.getActivityIds() != null) {
            linkAndValidateActivities(itinerary, request.getActivityIds());
        }

        Itinerary saved = itineraryRepository.save(itinerary);
        auditLogService.log("UPDATE_ITINERARY", "Itinerary", saved.getId().toString(),
                "Updated itinerary: " + saved.getTitle());
        return saved;
    }

    public void deleteItinerary(String stringId) {
        Itinerary itinerary = getItinerary(stringId);
        authorizationService.verifyOwnershipOrAdmin(
                itinerary.getCreator() != null ? itinerary.getCreator().getId() : null, "itinerary");
        itineraryRepository.delete(itinerary);
        auditLogService.log("DELETE_ITINERARY", "Itinerary", stringId, "Deleted itinerary with ID: " + stringId);
    }

    public Itinerary uploadImage(String itineraryId, MultipartFile file) {
        Itinerary itinerary = getItinerary(itineraryId);
        authorizationService.verifyOwnershipOrAdmin(
                itinerary.getCreator() != null ? itinerary.getCreator().getId() : null, "itinerary");

        // Se esiste già un'immagine, elimina quella vecchia
        if (itinerary.getImagePath() != null) {
            fileStorageService.delete(itinerary.getImagePath());
        }
        String relativePath = fileStorageService.store(file, ITINERARIES_SUBDIR);
        itinerary.setImagePath(relativePath);
        return itineraryRepository.save(itinerary);
    }

    public Resource getImage(String itineraryId) {
        Itinerary itinerary = getItinerary(itineraryId);
        if (itinerary.getImagePath() == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "itinerary.imageNotFound");
        }
        return fileStorageService.load(itinerary.getImagePath());
    }

    public Itinerary deleteImage(String itineraryId) {
        Itinerary itinerary = getItinerary(itineraryId);
        authorizationService.verifyOwnershipOrAdmin(
                itinerary.getCreator() != null ? itinerary.getCreator().getId() : null, "itinerary");

        if (itinerary.getImagePath() != null) {
            fileStorageService.delete(itinerary.getImagePath());
            itinerary.setImagePath(null);
            itineraryRepository.save(itinerary);
        }
        return itinerary;
    }

    public Itinerary updateVisibility(String stringId, String visibility) {
        Itinerary itinerary = getItinerary(stringId);
        authorizationService.verifyOwnershipOrAdmin(
                itinerary.getCreator() != null ? itinerary.getCreator().getId() : null, "itinerary");
        itinerary.setVisibility(visibility);
        if (visibility != null && "SHARED".equalsIgnoreCase(visibility.trim())) {
            if (itinerary.getShareCode() == null || itinerary.getShareCode().isBlank()) {
                itinerary.setShareCode(generateUniqueShareCode());
            }
        }
        Itinerary saved = itineraryRepository.save(itinerary);
        auditLogService.log("UPDATE_ITINERARY_VISIBILITY", "Itinerary", stringId,
                "Updated visibility to: " + visibility);
        return saved;
    }

    @Transactional
    public PaymentIntentResponseDto bookItinerary(String itineraryId, String userEmail) {
        Itinerary itinerary = itineraryRepository.findByIdForUpdate(UUID.fromString(itineraryId))
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "itinerary.notFound"));

        if (itinerary.getEndDateTime() != null && itinerary.getEndDateTime().isBefore(LocalDateTime.now())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "itinerary.booking.pastEvent");
        }

        User user = userRepository.getUserByEmail(userEmail)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "user.notFound"));

        Optional<ItineraryBooking> existingBookingOpt = itineraryBookingRepository
                .findByUserIdAndItineraryId(user.getId(), itinerary.getId());
        if (existingBookingOpt.isPresent()) {
            ItineraryBooking existingBooking = existingBookingOpt.get();
            if (existingBooking.getStatus() == BookingStatus.CONFIRMED) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "itinerary.booking.alreadyBooked");
            }
        }

        // Calcola il prezzo totale sommando le attività dell'itinerario
        BigDecimal totalPrice = BigDecimal.ZERO;
        for (Activity activity : itinerary.getActivities()) {
            if (activity.getPrice() != null) {
                totalPrice = totalPrice.add(activity.getPrice());
            }
        }

        String clientSecret = null;
        String paymentIntentId = null;
        BookingStatus status = BookingStatus.PENDING;

        if (totalPrice.compareTo(BigDecimal.ZERO) > 0 && !paymentMock) {
            clientSecret = paymentGateway.createPaymentIntent(totalPrice, "eur",
                    "Prenotazione Itinerario: " + itinerary.getTitle());
            if (clientSecret != null) {
                if (clientSecret.contains("_secret_")) {
                    paymentIntentId = clientSecret.substring(0, clientSecret.indexOf("_secret_"));
                } else {
                    paymentIntentId = clientSecret;
                }
            }
        } else {
            status = BookingStatus.CONFIRMED;
        }

        ItineraryBooking booking = existingBookingOpt.orElseGet(ItineraryBooking::new);
        booking.setUser(user);
        booking.setItinerary(itinerary);
        booking.setBookedAt(LocalDateTime.now());
        booking.setPaymentIntentId(paymentIntentId);
        booking.setStatus(status);
        ItineraryBooking savedBooking = itineraryBookingRepository.save(booking);

        // Prenota contestualmente le singole attività dell'itinerario
        for (Activity activity : itinerary.getActivities()) {
            Activity lockedActivity = activityRepository.findByIdForUpdate(activity.getId())
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "activity.notFound"));

            int currentParticipants = activityService.calculateCurrentParticipants(lockedActivity);
            if (lockedActivity.getParticipants() != null && currentParticipants >= lockedActivity.getParticipants()) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "activity.booking.full");
            }

            Optional<ActivityBooking> existingActBookingOpt = activityBookingRepository
                    .findByUserIdAndActivityId(user.getId(), lockedActivity.getId());
            ActivityBooking actBooking = existingActBookingOpt.orElseGet(ActivityBooking::new);
            actBooking.setUser(user);
            actBooking.setActivity(lockedActivity);
            actBooking.setItinerary(itinerary);
            actBooking.setBookedAt(LocalDateTime.now());
            actBooking.setPaymentIntentId(paymentIntentId);
            actBooking.setStatus(status);
            activityBookingRepository.save(actBooking);
        }

        if (status == BookingStatus.CONFIRMED) {
            sendItineraryBookingConfirmationNotifications(savedBooking);
        }

        auditLogService.log("BOOK_ITINERARY", "ItineraryBooking", savedBooking.getId().toString(),
                "Booking for user: " + userEmail + " status: " + status);

        return new PaymentIntentResponseDto(clientSecret, savedBooking.getId().toString());
    }

    @Transactional
    public void confirmItineraryBooking(String bookingId) {
        ItineraryBooking booking = itineraryBookingRepository.findById(UUID.fromString(bookingId))
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "itinerary.booking.notFound"));
        if (booking.getStatus() == BookingStatus.CONFIRMED) {
            return;
        }
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "itinerary.booking.alreadyCancelled");
        }
        booking.setStatus(BookingStatus.CONFIRMED);
        itineraryBookingRepository.save(booking);

        List<ActivityBooking> activityBookings = null;
        if (booking.getPaymentIntentId() != null) {
            activityBookings = activityBookingRepository.findByPaymentIntentId(booking.getPaymentIntentId());
        }
        if (activityBookings == null || activityBookings.isEmpty()) {
            activityBookings = activityBookingRepository.findByUserIdAndItineraryId(booking.getUser().getId(),
                    booking.getItinerary().getId());
        }
        for (ActivityBooking ab : activityBookings) {
            ab.setStatus(BookingStatus.CONFIRMED);
            activityBookingRepository.save(ab);
        }

        auditLogService.log("BOOK_ITINERARY_CONFIRMED", "ItineraryBooking", bookingId,
                "Confirmed booking for itinerary: " + booking.getItinerary().getId());

        sendItineraryBookingConfirmationNotifications(booking);
    }

    @Transactional
    public void cancelItineraryBooking(String itineraryId, String userEmail) {
        User user = userRepository.getUserByEmail(userEmail)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "user.notFound"));

        ItineraryBooking booking = itineraryBookingRepository
                .findByUserIdAndItineraryId(user.getId(), UUID.fromString(itineraryId))
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "itinerary.booking.notFound"));

        if (booking.getItinerary().getStartDateTime() != null && booking.getItinerary().getStartDateTime().isBefore(LocalDateTime.now())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "itinerary.booking.pastEvent");
        }

        itineraryBookingRepository.delete(booking);

        List<ActivityBooking> activityBookings = activityBookingRepository.findByUserIdAndItineraryId(user.getId(),
                UUID.fromString(itineraryId));
        activityBookingRepository.deleteAll(activityBookings);

        auditLogService.log("CANCEL_ITINERARY_BOOKING", "ItineraryBooking", booking.getId().toString(),
                "Cancelled booking for user: " + userEmail);
    }

    private void linkAndValidateActivities(Itinerary itinerary, List<String> activityStringIds) {
        if (activityStringIds == null || activityStringIds.isEmpty()) {
            itinerary.setActivities(null);
            return;
        }

        List<UUID> activityUuids = activityStringIds.stream()
                .map(UUID::fromString)
                .toList();
        List<Activity> activities = activityRepository.findAllById(activityUuids);

        // Ordinamento cronologico
        activities.sort(Comparator.comparing(Activity::getStartTime));

        // Validazione anti-sovrapposizione
        for (int i = 0; i < activities.size() - 1; i++) {
            Activity current = activities.get(i);
            Activity next = activities.get(i + 1);
            if (current.getEndTime().isAfter(next.getStartTime())) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "itinerary.activities.overlap");
            }
        }

        // Verifica che le attività ricadano all'interno dell'intervallo dell'itinerario (se impostato)
        if (itinerary.getStartDateTime() != null) {
            for (Activity act : activities) {
                if (act.getStartTime().isBefore(itinerary.getStartDateTime())) {
                    throw new ApiException(HttpStatus.BAD_REQUEST, "itinerary.activities.beforeStart");
                }
            }
        }
        if (itinerary.getEndDateTime() != null) {
            for (Activity act : activities) {
                if (act.getEndTime().isAfter(itinerary.getEndDateTime())) {
                    throw new ApiException(HttpStatus.BAD_REQUEST, "itinerary.activities.afterEnd");
                }
            }
        }

        itinerary.setActivities(activities);

        // Se l'itinerario non aveva date esplicite stabilite alla creazione, le calcoliamo dalle attività
        if (itinerary.getStartDateTime() == null && !activities.isEmpty()) {
            itinerary.setStartDateTime(activities.get(0).getStartTime());
        }
        if (itinerary.getEndDateTime() == null && !activities.isEmpty()) {
            itinerary.setEndDateTime(activities.get(activities.size() - 1).getEndTime());
        }
    }

    private void sendItineraryBookingConfirmationNotifications(ItineraryBooking booking) {
        try {
            notificationService.createNotification(
                    booking.getUser(),
                    "Itinerario Prenotato",
                    "Il tuo itinerario '" + booking.getItinerary().getTitle() + "' è stato prenotato con successo!",
                    NotificationType.PRENOTAZIONE_SUCCESSO);

            List<ActivityBooking> activityBookings = activityBookingRepository
                    .findByUserIdAndItineraryId(booking.getUser().getId(), booking.getItinerary().getId());
            for (ActivityBooking ab : activityBookings) {
                User organizer = ab.getActivity().getTemplate().getOrganizer();
                if (organizer != null) {
                    notificationService.createNotification(
                            organizer,
                            "Nuova Prenotazione Attività",
                            "Un utente ha prenotato la tua attività '" + ab.getActivity().getTemplate().getName()
                                    + "' all'interno di un itinerario.",
                            NotificationType.NUOVA_PRENOTAZIONE);
                }
            }
        } catch (Exception e) {
            auditLogService.log("NOTIFICATION_ERROR", "ItineraryBooking", booking.getId().toString(),
                    "Errore nell'invio delle notifiche per itinerario: " + e.getMessage());
        }
    }

    public boolean isItineraryBooked(String itineraryId, String userEmail) {
        User user = userRepository.getUserByEmail(userEmail).orElse(null);
        if (user == null)
            return false;

        Optional<ItineraryBooking> existingBookingOpt = itineraryBookingRepository
                .findByUserIdAndItineraryId(user.getId(), UUID.fromString(itineraryId));
        return existingBookingOpt.isPresent() && existingBookingOpt.get().getStatus() == BookingStatus.CONFIRMED;
    }

    @Transactional(readOnly = true)
    public List<Itinerary> getBookedItinerariesForUser(String userEmail) {
        User user = userRepository.getUserByEmail(userEmail)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "user.notFound"));
        List<ItineraryBooking> bookings = itineraryBookingRepository.findByUserId(user.getId());
        return bookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.CONFIRMED)
                .map(ItineraryBooking::getItinerary)
                .toList();
    }

    @Transactional
    public ItineraryJoinRequest requestJoinByCode(String shareCode, String userEmail) {
        if (userEmail == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "User not authenticated");
        }
        if (shareCode == null || shareCode.trim().isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "itinerary.codeRequired");
        }

        String code = shareCode.trim().toUpperCase();
        Itinerary itinerary = itineraryRepository.findByShareCode(code)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "itinerary.codeNotFound"));

        if (itinerary.getVisibility() == null || !"SHARED".equalsIgnoreCase(itinerary.getVisibility().trim())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "itinerary.notShared");
        }

        User requester = userRepository.getUserByEmail(userEmail)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "user.notFound"));

        if (itinerary.getCreator() != null && itinerary.getCreator().getId().equals(requester.getId())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "itinerary.join.isCreator");
        }

        Optional<ItineraryJoinRequest> existingOpt = itineraryJoinRequestRepository
                .findByUserIdAndItineraryId(requester.getId(), itinerary.getId());

        ItineraryJoinRequest joinRequest;
        if (existingOpt.isPresent()) {
            joinRequest = existingOpt.get();
            if (joinRequest.getStatus() == JoinRequestStatus.ACCEPTED) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "itinerary.join.alreadyMember");
            }
            if (joinRequest.getStatus() == JoinRequestStatus.PENDING) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "itinerary.join.alreadyPending");
            }
            joinRequest.setStatus(JoinRequestStatus.PENDING);
        } else {
            joinRequest = new ItineraryJoinRequest();
            joinRequest.setItinerary(itinerary);
            joinRequest.setUser(requester);
            joinRequest.setStatus(JoinRequestStatus.PENDING);
        }

        ItineraryJoinRequest saved = itineraryJoinRequestRepository.save(joinRequest);

        // Notifica l'organizzatore dell'itinerario
        try {
            if (itinerary.getCreator() != null) {
                String requesterName = requester.getFirstName() != null
                        ? (requester.getFirstName() + " " + (requester.getLastName() != null ? requester.getLastName() : "")).trim()
                        : requester.getEmail();
                notificationService.createNotification(
                        itinerary.getCreator(),
                        "Richiesta di Partecipazione",
                        requesterName + " ha richiesto di unirsi al tuo itinerario '" + itinerary.getTitle() + "'.",
                        NotificationType.RICHIESTA_PARTECIPAZIONE_ITINERARIO);
            }
        } catch (Exception e) {
            auditLogService.log("NOTIFICATION_ERROR", "ItineraryJoinRequest", saved.getId().toString(),
                    "Errore invio notifica richiesta partecipazione: " + e.getMessage());
        }

        auditLogService.log("REQUEST_JOIN_ITINERARY", "ItineraryJoinRequest", saved.getId().toString(),
                "User " + userEmail + " requested to join itinerary " + itinerary.getId());

        return saved;
    }

    @Transactional(readOnly = true)
    public List<ItineraryJoinRequest> getJoinRequestsForItinerary(String itineraryId, String userEmail, boolean isAdmin) {
        Itinerary itinerary = getItinerary(itineraryId);
        boolean isOwner = userEmail != null && itinerary.getCreator() != null
                && userEmail.equalsIgnoreCase(itinerary.getCreator().getEmail());
        if (!isAdmin && !isOwner) {
            throw new ApiException(HttpStatus.FORBIDDEN, "error.forbidden");
        }
        return itineraryJoinRequestRepository.findByItineraryIdOrderByCreatedAtDesc(itinerary.getId());
    }

    @Transactional
    public ItineraryJoinRequest acceptJoinRequest(String requestId, String userEmail, boolean isAdmin) {
        ItineraryJoinRequest joinRequest = itineraryJoinRequestRepository.findById(UUID.fromString(requestId))
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "itinerary.requestNotFound"));

        Itinerary itinerary = joinRequest.getItinerary();
        boolean isOwner = userEmail != null && itinerary.getCreator() != null
                && userEmail.equalsIgnoreCase(itinerary.getCreator().getEmail());
        if (!isAdmin && !isOwner) {
            throw new ApiException(HttpStatus.FORBIDDEN, "error.forbidden");
        }

        joinRequest.setStatus(JoinRequestStatus.ACCEPTED);
        ItineraryJoinRequest saved = itineraryJoinRequestRepository.save(joinRequest);

        try {
            notificationService.createNotification(
                    joinRequest.getUser(),
                    "Richiesta Accettata!",
                    "La tua richiesta per l'itinerario '" + itinerary.getTitle() + "' è stata accettata dall'organizzatore.",
                    NotificationType.ACCETTAZIONE_PARTECIPAZIONE_ITINERARIO);
        } catch (Exception e) {
            auditLogService.log("NOTIFICATION_ERROR", "ItineraryJoinRequest", saved.getId().toString(),
                    "Errore invio notifica accettazione: " + e.getMessage());
        }

        auditLogService.log("ACCEPT_JOIN_REQUEST", "ItineraryJoinRequest", saved.getId().toString(),
                "Accepted join request for user " + joinRequest.getUser().getEmail() + " on itinerary " + itinerary.getId());

        return saved;
    }

    @Transactional
    public ItineraryJoinRequest rejectJoinRequest(String requestId, String userEmail, boolean isAdmin) {
        ItineraryJoinRequest joinRequest = itineraryJoinRequestRepository.findById(UUID.fromString(requestId))
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "itinerary.requestNotFound"));

        Itinerary itinerary = joinRequest.getItinerary();
        boolean isOwner = userEmail != null && itinerary.getCreator() != null
                && userEmail.equalsIgnoreCase(itinerary.getCreator().getEmail());
        if (!isAdmin && !isOwner) {
            throw new ApiException(HttpStatus.FORBIDDEN, "error.forbidden");
        }

        joinRequest.setStatus(JoinRequestStatus.REJECTED);
        ItineraryJoinRequest saved = itineraryJoinRequestRepository.save(joinRequest);

        try {
            notificationService.createNotification(
                    joinRequest.getUser(),
                    "Richiesta Rifiutata",
                    "La tua richiesta per l'itinerario '" + itinerary.getTitle() + "' non è stata accettata dall'organizzatore.",
                    NotificationType.RIFIUTO_PARTECIPAZIONE_ITINERARIO);
        } catch (Exception e) {
            auditLogService.log("NOTIFICATION_ERROR", "ItineraryJoinRequest", saved.getId().toString(),
                    "Errore invio notifica rifiuto: " + e.getMessage());
        }

        auditLogService.log("REJECT_JOIN_REQUEST", "ItineraryJoinRequest", saved.getId().toString(),
                "Rejected join request for user " + joinRequest.getUser().getEmail() + " on itinerary " + itinerary.getId());

        return saved;
    }

    @Transactional(readOnly = true)
    public List<Itinerary> getJoinedItinerariesForUser(String userEmail) {
        User user = userRepository.getUserByEmail(userEmail)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "user.notFound"));
        List<ItineraryJoinRequest> accepted = itineraryJoinRequestRepository
                .findByUserIdAndStatusWithItinerary(user.getId(), JoinRequestStatus.ACCEPTED);
        return accepted.stream().map(ItineraryJoinRequest::getItinerary).toList();
    }

    @Transactional(readOnly = true)
    public List<ItineraryParticipantDto> getParticipants(String itineraryId, String userEmail, boolean isAdmin) {
        Itinerary itinerary = getItinerary(itineraryId, userEmail, isAdmin);
        List<ItineraryParticipantDto> result = new ArrayList<>();

        if (itinerary.getCreator() != null) {
            ItineraryParticipantDto creatorDto = new ItineraryParticipantDto();
            creatorDto.setUserId(itinerary.getCreator().getId());
            creatorDto.setUserName(getUserDisplayName(itinerary.getCreator()));
            creatorDto.setUserEmail(itinerary.getCreator().getEmail());
            creatorDto.setUserAvatarUrl(itinerary.getCreator().getAvatarUrl());
            creatorDto.setCreator(true);
            creatorDto.setJoinedAt(itinerary.getCreatedAt());
            result.add(creatorDto);
        }

        List<ItineraryJoinRequest> acceptedList = itineraryJoinRequestRepository
                .findByItineraryIdAndStatusWithUser(itinerary.getId(), JoinRequestStatus.ACCEPTED);
        for (ItineraryJoinRequest req : acceptedList) {
            ItineraryParticipantDto dto = new ItineraryParticipantDto();
            dto.setUserId(req.getUser().getId());
            dto.setUserName(getUserDisplayName(req.getUser()));
            dto.setUserEmail(req.getUser().getEmail());
            dto.setUserAvatarUrl(req.getUser().getAvatarUrl());
            dto.setCreator(false);
            dto.setJoinedAt(req.getCreatedAt());
            result.add(dto);
        }
        return result;
    }

    public boolean isUserAcceptedParticipant(UUID itineraryId, UUID userId) {
        return itineraryJoinRequestRepository.existsByUserIdAndItineraryIdAndStatus(userId, itineraryId, JoinRequestStatus.ACCEPTED);
    }

    public int getPendingRequestsCount(UUID itineraryId) {
        return (int) itineraryJoinRequestRepository.countByItineraryIdAndStatus(itineraryId, JoinRequestStatus.PENDING);
    }

    private String getUserDisplayName(User user) {
        if (user == null) return "Anonimo";
        if (user.getUserType() == it.unical.ea.enums.UserType.SOCIETA && user.getCompanyName() != null) {
            return user.getCompanyName();
        }
        String name = ((user.getFirstName() != null ? user.getFirstName() : "") + " " + (user.getLastName() != null ? user.getLastName() : "")).trim();
        return name.isEmpty() ? user.getEmail() : name;
    }
}
