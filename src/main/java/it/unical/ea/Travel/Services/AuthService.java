package it.unical.ea.Travel.Services;

import it.unical.ea.Travel.DTOs.authDto.LoginRequest;
import it.unical.ea.Travel.Services.keycloak.KeycloakAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Servizio che gestisce la logica di business relativa all'autenticazione.
 * Le credenziali vengono verificate da Keycloak, che emette il JWT usato dal frontend.
 */
@RequiredArgsConstructor
@Service
public class AuthService {

    private final KeycloakAuthService keycloakAuthService;

    public String login(LoginRequest request) {
        return keycloakAuthService.login(request);
    }
}
