package it.unical.ea.Travel.Services;

import it.unical.ea.Travel.DTOs.authDto.LoginRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

/**
 * Servizio che gestisce la logica di business relativa all'autenticazione.
 * Incapsula il processo di verifica delle credenziali tramite AuthenticationManager
 * e la successiva generazione del token JWT per l'utente autenticato.
 */
@RequiredArgsConstructor
@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public String login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        return jwtService.generateToken(request.getEmail());
    }
}
