package it.unical.ea.Travel.Services.itinerary;

import it.unical.ea.Travel.Entities.activity.Activity;
import it.unical.ea.Travel.Entities.itinerary.Itinerary;
import it.unical.ea.Travel.Entities.user.User;
import it.unical.ea.Travel.Exception.ApiException;
import it.unical.ea.Travel.Repositories.activity.ActivityRepository;
import it.unical.ea.Travel.Repositories.itinerary.ItineraryRepository;
import it.unical.ea.Travel.Repositories.user.UserRepository;
import it.unical.ea.Travel.Services.storage.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Service
public class ItineraryService {

    private static final String ITINERARIES_SUBDIR = "itineraries";

    private final ItineraryRepository itineraryRepository;
    private final UserRepository userRepository;
    private final ActivityRepository activityRepository;
    private final FileStorageService fileStorageService;

    @Autowired
    public ItineraryService(ItineraryRepository itineraryRepository,
                            UserRepository userRepository,
                            ActivityRepository activityRepository,
                            FileStorageService fileStorageService) {
        this.itineraryRepository = itineraryRepository;
        this.userRepository = userRepository;
        this.activityRepository = activityRepository;
        this.fileStorageService = fileStorageService;
    }

    // Riceve tutti gli itinerari dal database
    public List<Itinerary> getAllItineraries() {
        return itineraryRepository.findAll();
    }

    // Cerca un singolo itinerario tramite il suo ID
    public Itinerary getItinerary(String stringId) {
        UUID uuid = UUID.fromString(stringId);
        return itineraryRepository.findById(uuid)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "itinerary.notFound"));
    }

    // Cerca gli itinerari creati da un utente specifico
    public List<Itinerary> getItinerariesByCreator(String creatorStringId) {
        UUID creatorId = UUID.fromString(creatorStringId);
        return itineraryRepository.findByCreatorId(creatorId);
    }

    // Risolve il creatore e le activity, poi salva l'itinerario
    public Itinerary createItinerary(Itinerary itinerary, String creatorStringId, List<String> activityStringIds) {
        // Risolve il creatore
        UUID creatorId = UUID.fromString(creatorStringId);
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "user.notFound"));
        itinerary.setCreator(creator);

        // Risolve le activity (se presenti)
        if (activityStringIds != null && !activityStringIds.isEmpty()) {
            List<UUID> activityUuids = activityStringIds.stream()
                    .map(UUID::fromString)
                    .toList();
            List<Activity> activities = activityRepository.findAllById(activityUuids);
            itinerary.setActivities(activities);
        }

        return itineraryRepository.save(itinerary);
    }

    // Rimuove un itinerario (soft delete tramite @SQLDelete)
    public void deleteItinerary(String stringId) {
        UUID uuid = UUID.fromString(stringId);
        itineraryRepository.deleteById(uuid);
    }

    // Gestione immagine

    //Carica un'immagine per l'itinerario specificato.
    //Se esiste già un'immagine, viene sostituita.
    public Itinerary uploadImage(String itineraryId, MultipartFile file) {
        Itinerary itinerary = getItinerary(itineraryId);

        // Se esiste già un'immagine, elimina quella vecchia
        if (itinerary.getImagePath() != null) {
            fileStorageService.delete(itinerary.getImagePath());
        }

        String relativePath = fileStorageService.store(file, ITINERARIES_SUBDIR);
        itinerary.setImagePath(relativePath);

        return itineraryRepository.save(itinerary);
    }

    
    //Restituisce l'immagine dell'itinerario come Resource.

    public Resource getImage(String itineraryId) {
        Itinerary itinerary = getItinerary(itineraryId);

        if (itinerary.getImagePath() == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "itinerary.imageNotFound");
        }

        return fileStorageService.load(itinerary.getImagePath());
    }

    
    //Elimina l'immagine associata all'itinerario.

    public Itinerary deleteImage(String itineraryId) {
        Itinerary itinerary = getItinerary(itineraryId);

        if (itinerary.getImagePath() != null) {
            fileStorageService.delete(itinerary.getImagePath());
            itinerary.setImagePath(null);
            itineraryRepository.save(itinerary);
        }

        return itinerary;
    }
}

