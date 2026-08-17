package it.unical.ea.Travel.Services.itinerary;

import it.unical.ea.Travel.Entities.itinerary.Itinerary;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItineraryServiceTest {

    @Mock
    private ItineraryRepository itineraryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ActivityRepository activityRepository;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private ItineraryBookingRepository itineraryBookingRepository;

    @Mock
    private ActivityBookingRepository activityBookingRepository;

    @Mock
    private ActivityService activityService;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private PaymentGateway paymentGateway;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private ItineraryService itineraryService;

    private User owner;
    private User otherUser;
    private Itinerary publicItinerary;
    private Itinerary privateItinerary;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setId(UUID.randomUUID());
        owner.setEmail("owner@example.com");

        otherUser = new User();
        otherUser.setId(UUID.randomUUID());
        otherUser.setEmail("other@example.com");

        publicItinerary = new Itinerary();
        publicItinerary.setId(UUID.randomUUID());
        publicItinerary.setTitle("Tour Pubblico");
        publicItinerary.setVisibility("PUBLIC");
        publicItinerary.setCreator(owner);

        privateItinerary = new Itinerary();
        privateItinerary.setId(UUID.randomUUID());
        privateItinerary.setTitle("Viaggio Privato");
        privateItinerary.setVisibility("PRIVATE");
        privateItinerary.setCreator(owner);
    }

    @Test
    void getAllItineraries_AsAdmin_ShouldReturnAll() {
        when(itineraryRepository.findAll()).thenReturn(List.of(publicItinerary, privateItinerary));

        List<Itinerary> result = itineraryService.getAllItineraries("admin@example.com", true);

        assertEquals(2, result.size());
        verify(itineraryRepository).findAll();
    }

    @Test
    void getAllItineraries_AsRegularUser_ShouldReturnPublicAndOwnPrivate() {
        when(userRepository.getUserByEmail(owner.getEmail())).thenReturn(Optional.of(owner));
        when(itineraryRepository.findPublicOrCreatorItineraries(owner.getId())).thenReturn(List.of(publicItinerary, privateItinerary));

        List<Itinerary> result = itineraryService.getAllItineraries(owner.getEmail(), false);

        assertEquals(2, result.size());
        verify(itineraryRepository).findPublicOrCreatorItineraries(owner.getId());
    }

    @Test
    void getAllItineraries_Unauthenticated_ShouldReturnOnlyPublic() {
        when(itineraryRepository.findByVisibilityIgnoreCase("PUBLIC")).thenReturn(List.of(publicItinerary));

        List<Itinerary> result = itineraryService.getAllItineraries(null, false);

        assertEquals(1, result.size());
        assertEquals("PUBLIC", result.get(0).getVisibility());
        verify(itineraryRepository).findByVisibilityIgnoreCase("PUBLIC");
    }

    @Test
    void getItinerary_Public_ShouldBeAccessibleByAnyone() {
        when(itineraryRepository.findById(publicItinerary.getId())).thenReturn(Optional.of(publicItinerary));

        Itinerary result = itineraryService.getItinerary(publicItinerary.getId().toString(), otherUser.getEmail(), false);

        assertNotNull(result);
        assertEquals(publicItinerary.getId(), result.getId());
    }

    @Test
    void getItinerary_Private_ByOwner_ShouldBeAccessible() {
        when(itineraryRepository.findById(privateItinerary.getId())).thenReturn(Optional.of(privateItinerary));

        Itinerary result = itineraryService.getItinerary(privateItinerary.getId().toString(), owner.getEmail(), false);

        assertNotNull(result);
        assertEquals(privateItinerary.getId(), result.getId());
    }

    @Test
    void getItinerary_Private_ByAdmin_ShouldBeAccessible() {
        when(itineraryRepository.findById(privateItinerary.getId())).thenReturn(Optional.of(privateItinerary));

        Itinerary result = itineraryService.getItinerary(privateItinerary.getId().toString(), "admin@example.com", true);

        assertNotNull(result);
        assertEquals(privateItinerary.getId(), result.getId());
    }

    @Test
    void getItinerary_Private_ByOtherUser_ShouldThrowForbidden() {
        when(itineraryRepository.findById(privateItinerary.getId())).thenReturn(Optional.of(privateItinerary));

        ApiException exception = assertThrows(ApiException.class, () ->
                itineraryService.getItinerary(privateItinerary.getId().toString(), otherUser.getEmail(), false)
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
    }

    @Test
    void getItinerariesByCreator_ByOwner_ShouldReturnAll() {
        when(userRepository.getUserByEmail(owner.getEmail())).thenReturn(Optional.of(owner));
        when(itineraryRepository.findByCreatorId(owner.getId())).thenReturn(List.of(publicItinerary, privateItinerary));

        List<Itinerary> result = itineraryService.getItinerariesByCreator(owner.getId().toString(), owner.getEmail(), false);

        assertEquals(2, result.size());
        verify(itineraryRepository).findByCreatorId(owner.getId());
    }

    @Test
    void getItinerariesByCreator_ByOtherUser_ShouldReturnOnlyPublic() {
        when(userRepository.getUserByEmail(otherUser.getEmail())).thenReturn(Optional.of(otherUser));
        when(itineraryRepository.findByCreatorIdAndVisibilityIgnoreCase(owner.getId(), "PUBLIC")).thenReturn(List.of(publicItinerary));

        List<Itinerary> result = itineraryService.getItinerariesByCreator(owner.getId().toString(), otherUser.getEmail(), false);

        assertEquals(1, result.size());
        assertEquals("PUBLIC", result.get(0).getVisibility());
        verify(itineraryRepository).findByCreatorIdAndVisibilityIgnoreCase(owner.getId(), "PUBLIC");
    }
}
