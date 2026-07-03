package it.unical.ea.Travel.Services.activity;

import it.unical.ea.dtos.activity.ActivityDto;
import it.unical.ea.Travel.Entities.activity.Activity;
import it.unical.ea.Travel.Entities.activity.ActivityBooking;
import it.unical.ea.Travel.Entities.user.User;
import it.unical.ea.Travel.Exception.ApiException;
import it.unical.ea.Travel.Mappers.activity.ActivityMapper;
import it.unical.ea.Travel.Repositories.activity.ActivityBookingRepository;
import it.unical.ea.Travel.Repositories.activity.ActivityRepository;
import it.unical.ea.Travel.Repositories.user.UserRepository;
import it.unical.ea.Travel.Services.storage.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ActivityService {

    private static final String ACTIVITIES_SUBDIR = "activities";

    private final ActivityRepository activityRepository;
    private final ActivityBookingRepository activityBookingRepository;
    private final UserRepository userRepository;
    private final ActivityMapper activityMapper;
    private final FileStorageService fileStorageService;

    public List<ActivityDto> getAllActivities() {
        List<Activity> activities = activityRepository.findByApproved(true);
        List<ActivityDto> dtos = activityMapper.toDTOList(activities);

        // Arricchisci ogni DTO con il conteggio dinamico dei partecipanti
        for (int i = 0; i < activities.size(); i++) {
            dtos.get(i).setCurrentParticipants(calculateCurrentParticipants(activities.get(i)));
        }

        return dtos; 
    }

    public ActivityDto createActivity(ActivityDto activityDto) {
        Activity activity = activityMapper.toEntity(activityDto);
        activity.setApproved(false);
        Activity savedActivity = activityRepository.save(activity);
        return activityMapper.toDTO(savedActivity);
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
    public void bookActivity(String activityId, String userEmail) {
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

        if (activityBookingRepository.findByUserIdAndActivityId(user.getId(), activity.getId()).isPresent()) {
            throw new ApiException(HttpStatus.CONFLICT, "activity.booking.alreadyBooked");
        }

        ActivityBooking booking = new ActivityBooking();
        booking.setUser(user);
        booking.setActivity(activity);
        activityBookingRepository.save(booking);
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
    }

    // Carica una o più immagini per l'attività specificata
    @Transactional
    public ActivityDto uploadImages(String activityId, MultipartFile[] files) {
        UUID uuid = UUID.fromString(activityId);
        Activity activity = activityRepository.findById(uuid)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "activity.notFound"));

        if (activity.getImages() == null) {
            activity.setImages(new java.util.ArrayList<>());
        }

        for (MultipartFile file : files) {
            String relativePath = fileStorageService.store(file, ACTIVITIES_SUBDIR);
            activity.getImages().add(relativePath);
        }

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
        if (activity.getImages() != null && activity.getImages().contains(targetPath)) {
            fileStorageService.delete(targetPath);
            activity.getImages().remove(targetPath);
            Activity saved = activityRepository.save(activity);
            return activityMapper.toDTO(saved);
        } else {
            throw new ApiException(HttpStatus.NOT_FOUND, "activity.imageNotFound");
        }
    }
}