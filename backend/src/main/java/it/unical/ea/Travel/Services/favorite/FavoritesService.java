package it.unical.ea.Travel.Services.favorite;

import it.unical.ea.Travel.Entities.activity.Activity;
import it.unical.ea.Travel.Entities.itinerary.Itinerary;
import it.unical.ea.Travel.Entities.user.User;
import it.unical.ea.Travel.Exception.ApiException;
import it.unical.ea.Travel.Repositories.activity.ActivityRepository;
import it.unical.ea.Travel.Repositories.itinerary.ItineraryRepository;
import it.unical.ea.Travel.Repositories.user.UserRepository;
import it.unical.ea.dtos.favorite.UserFavoritesDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FavoritesService {

    private final UserRepository userRepository;
    private final ItineraryRepository itineraryRepository;
    private final ActivityRepository activityRepository;

    @Transactional(readOnly = true)
    public UserFavoritesDto getFavorites(String callerEmail) {
        User user = requireUser(callerEmail);
        UserFavoritesDto dto = new UserFavoritesDto();
        dto.setActivityIds(user.getFavoriteActivities().stream()
                .map(Activity::getId)
                .collect(Collectors.toSet()));
        dto.setItineraryIds(user.getFavoriteItineraries().stream()
                .map(Itinerary::getId)
                .collect(Collectors.toSet()));
        return dto;
    }

    @Transactional
    public void addFavoriteItinerary(UUID itineraryId, String callerEmail) {
        User user = requireUser(callerEmail);
        Itinerary itinerary = itineraryRepository.findById(itineraryId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "itinerary.notFound"));
        user.getFavoriteItineraries().add(itinerary);
        userRepository.save(user);
    }

    @Transactional
    public void removeFavoriteItinerary(UUID itineraryId, String callerEmail) {
        User user = requireUser(callerEmail);
        Itinerary itinerary = itineraryRepository.findById(itineraryId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "itinerary.notFound"));
        user.getFavoriteItineraries().remove(itinerary);
        userRepository.save(user);
    }

    @Transactional
    public void addFavoriteActivity(UUID activityId, String callerEmail) {
        User user = requireUser(callerEmail);
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "activity.notFound"));
        user.getFavoriteActivities().add(activity);
        userRepository.save(user);
    }

    @Transactional
    public void removeFavoriteActivity(UUID activityId, String callerEmail) {
        User user = requireUser(callerEmail);
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "activity.notFound"));
        user.getFavoriteActivities().remove(activity);
        userRepository.save(user);
    }

    private User requireUser(String email) {
        return userRepository.getUserByEmail(email)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "user.notFound"));
    }
}
