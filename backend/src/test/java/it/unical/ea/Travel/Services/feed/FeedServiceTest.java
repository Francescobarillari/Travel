package it.unical.ea.Travel.Services.feed;

import it.unical.ea.Travel.Entities.activity.Activity;
import it.unical.ea.Travel.Entities.activity.ActivityBooking;
import it.unical.ea.Travel.Entities.location.Location;
import it.unical.ea.Travel.Entities.user.User;
import it.unical.ea.Travel.Mappers.location.LocationMapper;
import it.unical.ea.Travel.Repositories.activity.ActivityBookingRepository;
import it.unical.ea.Travel.Repositories.location.LocationRepository;
import it.unical.ea.Travel.Repositories.user.UserRepository;
import it.unical.ea.dtos.location.LocationDto;
import it.unical.ea.enums.TravelTag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FeedServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private ActivityBookingRepository activityBookingRepository;

    @Mock
    private LocationMapper locationMapper;

    @InjectMocks
    private FeedService feedService;

    private User testUser;
    private final String userEmail = "traveler@example.com";

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setEmail(userEmail);
        testUser.setPreferences(new HashSet<>());
    }

    @Test
    void shouldReturnAllLocationsIfNoPreferencesAndNoBookings() {
        when(userRepository.getUserByEmail(userEmail)).thenReturn(Optional.of(testUser));
        when(activityBookingRepository.findByUserId(testUser.getId())).thenReturn(Collections.emptyList());

        Location location = new Location();
        location.setId(UUID.randomUUID());
        location.setName("Roma, Italia");

        LocationDto dto = new LocationDto();
        dto.setId(location.getId());
        dto.setName(location.getName());

        when(locationRepository.findAll()).thenReturn(Collections.singletonList(location));
        when(locationMapper.toDto(location)).thenReturn(dto);

        List<LocationDto> result = feedService.getPersonalizedFeed(userEmail);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Roma, Italia", result.get(0).getName());
        verify(locationRepository, times(1)).findAll();
        verify(locationRepository, never()).findAll(any(Specification.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldQueryUsingSpecificationIfPreferencesExist() {
        testUser.getPreferences().add(TravelTag.AVVENTURA);
        when(userRepository.getUserByEmail(userEmail)).thenReturn(Optional.of(testUser));
        when(activityBookingRepository.findByUserId(testUser.getId())).thenReturn(Collections.emptyList());

        Location location = new Location();
        location.setId(UUID.randomUUID());
        location.setName("Trentino-Alto Adige");

        LocationDto dto = new LocationDto();
        dto.setId(location.getId());
        dto.setName(location.getName());

        when(locationRepository.findAll(any(Specification.class))).thenReturn(Collections.singletonList(location));
        when(locationMapper.toDto(location)).thenReturn(dto);

        List<LocationDto> result = feedService.getPersonalizedFeed(userEmail);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Trentino-Alto Adige", result.get(0).getName());
        verify(locationRepository, times(1)).findAll(any(Specification.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldExtractPastBookingsAndUseSpecification() {
        when(userRepository.getUserByEmail(userEmail)).thenReturn(Optional.of(testUser));

        Activity activity = new Activity();
        activity.setLocation("Roma, Italia");
        activity.setTags(new HashSet<>(Collections.singletonList(TravelTag.STORIA)));

        Location actLoc = new Location();
        actLoc.setName("Roma, Italia");
        activity.setLocationEntity(actLoc);

        ActivityBooking booking = new ActivityBooking();
        booking.setActivity(activity);

        when(activityBookingRepository.findByUserId(testUser.getId())).thenReturn(Collections.singletonList(booking));

        Location recommendation = new Location();
        recommendation.setId(UUID.randomUUID());
        recommendation.setName("Roma, Italia");

        LocationDto dto = new LocationDto();
        dto.setId(recommendation.getId());
        dto.setName(recommendation.getName());

        when(locationRepository.findAll(any(Specification.class))).thenReturn(Collections.singletonList(recommendation));
        when(locationMapper.toDto(recommendation)).thenReturn(dto);

        List<LocationDto> result = feedService.getPersonalizedFeed(userEmail);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Roma, Italia", result.get(0).getName());
        verify(locationRepository, times(1)).findAll(any(Specification.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldFallbackToAllLocationsIfSpecificationReturnsNoResults() {
        testUser.getPreferences().add(TravelTag.RELAX);
        when(userRepository.getUserByEmail(userEmail)).thenReturn(Optional.of(testUser));
        when(activityBookingRepository.findByUserId(testUser.getId())).thenReturn(Collections.emptyList());

        // Spec query returns empty
        when(locationRepository.findAll(any(Specification.class))).thenReturn(Collections.emptyList());

        // Fallback: findAll returns locations
        Location location = new Location();
        location.setId(UUID.randomUUID());
        location.setName("Dolomiti");

        LocationDto dto = new LocationDto();
        dto.setId(location.getId());
        dto.setName(location.getName());

        when(locationRepository.findAll()).thenReturn(Collections.singletonList(location));
        when(locationMapper.toDto(location)).thenReturn(dto);

        List<LocationDto> result = feedService.getPersonalizedFeed(userEmail);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Dolomiti", result.get(0).getName());
        verify(locationRepository, times(1)).findAll(any(Specification.class));
        verify(locationRepository, times(1)).findAll();
    }
}
