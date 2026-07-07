package it.unical.ea.Travel.Services.activity;

import it.unical.ea.dtos.activity.ActivityDto;
import it.unical.ea.Travel.Entities.activity.Activity;
import it.unical.ea.Travel.Entities.activity.ActivityBooking;
import it.unical.ea.Travel.Entities.user.User;
import it.unical.ea.Travel.Exception.ApiException;
import it.unical.ea.Travel.Mappers.activity.ActivityMapper;
import it.unical.ea.Travel.Repositories.activity.ActivityBookingRepository;
import it.unical.ea.Travel.Repositories.activity.ActivityRepository;
import it.unical.ea.Travel.Repositories.activity.ActivityTemplateRepository;
import it.unical.ea.Travel.Entities.activity.ActivityTemplate;
import it.unical.ea.Travel.Repositories.user.UserRepository;
import it.unical.ea.Travel.Services.storage.FileStorageService;
import it.unical.ea.Travel.Services.audit.AuditLogService;
import it.unical.ea.dtos.user.UserDTO;
import it.unical.ea.Travel.Mappers.user.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import it.unical.ea.Travel.Config.SecurityUtils;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;
import java.util.UUID;
import java.math.BigDecimal;
import it.unical.ea.Travel.Services.payment.PaymentGateway;
import it.unical.ea.dtos.payment.PaymentIntentResponseDto;
import it.unical.ea.Travel.Entities.payment.BookingStatus;

@Service
@RequiredArgsConstructor
public class ActivityService {

    private static final String ACTIVITIES_SUBDIR = "activities";

    private final ActivityRepository activityRepository;
    private final ActivityTemplateRepository activityTemplateRepository;
    private final ActivityBookingRepository activityBookingRepository;
    private final UserRepository userRepository;
    private final ActivityMapper activityMapper;
    private final FileStorageService fileStorageService;
    private final AuditLogService auditLogService;
    private final UserMapper userMapper;
    private final it.unical.ea.Travel.Services.location.LocationService locationService;
    private final PaymentGateway paymentGateway;

    @Value("${payment.mock:true}")
    private boolean paymentMock;

    public List<ActivityDto> getAllActivities() {
        List<Activity> activities = activityRepository.findAll();
        List<ActivityDto> dtos = activityMapper.toDTOList(activities);

        // Arricchisci ogni DTO con il conteggio dinamico dei partecipanti
        for (int i = 0; i < activities.size(); i++) {
            dtos.get(i).setCurrentParticipants(calculateCurrentParticipants(activities.get(i)));
        }

        return dtos; 
    }

    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<ActivityDto> searchActivities(String keyword, Double minPrice, Double maxPrice, java.time.LocalDateTime minStartTime, int page, int size) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        String safeKeyword = (keyword == null) ? "" : keyword.trim();
        org.springframework.data.domain.Page<Activity> activities = activityRepository.searchByKeyword(safeKeyword, minPrice, maxPrice, minStartTime, pageable);
        
