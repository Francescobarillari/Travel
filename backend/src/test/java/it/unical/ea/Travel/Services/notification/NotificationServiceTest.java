package it.unical.ea.Travel.Services.notification;

import it.unical.ea.Travel.Entities.notification.Notification;
import it.unical.ea.Travel.Entities.user.User;
import it.unical.ea.Travel.Exception.ApiException;
import it.unical.ea.Travel.Mappers.notification.NotificationMapper;
import it.unical.ea.Travel.Repositories.notification.NotificationRepository;
import it.unical.ea.dtos.notification.NotificationDto;
import it.unical.ea.enums.NotificationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationMapper notificationMapper;

    @InjectMocks
    private NotificationService notificationService;

    private User testUser;
    private User otherUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setEmail("user@example.com");

        otherUser = new User();
        otherUser.setId(UUID.randomUUID());
        otherUser.setEmail("other@example.com");
    }

    @Test
    @DisplayName("registerEmitter - Limita il numero massimo di connessioni contemporanee a 5")
    void registerEmitter_ShouldEnforceMaxConnectionLimit() {
        UUID userId = testUser.getId();

        // Registra 7 connessioni SSE per lo stesso utente
        for (int i = 0; i < 7; i++) {
            SseEmitter emitter = notificationService.registerEmitter(userId);
            assertNotNull(emitter);
        }

        // Verifica che il numero di emitter attivi non superi mai 5
        assertEquals(5, notificationService.getActiveEmittersCount(userId));
    }

    @Test
    @DisplayName("createNotification - Crea e salva la notifica")
    void createNotification_ShouldSaveAndReturnDto() {
        Notification saved = new Notification();
        saved.setId(UUID.randomUUID());
        saved.setUser(testUser);
        saved.setTitle("Titolo");
        saved.setMessage("Messaggio");
        saved.setType(NotificationType.PRENOTAZIONE_SUCCESSO);

        NotificationDto dto = new NotificationDto();
        dto.setId(saved.getId());
        dto.setTitle("Titolo");

        when(notificationRepository.save(any(Notification.class))).thenReturn(saved);
        when(notificationMapper.toDTO(saved)).thenReturn(dto);

        NotificationDto result = notificationService.createNotification(testUser, "Titolo", "Messaggio", NotificationType.PRENOTAZIONE_SUCCESSO);

        assertNotNull(result);
        assertEquals(dto.getId(), result.getId());
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    @DisplayName("markAsRead - Utente autorizzato segna notifica come letta")
    void markAsRead_ByOwner_ShouldSucceed() {
        UUID notifId = UUID.randomUUID();
        Notification notif = new Notification();
        notif.setId(notifId);
        notif.setUser(testUser);
        notif.setRead(false);

        when(notificationRepository.findById(notifId)).thenReturn(Optional.of(notif));

        notificationService.markAsRead(notifId, testUser);

        assertTrue(notif.isRead());
        verify(notificationRepository).save(notif);
    }

    @Test
    @DisplayName("markAsRead - Altro utente tenta di segnare notifica non sua -> FORBIDDEN")
    void markAsRead_ByOtherUser_ShouldThrowForbidden() {
        UUID notifId = UUID.randomUUID();
        Notification notif = new Notification();
        notif.setId(notifId);
        notif.setUser(testUser);
        notif.setRead(false);

        when(notificationRepository.findById(notifId)).thenReturn(Optional.of(notif));

        ApiException ex = assertThrows(ApiException.class, () -> notificationService.markAsRead(notifId, otherUser));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
        verify(notificationRepository, never()).save(any());
    }
}
