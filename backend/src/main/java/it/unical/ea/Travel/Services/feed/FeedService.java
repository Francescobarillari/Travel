package it.unical.ea.Travel.Services.feed;

import it.unical.ea.Travel.Entities.activity.Activity;
import it.unical.ea.Travel.Entities.activity.ActivityBooking;
import it.unical.ea.Travel.Entities.location.Location;
import it.unical.ea.Travel.Entities.user.User;
import it.unical.ea.Travel.Exception.ApiException;
import it.unical.ea.Travel.Mappers.location.LocationMapper;
import it.unical.ea.Travel.Repositories.activity.ActivityBookingRepository;
import it.unical.ea.Travel.Repositories.location.LocationRepository;
import it.unical.ea.Travel.Repositories.user.UserRepository;
import it.unical.ea.dtos.location.LocationDto;
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
    private final LocationRepository locationRepository;
    private final ActivityBookingRepository activityBookingRepository;
    private final LocationMapper locationMapper;

    @Transactional(readOnly = true)
    public List<LocationDto> getPersonalizedFeed(String userEmail) {
        User user = userRepository.getUserByEmail(userEmail)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "user.notFound"));

        // 1. Raccogli preferenze esplicite dell'utente
        Set<TravelTag> targetTags = new HashSet<>(user.getPreferences());

        // 2. Raccogli preferenze implicite dalle prenotazioni passate
        Set<String> targetLocations = new HashSet<>();
        List<ActivityBooking> bookings = activityBookingRepository.findByUserId(user.getId());
        
        for (ActivityBooking booking : bookings) {
            Activity act = booking.getActivity();
            if (act != null && act.getTemplate() != null) {
                // Estrai la località dell'attività (es. città)
                if (act.getTemplate().getLocationEntity() != null) {
                    targetLocations.add(act.getTemplate().getLocationEntity().getName());
                } else if (act.getTemplate().getLocation() != null && !act.getTemplate().getLocation().isBlank()) {
                    // Estrai solo il nome prima della virgola se presente, per fare corrispondenza sulla città
                    String city = act.getTemplate().getLocation().split(",")[0].trim();
                    if (!city.isEmpty()) {
                        targetLocations.add(city);
                    }
                }
                // Estrai i tag associati a quell'attività prenotata
                if (act.getTemplate().getTags() != null) {
                    targetTags.addAll(act.getTemplate().getTags());
                }
            }
        }

        // Se l'utente non ha preferenze ed è al primo viaggio, restituiamo tutte le località come raccomandazione predefinita
        if (targetTags.isEmpty() && targetLocations.isEmpty()) {
            return locationRepository.findAll().stream()
                    .map(locationMapper::toDto)
                    .collect(Collectors.toList());
        }

        // 3. Crea la Specification dinamica basata sull'API Criteria di JPA
        Specification<Location> spec = (root, query, cb) -> {
            // Rende la query distinct per evitare duplicati causati dalle JOIN
            query.distinct(true);
            
            List<Predicate> predicates = new ArrayList<>();

            // Condizione per i tag: controlla se una qualsiasi attività della località ha un tag che corrisponde
            if (!targetTags.isEmpty()) {
                Join<Location, it.unical.ea.Travel.Entities.activity.ActivityTemplate> activityJoin = root.join("activityTemplates", JoinType.LEFT);
                Join<it.unical.ea.Travel.Entities.activity.ActivityTemplate, TravelTag> tagJoin = activityJoin.join("tags", JoinType.LEFT);
                
                List<Predicate> tagPredicates = new ArrayList<>();
                for (TravelTag tag : targetTags) {
                    tagPredicates.add(cb.equal(tagJoin, tag));
                }
                predicates.add(cb.or(tagPredicates.toArray(new Predicate[0])));
            }

            // Condizione per la location: controlla se il nome della località è simile a una di quelle prenotate in passato
            if (!targetLocations.isEmpty()) {
                List<Predicate> locPredicates = new ArrayList<>();
                for (String loc : targetLocations) {
                    locPredicates.add(cb.like(cb.lower(root.get("name")), "%" + loc.toLowerCase() + "%"));
                }
                predicates.add(cb.or(locPredicates.toArray(new Predicate[0])));
            }

            if (predicates.isEmpty()) {
                return cb.conjunction();
            }

            // Combiniamo le clausole con OR per dare ampia rilevanza sia ai tag che alle location passate
            return cb.or(predicates.toArray(new Predicate[0]));
        };

        List<Location> matchedLocations = locationRepository.findAll(spec);

        // Fallback: se la query personalizzata non restituisce nulla, mostriamo tutte le località disponibili
        if (matchedLocations.isEmpty()) {
            return locationRepository.findAll().stream()
                    .map(locationMapper::toDto)
                    .collect(Collectors.toList());
        }

        return matchedLocations.stream()
                .map(locationMapper::toDto)
                .collect(Collectors.toList());
    }
}