        return activities.map(activity -> {
            ActivityDto dto = activityMapper.toDTO(activity);
            dto.setCurrentParticipants(calculateCurrentParticipants(activity));
            return dto;
        });
    }

    @Transactional
    public ActivityDto createActivity(ActivityDto activityDto) {
        Activity activity = activityMapper.toEntity(activityDto);
        ActivityTemplate template;

        if (activityDto.getTemplateId() != null) {
            template = activityTemplateRepository.findById(activityDto.getTemplateId())
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "activity.templateNotFound"));
        } else {
            template = activityMapper.toTemplateEntity(activityDto);
            template.setApproved(false);
            it.unical.ea.Travel.Entities.location.Location locationEntity = locationService.getOrCreateLocation(activityDto.getLocation());
            template.setLocationEntity(locationEntity);
            String email = SecurityUtils.getCurrentUserEmail();
            User organizer = userRepository.getUserByEmail(email)
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "user.notFound"));
            template.setOrganizer(organizer);
            template = activityTemplateRepository.save(template);
        }

        activity.setTemplate(template);
        Activity savedActivity = activityRepository.save(activity);
        auditLogService.log("CREATE_ACTIVITY", "Activity", savedActivity.getId().toString(), "Created activity schedule for template: " + template.getName());
        return activityMapper.toDTO(savedActivity);
    }

    @Transactional
    public ActivityDto updateActivity(String stringId, ActivityDto activityDto) {
        UUID uuid = UUID.fromString(stringId);
        Activity activity = activityRepository.findById(uuid)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "activity.notFound"));

        // Update schedule fields
        activity.setStartTime(activityDto.getStartTime());
        activity.setEndTime(activityDto.getEndTime());
        activity.setParticipants(activityDto.getParticipants());
        activity.setPrice(activityDto.getPrice());

        // Update template fields
        ActivityTemplate template = activity.getTemplate();
        template.setName(activityDto.getName());
        template.setDescription(activityDto.getDescription());
        template.setLocation(activityDto.getLocation());
        
        it.unical.ea.Travel.Entities.location.Location locationEntity = locationService.getOrCreateLocation(activityDto.getLocation());
        template.setLocationEntity(locationEntity);
        activityTemplateRepository.save(template);

        Activity saved = activityRepository.save(activity);
        auditLogService.log("UPDATE_ACTIVITY", "Activity", saved.getId().toString(), "Updated activity: " + template.getName());
        return activityMapper.toDTO(saved);
    }

    
    public ActivityDto getActivity(String stringId) {
        UUID uuid = UUID.fromString(stringId);
        
        Activity activity = activityRepository.findById(uuid)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "activity.notFound"));

        ActivityDto dto = activityMapper.toDTO(activity);
        dto.setCurrentParticipants(calculateCurrentParticipants(activity));

        return dto;
    }

    
    public void deleteActivity(String stringId) {
        UUID uuid = UUID.fromString(stringId);
        activityRepository.deleteById(uuid);
        auditLogService.log("DELETE_ACTIVITY", "Activity", stringId, "Deleted activity with ID: " + stringId);
    }

    // --- Logica Prenotazione ---

    /**
     * Calcola il numero attuale di partecipanti per un'attività.
     * Include le prenotazioni dirette e (in futuro) quelle tramite itinerari.
     */
    public int calculateCurrentParticipants(Activity activity) {
        // 1. Conteggio diretto
        long direct = activityBookingRepository.countDirectParticipants(activity.getId());

        // 2. Conteggio tramite itinerari (sarà implementato integrando ItineraryBookingRepository)
        long fromItineraries = countParticipantsFromItineraries(activity.getId());

        return (int) (direct + fromItineraries);
    }

    /**
     * Placeholder per il conteggio dei partecipanti provenienti da prenotazioni di itinerari.
     * Da implementare quando verrà creata l'entità ItineraryBooking.
     */
    private long countParticipantsFromItineraries(UUID activityId) {
        // TODO: Implementare con ItineraryBookingRepository quando disponibile
        return 0;
    }

    @Transactional
    public PaymentIntentResponseDto bookActivity(String activityId, String userEmail) {
        Activity activity = activityRepository.findByIdForUpdate(UUID.fromString(activityId))
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "activity.notFound"));

        // REGOLA DATI PASSATI: Blocco prenotazione per attività passate
        if (activity.getEndTime() != null && activity.getEndTime().isBefore(LocalDateTime.now())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "activity.booking.pastEvent");
        }

        User user = userRepository.getUserByEmail(userEmail)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "user.notFound"));

        // Verifica capienza massima
        int current = calculateCurrentParticipants(activity);
        if (activity.getParticipants() != null && current >= activity.getParticipants()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "activity.booking.full");
        }

        Optional<ActivityBooking> existingBookingOpt = activityBookingRepository.findByUserIdAndActivityId(user.getId(), activity.getId());
        if (existingBookingOpt.isPresent()) {
            ActivityBooking existingBooking = existingBookingOpt.get();
            if (existingBooking.getStatus() == BookingStatus.PENDING || existingBooking.getStatus() == BookingStatus.FAILED) {
                cancelActivityBooking(activity.getId().toString(), userEmail);
            } else {
                throw new ApiException(HttpStatus.CONFLICT, "activity.booking.alreadyBooked");
            }
        }

        String clientSecret = null;
        String paymentIntentId = null;
        BookingStatus status = BookingStatus.PENDING;

        if (activity.getPrice() != null && activity.getPrice().compareTo(BigDecimal.ZERO) > 0 && !paymentMock) {
            clientSecret = paymentGateway.createPaymentIntent(activity.getPrice(), "eur", "Booking for Activity: " + activity.getTemplate().getName());
            if (clientSecret != null && clientSecret.contains("_secret_")) {
                paymentIntentId = clientSecret.substring(0, clientSecret.indexOf("_secret_"));
            }
        } else {
            status = BookingStatus.CONFIRMED;
        }

        ActivityBooking booking = new ActivityBooking();
        booking.setUser(user);
        booking.setActivity(activity);
        booking.setStatus(status);
        booking.setPaymentIntentId(paymentIntentId);
        ActivityBooking savedBooking = activityBookingRepository.save(booking);
        auditLogService.log("BOOK_ACTIVITY", "ActivityBooking", savedBooking.getId().toString(), "User " + userEmail + " booked activity " + activity.getTemplate().getName() + " status: " + status);
        
        return new PaymentIntentResponseDto(clientSecret, savedBooking.getId().toString());
    }

    @Transactional
    public void cancelActivityBooking(String activityId, String userEmail) {
        Activity activity = activityRepository.findByIdForUpdate(UUID.fromString(activityId))
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "activity.notFound"));

        // REGOLA DATI PASSATI: Blocco cancellazione per attività passate
        if (activity.getEndTime() != null && activity.getEndTime().isBefore(LocalDateTime.now())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "activity.booking.pastEvent");
        }

        User user = userRepository.getUserByEmail(userEmail)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "user.notFound"));

        ActivityBooking booking = activityBookingRepository.findByUserIdAndActivityId(user.getId(), activity.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "activity.booking.notFound"));

        activityBookingRepository.delete(booking);
        activityBookingRepository.flush();
        auditLogService.log("CANCEL_BOOKING", "ActivityBooking", booking.getId().toString(), "User " + userEmail + " cancelled booking for activity: " + activity.getTemplate().getName());
    }

    // Carica una o più immagini per l'attività specificata
    @Transactional
    public ActivityDto uploadImages(String activityId, MultipartFile[] files) {
        if (files == null || files.length == 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "file.empty");
        }
        if (files.length > 5) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "activity.images.maxCountExceeded");
        }

        UUID uuid = UUID.fromString(activityId);
        Activity activity = activityRepository.findById(uuid)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "activity.notFound"));

        if (activity.getTemplate().getImages() == null) {
            activity.getTemplate().setImages(new java.util.ArrayList<>());
        }

        for (MultipartFile file : files) {
            String relativePath = fileStorageService.store(file, ACTIVITIES_SUBDIR);
            activity.getTemplate().getImages().add(relativePath);
        }

        activityTemplateRepository.save(activity.getTemplate());
        Activity saved = activityRepository.save(activity);
        return activityMapper.toDTO(saved);
    }

    // Carica l'immagine dell'attività come risorsa
    public Resource loadImage(String filename) {
        return fileStorageService.load(ACTIVITIES_SUBDIR + "/" + filename);
    }

    // Elimina una specifica immagine associata all'attività
    @Transactional
    public ActivityDto deleteImage(String activityId, String filename) {
        UUID uuid = UUID.fromString(activityId);
        Activity activity = activityRepository.findById(uuid)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "activity.notFound"));

        String targetPath = ACTIVITIES_SUBDIR + "/" + filename;
        if (activity.getTemplate().getImages() != null && activity.getTemplate().getImages().contains(targetPath)) {
            fileStorageService.delete(targetPath);
            activity.getTemplate().getImages().remove(targetPath);
            activityTemplateRepository.save(activity.getTemplate());
            Activity saved = activityRepository.save(activity);
            return activityMapper.toDTO(saved);
        } else {
            throw new ApiException(HttpStatus.NOT_FOUND, "activity.imageNotFound");
        }
    }

    public List<UserDTO> getBookedUsers(String activityId) {
        UUID uuid = UUID.fromString(activityId);
        List<ActivityBooking> bookings = activityBookingRepository.findByActivityId(uuid);
        return bookings.stream()
                .map(ActivityBooking::getUser)
                .map(userMapper::toDTO)
                .toList();
    }
}