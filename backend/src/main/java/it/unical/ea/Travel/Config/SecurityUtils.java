package it.unical.ea.Travel.Config;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

public class SecurityUtils {

    private SecurityUtils() {
        // Prevent instantiation
    }

    public static String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof Jwt jwt) {
                String email = jwt.getClaimAsString("email");
                if (email != null) {
                    return email;
                }
                String username = jwt.getClaimAsString("preferred_username");
                if (username != null) {
                    return username;
                }
                return jwt.getSubject();
            } else if (principal instanceof String str) {
                return str;
            }
        }
        return "SYSTEM";
    }
}
