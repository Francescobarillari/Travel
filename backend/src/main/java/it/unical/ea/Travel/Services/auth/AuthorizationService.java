package it.unical.ea.Travel.Services.auth;

import it.unical.ea.Travel.Config.SecurityUtils;
import it.unical.ea.Travel.Entities.user.User;
import it.unical.ea.Travel.Exception.ApiException;
import it.unical.ea.Travel.Repositories.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthorizationService {

    private final UserRepository userRepository;

    /**
     * Recupera l'utente correntemente autenticato dal SecurityContext.
     */
    public User getCurrentUser() {
        String email = SecurityUtils.getCurrentUserEmail();
        return userRepository.getUserByEmail(email)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "user.notFound"));
    }

    /**
     * Verifica se l'utente corrente è proprietario della risorsa o possiede il ruolo ADMIN.
     */
    public boolean isOwnerOrAdmin(UUID resourceOwnerId) {
        if (resourceOwnerId == null) {
            return false;
        }
        User currentUser = getCurrentUser();
        if (currentUser.getId() != null && currentUser.getId().equals(resourceOwnerId)) {
            return true;
        }

        // Verifica ruolo admin tramite SecurityContext Authorities o campo roles nel DB
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getAuthorities() != null) {
            boolean hasAdminAuthority = authentication.getAuthorities().stream()
                    .anyMatch(a -> "ROLE_ADMIN".equalsIgnoreCase(a.getAuthority()) || "ADMIN".equalsIgnoreCase(a.getAuthority()));
            if (hasAdminAuthority) {
                return true;
            }
        }

        if (currentUser.getRoles() != null && (currentUser.getRoles().contains("ADMIN") || currentUser.getRoles().contains("ROLE_ADMIN"))) {
            return true;
        }

        return false;
    }

    /**
     * Helper per lanciare ApiException(HttpStatus.FORBIDDEN) se l'utente non è proprietario né admin.
     */
    public void verifyOwnershipOrAdmin(UUID resourceOwnerId, String resourceType) {
        if (!isOwnerOrAdmin(resourceOwnerId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, resourceType + ".forbidden");
        }
    }
}
