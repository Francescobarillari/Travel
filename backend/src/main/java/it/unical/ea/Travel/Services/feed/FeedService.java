package it.unical.ea.Travel.Services.feed;

import it.unical.ea.Travel.Entities.activity.Activity;
import it.unical.ea.Travel.Entities.activity.ActivityBooking;
import it.unical.ea.Travel.Entities.trip.Trip;
import it.unical.ea.Travel.Entities.user.User;
import it.unical.ea.Travel.Exception.ApiException;
import it.unical.ea.Travel.Mappers.trip.TripMapper;
import it.unical.ea.Travel.Repositories.activity.ActivityBookingRepository;
import it.unical.ea.Travel.Repositories.trip.TripRepository;
import it.unical.ea.Travel.Repositories.user.UserRepository;
import it.unical.ea.dtos.trip.TripDto;
import it.unical.ea.enums.TravelTag;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FeedService {

    private final UserRepository userRepository;
    private final TripRepository tripRepository;
    private final ActivityBookingRepository activityBookingRepository;
    private final TripMapper tripMapper;

    @Transactional(readOnly = true)
    public List<TripDto> getPersonalizedFeed(String userEmail) {
        User user = userRepository.getUserByEmail(userEmail)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "user.notFound"));

        // 1. Raccogli preferenze esplicite dell'utente
        Set<TravelTag> targetTags = new HashSet<>(user.getPreferences());

        // 2. Raccogli preferenze implicite dalle prenotazioni passate
        Set<String> targetLocations = new HashSet<>();
        List<ActivityBooking> bookings = activityBookingRepository.findByUserId(user.getId());
        
        for (ActivityBooking booking : bookings) {
            Activity act = booking.getActivity();
            if (act != null) {
                // Estrai la località dell'attività (es. città)
                if (act.getLocation() != null && !act.getLocation().isBlank()) {
                    // Estrai solo il nome prima della virgola se presente, per fare corrispondenza sulla città
                    String city = act.getLocation().split(",")[0].trim();
                    if (!city.isEmpty()) {
                        targetLocations.add(city);
                    }
                }
                // Estrai i tag associati a quell'attività prenotata
                if (act.getTags() != null) {
                    targetTags.addAll(act.getTags());
                }
            }
        }

        // Se l'utente non ha preferenze ed è al primo viaggio, restituiamo tutti i viaggi come raccomandazione predefinita
        if (targetTags.isEmpty() && targetLocations.isEmpty()) {
            return tripRepository.findAll().stream()
                    .map(tripMapper::toDto)
                    .collect(Collectors.toList());
        }

        // 3. Crea la Specification dinamica basata sull'API Criteria di JPA
        Specification<Trip> spec = (root, query, cb) -> {
            // Rende la query distinct per evitare duplicati causati dalle JOIN
            query.distinct(true);
            
            List<Predicate> predicates = new ArrayList<>();

            // Condizione per i tag: controlla se una qualsiasi attività del viaggio ha un tag che corrisponde
            if (!targetTags.isEmpty()) {
                Join<Trip, Activity> activityJoin = root.join("activities", JoinType.LEFT);
                Join<Activity, TravelTag> tagJoin = activityJoin.join("tags", JoinType.LEFT);
                
                List<Predicate> tagPredicates = new ArrayList<>();
                for (TravelTag tag : targetTags) {
                    tagPredicates.add(cb.equal(tagJoin, tag));
                }
                predicates.add(cb.or(tagPredicates.toArray(new Predicate[0])));
            }

            // Condizione per la location: controlla se la località del viaggio è simile a una di quelle prenotate in passato
            if (!targetLocations.isEmpty()) {
                List<Predicate> locPredicates = new ArrayList<>();
                for (String loc : targetLocations) {
                    locPredicates.add(cb.like(cb.lower(root.get("location")), "%" + loc.toLowerCase() + "%"));
                }
                predicates.add(cb.or(locPredicates.toArray(new Predicate[0])));
            }

            if (predicates.isEmpty()) {
                return cb.conjunction();
            }

            // Combiniamo le clausole con OR per dare ampia rilevanza sia ai tag che alle location passate
            return cb.or(predicates.toArray(new Predicate[0]));
        };

        List<Trip> matchedTrips = tripRepository.findAll(spec);

        // Fallback: se la query personalizzata non restituisce nulla, mostriamo tutti i viaggi disponibili
        if (matchedTrips.isEmpty()) {
            return tripRepository.findAll().stream()
                    .map(tripMapper::toDto)
                    .collect(Collectors.toList());
        }

        return matchedTrips.stream()
                .map(tripMapper::toDto)
                .collect(Collectors.toList());
    }
}
