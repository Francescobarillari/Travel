package it.unical.ea.Travel.Services.itinerary;

import it.unical.ea.Travel.Entities.activity.Activity;
import it.unical.ea.Travel.Entities.itinerary.Itinerary;
import it.unical.ea.Travel.Entities.user.User;
import it.unical.ea.Travel.Repositories.activity.ActivityRepository;
import it.unical.ea.Travel.Repositories.itinerary.ItineraryRepository;
import it.unical.ea.Travel.Repositories.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ItineraryService {

    private final ItineraryRepository itineraryRepository;
    private final UserRepository userRepository;
    private final ActivityRepository activityRepository;

    @Autowired
    public ItineraryService(ItineraryRepository itineraryRepository,
                            UserRepository userRepository,
                            ActivityRepository activityRepository) {
        this.itineraryRepository = itineraryRepository;
        this.userRepository = userRepository;
        this.activityRepository = activityRepository;
    }

    // Riceve tutti gli itinerari dal database
    public List<Itinerary> getAllItineraries() {
        return itineraryRepository.findAll();
    }

    // Cerca un singolo itinerario tramite il suo ID
    public Itinerary getItinerary(String stringId) {
        UUID uuid = UUID.fromString(stringId);
        return itineraryRepository.findById(uuid)
                .orElseThrow(() -> new RuntimeException("Itinerario non trovato con ID: " + stringId));
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
                .orElseThrow(() -> new RuntimeException("Utente non trovato con ID: " + creatorStringId));
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
}
