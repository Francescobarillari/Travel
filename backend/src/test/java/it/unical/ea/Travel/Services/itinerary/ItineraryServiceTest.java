package it.unical.ea.Travel.Services.itinerary;

import it.unical.ea.Travel.Entities.itinerary.Itinerary;
import it.unical.ea.Travel.Entities.itinerary.ItineraryJoinRequest;
import it.unical.ea.Travel.Entities.user.User;
import it.unical.ea.Travel.Exception.ApiException;
import it.unical.ea.Travel.Repositories.activity.ActivityBookingRepository;
import it.unical.ea.Travel.Repositories.activity.ActivityRepository;
import it.unical.ea.Travel.Repositories.itinerary.ItineraryBookingRepository;
import it.unical.ea.Travel.Repositories.itinerary.ItineraryJoinRequestRepository;
import it.unical.ea.Travel.Repositories.itinerary.ItineraryRepository;
import it.unical.ea.Travel.Repositories.user.UserRepository;
import it.unical.ea.Travel.Services.activity.ActivityService;
import it.unical.ea.Travel.Services.audit.AuditLogService;
import it.unical.ea.Travel.Services.notification.NotificationService;
import it.unical.ea.Travel.Services.payment.PaymentGateway;
import it.unical.ea.Travel.Services.storage.FileStorageService;
import it.unical.ea.enums.JoinRequestStatus;
import it.unical.ea.enums.NotificationType;
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

    @Mock
    private ItineraryJoinRequestRepository itineraryJoinRequestRepository;

    @InjectMocks
    private ItineraryService itineraryService;

    private User owner;
    private User otherUser;
    private Itinerary publicItinerary;
    private Itinerary privateItinerary;
    private Itinerary sharedItinerary;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setId(UUID.randomUUID());
        owner.setEmail("owner@example.com");
        owner.setFirstName("Owner");
        owner.setLastName("User");

        otherUser = new User();
        otherUser.setId(UUID.randomUUID());
        otherUser.setEmail("other@example.com");
        otherUser.setFirstName("Other");
        otherUser.setLastName("User");

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

        sharedItinerary = new Itinerary();
        sharedItinerary.setId(UUID.randomUUID());
        sharedItinerary.setTitle("Viaggio Condiviso");
        sharedItinerary.setVisibility("SHARED");
        sharedItinerary.setShareCode("TRV8K2");
        sharedItinerary.setCreator(owner);
    }

    @Test
    void getAllItineraries_AsAdmin_ShouldReturnAll() {
        when(itineraryRepository.findAll()).thenReturn(List.of(publicItinerary, privateItinerary, sharedItinerary));

        List<Itinerary> result = itineraryService.getAllItineraries("admin@example.com", true);

        assertEquals(3, result.size());
        verify(itineraryRepository).findAll();
    }

    @Test
    void getAllItineraries_AsRegularUser_ShouldReturnPublicOwnAndSharedParticipant() {
        when(userRepository.getUserByEmail(owner.getEmail())).thenReturn(Optional.of(owner));
        when(itineraryRepository.findPublicOrCreatorOrParticipantItineraries(owner.getId())).thenReturn(List.of(publicItinerary, privateItinerary, sharedItinerary));

        List<Itinerary> result = itineraryService.getAllItineraries(owner.getEmail(), false);

        assertEquals(3, result.size());
        verify(itineraryRepository).findPublicOrCreatorOrParticipantItineraries(owner.getId());
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
    void getItinerary_Private_ByOtherUser_ShouldThrowForbidden() {
        when(itineraryRepository.findById(privateItinerary.getId())).thenReturn(Optional.of(privateItinerary));

        ApiException exception = assertThrows(ApiException.class, () ->
                itineraryService.getItinerary(privateItinerary.getId().toString(), otherUser.getEmail(), false)
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
    }

    @Test
    void getItinerary_Shared_ByAcceptedParticipant_ShouldBeAccessible() {
        when(itineraryRepository.findById(sharedItinerary.getId())).thenReturn(Optional.of(sharedItinerary));
        when(userRepository.getUserByEmail(otherUser.getEmail())).thenReturn(Optional.of(otherUser));
        when(itineraryJoinRequestRepository.existsByUserIdAndItineraryIdAndStatus(otherUser.getId(), sharedItinerary.getId(), JoinRequestStatus.ACCEPTED))
                .thenReturn(true);

        Itinerary result = itineraryService.getItinerary(sharedItinerary.getId().toString(), otherUser.getEmail(), false);

        assertNotNull(result);
        assertEquals(sharedItinerary.getId(), result.getId());
    }

    @Test
    void getItinerary_Shared_ByNonParticipant_ShouldThrowForbidden() {
        when(itineraryRepository.findById(sharedItinerary.getId())).thenReturn(Optional.of(sharedItinerary));
        when(userRepository.getUserByEmail(otherUser.getEmail())).thenReturn(Optional.of(otherUser));
        when(itineraryJoinRequestRepository.existsByUserIdAndItineraryIdAndStatus(otherUser.getId(), sharedItinerary.getId(), JoinRequestStatus.ACCEPTED))
                .thenReturn(false);

        ApiException exception = assertThrows(ApiException.class, () ->
                itineraryService.getItinerary(sharedItinerary.getId().toString(), otherUser.getEmail(), false)
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
    }

    @Test
    void requestJoinByCode_Success_ShouldCreatePendingRequestAndNotifyOwner() {
        when(itineraryRepository.findByShareCode("TRV8K2")).thenReturn(Optional.of(sharedItinerary));
        when(userRepository.getUserByEmail(otherUser.getEmail())).thenReturn(Optional.of(otherUser));
        when(itineraryJoinRequestRepository.findByUserIdAndItineraryId(otherUser.getId(), sharedItinerary.getId())).thenReturn(Optional.empty());
        when(itineraryJoinRequestRepository.save(any(ItineraryJoinRequest.class))).thenAnswer(i -> {
            ItineraryJoinRequest r = i.getArgument(0);
            r.setId(UUID.randomUUID());
            return r;
        });

        ItineraryJoinRequest request = itineraryService.requestJoinByCode("TRV8K2", otherUser.getEmail());

        assertNotNull(request);
        assertEquals(JoinRequestStatus.PENDING, request.getStatus());
        verify(notificationService).createNotification(
                eq(owner),
                contains("Partecipazione"),
                contains("Other User"),
                eq(NotificationType.RICHIESTA_PARTECIPAZIONE_ITINERARIO)
        );
    }

    @Test
    void requestJoinByCode_ByCreator_ShouldThrowBadRequest() {
        when(itineraryRepository.findByShareCode("TRV8K2")).thenReturn(Optional.of(sharedItinerary));
        when(userRepository.getUserByEmail(owner.getEmail())).thenReturn(Optional.of(owner));

        ApiException exception = assertThrows(ApiException.class, () ->
                itineraryService.requestJoinByCode("TRV8K2", owner.getEmail())
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("itinerary.join.isCreator", exception.getMessage());
    }

    @Test
    void acceptJoinRequest_ShouldUpdateStatusAndNotifyRequester() {
        ItineraryJoinRequest joinRequest = new ItineraryJoinRequest();
        joinRequest.setId(UUID.randomUUID());
        joinRequest.setItinerary(sharedItinerary);
        joinRequest.setUser(otherUser);
        joinRequest.setStatus(JoinRequestStatus.PENDING);

        when(itineraryJoinRequestRepository.findById(joinRequest.getId())).thenReturn(Optional.of(joinRequest));
        when(itineraryJoinRequestRepository.save(any(ItineraryJoinRequest.class))).thenReturn(joinRequest);

        ItineraryJoinRequest result = itineraryService.acceptJoinRequest(joinRequest.getId().toString(), owner.getEmail(), false);

        assertEquals(JoinRequestStatus.ACCEPTED, result.getStatus());
        verify(notificationService).createNotification(
                eq(otherUser),
                contains("Accettata"),
                contains(sharedItinerary.getTitle()),
                eq(NotificationType.ACCETTAZIONE_PARTECIPAZIONE_ITINERARIO)
        );
    }

    @Test
    void rejectJoinRequest_ShouldUpdateStatusAndNotifyRequester() {
        ItineraryJoinRequest joinRequest = new ItineraryJoinRequest();
        joinRequest.setId(UUID.randomUUID());
        joinRequest.setItinerary(sharedItinerary);
        joinRequest.setUser(otherUser);
        joinRequest.setStatus(JoinRequestStatus.PENDING);

        when(itineraryJoinRequestRepository.findById(joinRequest.getId())).thenReturn(Optional.of(joinRequest));
        when(itineraryJoinRequestRepository.save(any(ItineraryJoinRequest.class))).thenReturn(joinRequest);

        ItineraryJoinRequest result = itineraryService.rejectJoinRequest(joinRequest.getId().toString(), owner.getEmail(), false);

        assertEquals(JoinRequestStatus.REJECTED, result.getStatus());
        verify(notificationService).createNotification(
                eq(otherUser),
                contains("Rifiutata"),
                contains(sharedItinerary.getTitle()),
                eq(NotificationType.RIFIUTO_PARTECIPAZIONE_ITINERARIO)
        );
    }
}
