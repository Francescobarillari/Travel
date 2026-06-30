package it.unical.ea.Travel.Services.activity;

import it.unical.ea.Travel.Entities.activity.Activity;
import it.unical.ea.Travel.Entities.activity.ActivityBooking;
import it.unical.ea.Travel.Entities.user.User;
import it.unical.ea.Travel.Exception.ApiException;
import it.unical.ea.Travel.Mappers.activity.ActivityMapper;
import it.unical.ea.Travel.Repositories.activity.ActivityBookingRepository;
import it.unical.ea.Travel.Repositories.activity.ActivityRepository;
import it.unical.ea.Travel.Repositories.user.UserRepository;
import it.unical.ea.Travel.Services.storage.FileStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ActivityServiceTest {

    @Mock
    private ActivityRepository activityRepository;

    @Mock
    private ActivityBookingRepository activityBookingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ActivityMapper activityMapper;

    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private ActivityService activityService;

    private Activity futureActivity;
    private Activity pastActivity;
    private User testUser;
    private final UUID activityId = UUID.randomUUID();
    private final UUID pastActivityId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();
    private final String userEmail = "test@example.com";

    @BeforeEach
    void setUp() {
        futureActivity = new Activity();
        futureActivity.setId(activityId);
        futureActivity.setName("Visita al Colosseo");
        futureActivity.setStartTime(LocalDateTime.now().plusDays(7));
        futureActivity.setEndTime(LocalDateTime.now().plusDays(7).plusHours(2));
        futureActivity.setParticipants(20);

        pastActivity = new Activity();
        pastActivity.setId(pastActivityId);
        pastActivity.setName("Attività Passata");
        pastActivity.setStartTime(LocalDateTime.now().minusDays(7));
        pastActivity.setEndTime(LocalDateTime.now().minusDays(7).plusHours(2));
        pastActivity.setParticipants(10);

        testUser = new User();
        testUser.setId(userId);
        testUser.setEmail(userEmail);
    }

    // --- Test Prenotazione ---

    @Nested
    @DisplayName("bookActivity")
    class BookActivityTests {

        @Test
        @DisplayName("Dovrebbe prenotare con successo un'attività futura")
        void shouldBookFutureActivity() {
            when(activityRepository.findByIdForUpdate(activityId)).thenReturn(Optional.of(futureActivity));
            when(userRepository.getUserByEmail(userEmail)).thenReturn(Optional.of(testUser));
            when(activityBookingRepository.countDirectParticipants(activityId)).thenReturn(5L);
            when(activityBookingRepository.findByUserIdAndActivityId(userId, activityId)).thenReturn(Optional.empty());

            assertDoesNotThrow(() -> activityService.bookActivity(activityId.toString(), userEmail));

            verify(activityBookingRepository).save(any(ActivityBooking.class));
        }

        @Test
        @DisplayName("Dovrebbe bloccare la prenotazione per un'attività passata")
        void shouldBlockBookingForPastActivity() {
            when(activityRepository.findByIdForUpdate(pastActivityId)).thenReturn(Optional.of(pastActivity));

            ApiException exception = assertThrows(ApiException.class,
                    () -> activityService.bookActivity(pastActivityId.toString(), userEmail));

            assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
            assertEquals("activity.booking.pastEvent", exception.getMessageKey());
            verify(activityBookingRepository, never()).save(any());
        }

        @Test
        @DisplayName("Dovrebbe bloccare la prenotazione quando la capienza è piena")
        void shouldBlockBookingWhenFull() {
            when(activityRepository.findByIdForUpdate(activityId)).thenReturn(Optional.of(futureActivity));
            when(userRepository.getUserByEmail(userEmail)).thenReturn(Optional.of(testUser));
            // Simula capienza piena: 20 partecipanti su 20 massimi
            when(activityBookingRepository.countDirectParticipants(activityId)).thenReturn(20L);

            ApiException exception = assertThrows(ApiException.class,
                    () -> activityService.bookActivity(activityId.toString(), userEmail));

            assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
            assertEquals("activity.booking.full", exception.getMessageKey());
            verify(activityBookingRepository, never()).save(any());
        }

        @Test
        @DisplayName("Dovrebbe bloccare la prenotazione duplicata")
        void shouldBlockDuplicateBooking() {
            when(activityRepository.findByIdForUpdate(activityId)).thenReturn(Optional.of(futureActivity));
            when(userRepository.getUserByEmail(userEmail)).thenReturn(Optional.of(testUser));
            when(activityBookingRepository.countDirectParticipants(activityId)).thenReturn(5L);
            when(activityBookingRepository.findByUserIdAndActivityId(userId, activityId))
                    .thenReturn(Optional.of(new ActivityBooking()));

            ApiException exception = assertThrows(ApiException.class,
                    () -> activityService.bookActivity(activityId.toString(), userEmail));

            assertEquals(HttpStatus.CONFLICT, exception.getStatus());
            assertEquals("activity.booking.alreadyBooked", exception.getMessageKey());
            verify(activityBookingRepository, never()).save(any());
        }

        @Test
        @DisplayName("Dovrebbe lanciare errore se l'attività non esiste")
        void shouldThrowIfActivityNotFound() {
            UUID nonExistentId = UUID.randomUUID();
            when(activityRepository.findByIdForUpdate(nonExistentId)).thenReturn(Optional.empty());

            ApiException exception = assertThrows(ApiException.class,
                    () -> activityService.bookActivity(nonExistentId.toString(), userEmail));

            assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
            assertEquals("activity.notFound", exception.getMessageKey());
        }

        @Test
        @DisplayName("Dovrebbe lanciare errore se l'utente non esiste")
        void shouldThrowIfUserNotFound() {
            when(activityRepository.findByIdForUpdate(activityId)).thenReturn(Optional.of(futureActivity));
            when(userRepository.getUserByEmail("unknown@example.com")).thenReturn(Optional.empty());

            ApiException exception = assertThrows(ApiException.class,
                    () -> activityService.bookActivity(activityId.toString(), "unknown@example.com"));

            assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
            assertEquals("user.notFound", exception.getMessageKey());
        }
    }

    // --- Test Cancellazione ---

    @Nested
    @DisplayName("cancelActivityBooking")
    class CancelBookingTests {

        @Test
        @DisplayName("Dovrebbe cancellare con successo una prenotazione per un'attività futura")
        void shouldCancelFutureBooking() {
            ActivityBooking existingBooking = new ActivityBooking();
            existingBooking.setId(UUID.randomUUID());

            when(activityRepository.findByIdForUpdate(activityId)).thenReturn(Optional.of(futureActivity));
            when(userRepository.getUserByEmail(userEmail)).thenReturn(Optional.of(testUser));
            when(activityBookingRepository.findByUserIdAndActivityId(userId, activityId))
                    .thenReturn(Optional.of(existingBooking));

            assertDoesNotThrow(() -> activityService.cancelActivityBooking(activityId.toString(), userEmail));

            verify(activityBookingRepository).delete(existingBooking);
        }

        @Test
        @DisplayName("Dovrebbe bloccare la cancellazione per un'attività passata")
        void shouldBlockCancellationForPastActivity() {
            when(activityRepository.findByIdForUpdate(pastActivityId)).thenReturn(Optional.of(pastActivity));

            ApiException exception = assertThrows(ApiException.class,
                    () -> activityService.cancelActivityBooking(pastActivityId.toString(), userEmail));

            assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
            assertEquals("activity.booking.pastEvent", exception.getMessageKey());
            verify(activityBookingRepository, never()).delete(any(ActivityBooking.class));
        }

        @Test
        @DisplayName("Dovrebbe lanciare errore se la prenotazione non esiste")
        void shouldThrowIfBookingNotFound() {
            when(activityRepository.findByIdForUpdate(activityId)).thenReturn(Optional.of(futureActivity));
            when(userRepository.getUserByEmail(userEmail)).thenReturn(Optional.of(testUser));
            when(activityBookingRepository.findByUserIdAndActivityId(userId, activityId))
                    .thenReturn(Optional.empty());

            ApiException exception = assertThrows(ApiException.class,
                    () -> activityService.cancelActivityBooking(activityId.toString(), userEmail));

            assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
            assertEquals("activity.booking.notFound", exception.getMessageKey());
        }
    }

    // --- Test Calcolo Partecipanti ---

    @Nested
    @DisplayName("calculateCurrentParticipants")
    class CalculateParticipantsTests {

        @Test
        @DisplayName("Dovrebbe restituire il conteggio corretto dei partecipanti diretti")
        void shouldReturnCorrectDirectCount() {
            when(activityBookingRepository.countDirectParticipants(activityId)).thenReturn(7L);

            int count = activityService.calculateCurrentParticipants(futureActivity);

            assertEquals(7, count);
        }

        @Test
        @DisplayName("Dovrebbe restituire 0 quando non ci sono prenotazioni")
        void shouldReturnZeroWhenNoBookings() {
            when(activityBookingRepository.countDirectParticipants(activityId)).thenReturn(0L);

            int count = activityService.calculateCurrentParticipants(futureActivity);

            assertEquals(0, count);
        }
    }
}
