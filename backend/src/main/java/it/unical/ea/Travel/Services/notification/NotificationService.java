package it.unical.ea.Travel.Services.notification;

import it.unical.ea.Travel.Entities.notification.Notification;
import it.unical.ea.Travel.Entities.user.User;
import it.unical.ea.Travel.Mappers.notification.NotificationMapper;
import it.unical.ea.Travel.Repositories.notification.NotificationRepository;
import it.unical.ea.dtos.notification.NotificationDto;
import it.unical.ea.enums.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;

    // Connessioni real-time attive degli utenti (più tab o dispositivi possibili)
    private final Map<UUID, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

    @Transactional
    public NotificationDto createNotification(User user, String title, String message, NotificationType type) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notification.setRead(false);

        Notification saved = notificationRepository.save(notification);
        NotificationDto dto = notificationMapper.toDTO(saved);

        // Invia notifica in tempo reale agli emitter registrati dell'utente
        sendRealtimeNotification(user.getId(), dto);

        return dto;
    }

    @Transactional(readOnly = true)
    public List<NotificationDto> getUnreadNotifications(User user) {
        List<Notification> unread = notificationRepository.findByUserAndIsReadOrderByCreatedAtDesc(user, false);
        return notificationMapper.toDTOList(unread);
    }

    @Transactional
    public void markAsRead(UUID notificationId) {
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            notification.setRead(true);
            notificationRepository.save(notification);
        });
    }

    public SseEmitter registerEmitter(UUID userId) {
        SseEmitter emitter = new SseEmitter(10 * 60 * 1000L); // 10 minuti di timeout per evitare leak

        emitters.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> removeEmitter(userId, emitter));
        emitter.onTimeout(() -> removeEmitter(userId, emitter));
        emitter.onError((e) -> removeEmitter(userId, emitter));

        // Invia un heartbeat iniziale per convalidare la connessione
        try {
            emitter.send(SseEmitter.event().name("INIT").data("Connected"));
        } catch (IOException e) {
            removeEmitter(userId, emitter);
        }

        return emitter;
    }

    private void removeEmitter(UUID userId, SseEmitter emitter) {
        List<SseEmitter> userEmitters = emitters.get(userId);
        if (userEmitters != null) {
            userEmitters.remove(emitter);
            if (userEmitters.isEmpty()) {
                emitters.remove(userId);
            }
        }
    }

    private void sendRealtimeNotification(UUID userId, NotificationDto dto) {
        List<SseEmitter> userEmitters = emitters.get(userId);
        if (userEmitters != null) {
            List<SseEmitter> deadEmitters = new ArrayList<>();
            for (SseEmitter emitter : userEmitters) {
                try {
                    emitter.send(SseEmitter.event().name("NOTIFICATION").data(dto));
                } catch (IOException e) {
                    deadEmitters.add(emitter);
                }
            }
            // Rimuove gli emitter non più validi
            for (SseEmitter dead : deadEmitters) {
                removeEmitter(userId, dead);
            }
        }
    }
}
