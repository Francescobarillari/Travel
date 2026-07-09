package it.unical.ea.Travel.Controllers.notification;

import it.unical.ea.Travel.Entities.user.User;
import it.unical.ea.Travel.Services.notification.NotificationService;
import it.unical.ea.Travel.Services.user.UserService;
import it.unical.ea.dtos.notification.NotificationDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import it.unical.ea.Travel.Exception.ApiException;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
@Tag(name = "Notification", description = "Gestione delle notifiche native")
public class NotificationController {

    private final NotificationService notificationService;
    private final UserService userService;

    @Operation(summary = "Ottieni le notifiche non lette dell'utente autenticato")
    @GetMapping("/unread")
    public List<NotificationDto> getUnreadNotifications(@AuthenticationPrincipal Jwt jwt) {
        User user = getAuthenticatedUser(jwt);
        return notificationService.getUnreadNotifications(user);
    }

    @Operation(summary = "Segna una notifica come letta")
    @PutMapping("/{id}/read")
    public void markAsRead(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        getAuthenticatedUser(jwt);
        notificationService.markAsRead(id);
    }

    @Operation(summary = "Registra uno stream SSE per ricevere notifiche in tempo reale")
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamNotifications(@AuthenticationPrincipal Jwt jwt) {
        User user = getAuthenticatedUser(jwt);
        return notificationService.registerEmitter(user.getId());
    }

    private User getAuthenticatedUser(Jwt jwt) {
        if (jwt == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "auth.login.invalidCredentials");
        }
        String email = jwt.getClaimAsString("email");
        return userService.getUserByEmail(email);
    }
}
