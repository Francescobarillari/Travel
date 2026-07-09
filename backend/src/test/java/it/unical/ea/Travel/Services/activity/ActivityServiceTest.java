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
import it.unical.ea.Travel.Services.audit.AuditLogService;
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
    private it.unical.ea.Travel.Repositories.activity.ActivityTemplateRepository activityTemplateRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ActivityMapper activityMapper;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private AuditLogService auditLogService;

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
        it.unical.ea.Travel.Entities.activity.ActivityTemplate template = new it.unical.ea.Travel.Entities.activity.ActivityTemplate();
        template.setName("Visita al Colosseo");
        template.setImages(new java.util.ArrayList<>());

        futureActivity = new Activity();
        futureActivity.setId(activityId);
        futureActivity.setTemplate(template);
        futureActivity.setStartTime(LocalDateTime.now().plusDays(7));
        futureActivity.setEndTime(LocalDateTime.now().plusDays(7).plusHours(2));
        futureActivity.setParticipants(20);

        it.unical.ea.Travel.Entities.activity.ActivityTemplate pastTemplate = new it.unical.ea.Travel.Entities.activity.ActivityTemplate();
        pastTemplate.setName("Attività Passata");
        pastTemplate.setImages(new java.util.ArrayList<>());

        pastActivity = new Activity();
        pastActivity.setId(pastActivityId);
        pastActivity.setTemplate(pastTemplate);
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
            when(activityBookingRepository.save(any(ActivityBooking.class))).thenAnswer(invocation -> {
                ActivityBooking b = invocation.getArgument(0);
                b.setId(UUID.randomUUID());
                return b;
            });

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
            ActivityBooking mockBooking = new ActivityBooking();
            mockBooking.setId(UUID.randomUUID());
            mockBooking.setStatus(it.unical.ea.Travel.Entities.payment.BookingStatus.CONFIRMED);
            when(activityBookingRepository.findByUserIdAndActivityId(any(), any()))
                    .thenReturn(Optional.of(mockBooking));

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
            when(userRepository.getUserByEmail(userEmail)).thenReturn(Optional.of(testUser));
            when(activityRepository.findByIdForUpdate(pastActivityId)).thenReturn(Optional.of(pastActivity));
            when(activityBookingRepository.findByUserIdAndActivityId(userId, pastActivityId))
                    .thenReturn(Optional.of(new ActivityBooking()));

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

    @Nested
    @DisplayName("uploadImages")
    class UploadImagesTests {

        @Test
        @DisplayName("Dovrebbe lanciare eccezione se l'array dei file è nullo o vuoto")
        void shouldThrowIfFilesNullOrEmpty() {
            ApiException exNull = assertThrows(ApiException.class,
                    () -> activityService.uploadImages(activityId.toString(), null));
            assertEquals(HttpStatus.BAD_REQUEST, exNull.getStatus());
            assertEquals("file.empty", exNull.getMessageKey());

            org.springframework.web.multipart.MultipartFile[] emptyArray = new org.springframework.web.multipart.MultipartFile[0];
            ApiException exEmpty = assertThrows(ApiException.class,
                    () -> activityService.uploadImages(activityId.toString(), emptyArray));
            assertEquals(HttpStatus.BAD_REQUEST, exEmpty.getStatus());
            assertEquals("file.empty", exEmpty.getMessageKey());
        }

        @Test
        @DisplayName("Dovrebbe lanciare eccezione se vengono caricati più di 5 file")
        void shouldThrowIfTooManyFiles() {
            org.springframework.web.multipart.MultipartFile mockFile = mock(org.springframework.web.multipart.MultipartFile.class);
            org.springframework.web.multipart.MultipartFile[] tooManyFiles = new org.springframework.web.multipart.MultipartFile[6];
            for (int i = 0; i < 6; i++) {
                tooManyFiles[i] = mockFile;
            }

            ApiException ex = assertThrows(ApiException.class,
                    () -> activityService.uploadImages(activityId.toString(), tooManyFiles));
            assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
            assertEquals("activity.images.maxCountExceeded", ex.getMessageKey());
        }

        @Test
        @DisplayName("Dovrebbe caricare correttamente fino a 5 immagini")
        void shouldUploadImagesSuccessfully() {
            org.springframework.web.multipart.MultipartFile file1 = mock(org.springframework.web.multipart.MultipartFile.class);
            org.springframework.web.multipart.MultipartFile file2 = mock(org.springframework.web.multipart.MultipartFile.class);
            org.springframework.web.multipart.MultipartFile[] files = {file1, file2};

            when(activityRepository.findById(activityId)).thenReturn(Optional.of(futureActivity));
            when(fileStorageService.store(file1, "activities")).thenReturn("activities/img1.jpg");
            when(fileStorageService.store(file2, "activities")).thenReturn("activities/img2.jpg");
            when(activityRepository.save(any(Activity.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(activityMapper.toDTO(any(Activity.class))).thenReturn(new ActivityDto());

            assertDoesNotThrow(() -> activityService.uploadImages(activityId.toString(), files));

            verify(fileStorageService).store(file1, "activities");
            verify(fileStorageService).store(file2, "activities");
            verify(activityRepository).save(futureActivity);
            assertTrue(futureActivity.getTemplate().getImages().contains("activities/img1.jpg"));
            assertTrue(futureActivity.getTemplate().getImages().contains("activities/img2.jpg"));
        }
    }
}
