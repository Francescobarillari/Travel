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
import it.unical.ea.Travel.Services.notification.NotificationService;
import it.unical.ea.Travel.Services.payment.PaymentGateway;
import it.unical.ea.Travel.Services.storage.FileStorageService;
import it.unical.ea.dtos.itinerary.CreateItineraryRequest;
import it.unical.ea.dtos.payment.PaymentIntentResponseDto;
import it.unical.ea.enums.NotificationType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ItineraryService {

    private static final String ITINERARIES_SUBDIR = "itineraries";

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

    @Value("${payment.mock:true}")
    private boolean paymentMock;

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
                return itineraryRepository.findPublicOrCreatorItineraries(user.getId());
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
        if (itinerary.getVisibility() != null && "PRIVATE".equalsIgnoreCase(itinerary.getVisibility().trim())) {
            boolean isOwner = userEmail != null && itinerary.getCreator() != null
                    && userEmail.equalsIgnoreCase(itinerary.getCreator().getEmail());
            if (!isAdmin && !isOwner) {
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

        linkAndValidateActivities(itinerary, activityStringIds);

        Itinerary saved = itineraryRepository.save(itinerary);
        auditLogService.log("CREATE_ITINERARY", "Itinerary", saved.getId().toString(), "Created itinerary: " + saved.getTitle());
        return saved;
    }

    public Itinerary updateItinerary(String stringId, CreateItineraryRequest request) {
        Itinerary itinerary = getItinerary(stringId);
        itinerary.setTitle(request.getTitle());
        itinerary.setDescription(request.getDescription());
        itinerary.setVisibility(request.getVisibility() != null ? request.getVisibility() : "PRIVATE");

        if (request.getActivityIds() != null) {
            linkAndValidateActivities(itinerary, request.getActivityIds());
        }

        Itinerary saved = itineraryRepository.save(itinerary);
        auditLogService.log("UPDATE_ITINERARY", "Itinerary", saved.getId().toString(), "Updated itinerary: " + saved.getTitle());
        return saved;
    }

    public void deleteItinerary(String stringId) {
        itineraryRepository.deleteById(UUID.fromString(stringId));
        auditLogService.log("DELETE_ITINERARY", "Itinerary", stringId, "Deleted itinerary with ID: " + stringId);
    }

    public Itinerary uploadImage(String itineraryId, MultipartFile file) {
        Itinerary itinerary = getItinerary(itineraryId);
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
        if (itinerary.getImagePath() != null) {
            fileStorageService.delete(itinerary.getImagePath());
            itinerary.setImagePath(null);
            itineraryRepository.save(itinerary);
        }
        return itinerary;
    }

    public Itinerary updateVisibility(String stringId, String visibility) {
        Itinerary itinerary = getItinerary(stringId);
        itinerary.setVisibility(visibility);
        Itinerary saved = itineraryRepository.save(itinerary);
        auditLogService.log("UPDATE_ITINERARY_VISIBILITY", "Itinerary", stringId, "Updated visibility to: " + visibility);
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

        BigDecimal totalPrice = BigDecimal.ZERO;
        for (Activity activity : itinerary.getActivities()) {
            Activity lockedActivity = activityRepository.findByIdForUpdate(activity.getId())
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "activity.notFound"));

            int current = activityService.calculateCurrentParticipants(lockedActivity);
            if (lockedActivity.getParticipants() != null && current >= lockedActivity.getParticipants()) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "itinerary.booking.activityFull");
            }
            if (activity.getPrice() != null) {
                totalPrice = totalPrice.add(activity.getPrice());
            }
        }

        Optional<ItineraryBooking> existingBookingOpt = itineraryBookingRepository.findByUserIdAndItineraryId(user.getId(), itinerary.getId());
        if (existingBookingOpt.isPresent()) {
            ItineraryBooking existing = existingBookingOpt.get();
            if (existing.getStatus() == BookingStatus.PENDING || existing.getStatus() == BookingStatus.FAILED) {
                cancelItineraryBooking(itineraryId, userEmail);
            } else {
                throw new ApiException(HttpStatus.CONFLICT, "itinerary.booking.alreadyBooked");
            }
        }

        String clientSecret = null;
        String paymentIntentId = null;
        BookingStatus status = BookingStatus.PENDING;

        if (totalPrice.compareTo(BigDecimal.ZERO) > 0 && !paymentMock) {
            clientSecret = paymentGateway.createPaymentIntent(totalPrice, "eur", "Booking for Itinerary: " + itinerary.getTitle());
            paymentIntentId = clientSecret;
        } else {
            status = BookingStatus.CONFIRMED;
        }

        ItineraryBooking booking = new ItineraryBooking();
        booking.setUser(user);
        booking.setItinerary(itinerary);
        booking.setStatus(status);
        booking.setPaymentIntentId(paymentIntentId);
        itineraryBookingRepository.save(booking);

        for (Activity activity : itinerary.getActivities()) {
            if (activityBookingRepository.findByUserIdAndActivityId(user.getId(), activity.getId()).isPresent()) {
                continue;
            }
            ActivityBooking actBooking = new ActivityBooking();
            actBooking.setUser(user);
            actBooking.setActivity(activity);
            actBooking.setItinerary(itinerary);
            actBooking.setStatus(status);
            actBooking.setPaymentIntentId(paymentIntentId);
            activityBookingRepository.save(actBooking);
        }

        auditLogService.log("BOOK_ITINERARY", "ItineraryBooking", booking.getId().toString(),
                "User " + userEmail + " booked itinerary: " + itinerary.getTitle() + " status: " + status);

        if (status == BookingStatus.CONFIRMED) {
            sendItineraryBookingConfirmationNotifications(booking);
        }
        return new PaymentIntentResponseDto(clientSecret, booking.getId().toString());
    }

    @Transactional
    public void cancelItineraryBooking(String itineraryId, String userEmail) {
        Itinerary itinerary = itineraryRepository.findByIdForUpdate(UUID.fromString(itineraryId))
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "itinerary.notFound"));

        if (itinerary.getEndDateTime() != null && itinerary.getEndDateTime().isBefore(LocalDateTime.now())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "itinerary.booking.pastEvent");
        }

        User user = userRepository.getUserByEmail(userEmail)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "user.notFound"));

        ItineraryBooking booking = itineraryBookingRepository.findByUserIdAndItineraryId(user.getId(), itinerary.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "itinerary.booking.notFound"));

        List<ActivityBooking> associatedBookings = activityBookingRepository.findByUserIdAndItineraryId(user.getId(), itinerary.getId());
        activityBookingRepository.deleteAll(associatedBookings);
        activityBookingRepository.flush();

        itineraryBookingRepository.delete(booking);
        itineraryBookingRepository.flush();

        auditLogService.log("CANCEL_ITINERARY_BOOKING", "ItineraryBooking", booking.getId().toString(),
                "User " + userEmail + " cancelled booking for itinerary: " + itinerary.getTitle());
    }

    @Transactional
    public void confirmItineraryBooking(String bookingId) {
        ItineraryBooking booking = itineraryBookingRepository.findById(UUID.fromString(bookingId))
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "itinerary.booking.notFound"));

        if (booking.getStatus() == BookingStatus.CONFIRMED) {
            return;
        }
        booking.setStatus(BookingStatus.CONFIRMED);
        itineraryBookingRepository.save(booking);

        List<ActivityBooking> activityBookings = booking.getPaymentIntentId() != null
                ? activityBookingRepository.findByPaymentIntentId(booking.getPaymentIntentId())
                : null;
        if (activityBookings == null || activityBookings.isEmpty()) {
            activityBookings = activityBookingRepository.findByUserIdAndItineraryId(booking.getUser().getId(), booking.getItinerary().getId());
        }
        for (ActivityBooking ab : activityBookings) {
            ab.setStatus(BookingStatus.CONFIRMED);
            activityBookingRepository.save(ab);
        }
        sendItineraryBookingConfirmationNotifications(booking);
        auditLogService.log("CONFIRM_ITINERARY_BOOKING", "ItineraryBooking", booking.getId().toString(), "Booking confirmed client-side");
    }

    public boolean isItineraryBooked(String itineraryId, String userEmail) {
        User user = userRepository.getUserByEmail(userEmail).orElse(null);
        if (user == null) return false;

        Optional<ItineraryBooking> existing = itineraryBookingRepository.findByUserIdAndItineraryId(user.getId(), UUID.fromString(itineraryId));
        return existing.isPresent() && existing.get().getStatus() == BookingStatus.CONFIRMED;
    }

    @Transactional(readOnly = true)
    public List<Itinerary> getBookedItinerariesForUser(String userEmail) {
        User user = userRepository.getUserByEmail(userEmail)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "user.notFound"));
        return itineraryBookingRepository.findByUserId(user.getId()).stream()
                .filter(b -> b.getStatus() == BookingStatus.CONFIRMED)
                .map(ItineraryBooking::getItinerary)
                .toList();
    }

    private void linkAndValidateActivities(Itinerary itinerary, List<String> activityStringIds) {
        if (activityStringIds == null || activityStringIds.isEmpty()) {
            itinerary.setActivities(null);
            itinerary.setStartDateTime(null);
            itinerary.setEndDateTime(null);
            return;
        }

        List<UUID> activityUuids = activityStringIds.stream()
                .map(UUID::fromString)
                .toList();
        List<Activity> activities = activityRepository.findAllById(activityUuids);
        activities.sort(Comparator.comparing(Activity::getStartTime));

        for (int i = 0; i < activities.size() - 1; i++) {
            if (activities.get(i).getEndTime().isAfter(activities.get(i + 1).getStartTime())) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "itinerary.activities.overlap");
            }
        }

        itinerary.setActivities(activities);
        if (!activities.isEmpty()) {
            itinerary.setStartDateTime(activities.get(0).getStartTime());
            itinerary.setEndDateTime(activities.get(activities.size() - 1).getEndTime());
        }
    }

    private void sendItineraryBookingConfirmationNotifications(ItineraryBooking booking) {
        try {
            notificationService.createNotification(
                    booking.getUser(),
                    "Itinerario Prenotato",
                    "Il tuo itinerario '" + booking.getItinerary().getTitle() + "' è stato prenotato con successo!",
                    NotificationType.PRENOTAZIONE_SUCCESSO
            );

            List<ActivityBooking> activityBookings = activityBookingRepository.findByUserIdAndItineraryId(booking.getUser().getId(), booking.getItinerary().getId());
            for (ActivityBooking ab : activityBookings) {
                User organizer = ab.getActivity().getTemplate().getOrganizer();
                if (organizer != null) {
                    notificationService.createNotification(
                            organizer,
                            "Nuova Prenotazione Attività",
                            "Un utente ha prenotato la tua attività '" + ab.getActivity().getTemplate().getName() + "' all'interno di un itinerario.",
                            NotificationType.NUOVA_PRENOTAZIONE
                    );
                }
            }
        } catch (Exception e) {
            auditLogService.log("NOTIFICATION_ERROR", "ItineraryBooking", booking.getId().toString(),
                    "Errore nell'invio delle notifiche per itinerario: " + e.getMessage());
        }
    }
}
