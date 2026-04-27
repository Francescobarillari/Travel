package it.unical.ea.Travel.Controllers;

import it.unical.ea.Travel.Entities.User;
import it.unical.ea.Travel.Services.JwtService;
import it.unical.ea.Travel.Services.UserService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody AuthRequest request) {
        try {
            // Se fallisce, questa riga lancia un'eccezione e salta direttamente al "catch"
            Authentication authenticate = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            // Se arriviamo qui, l'autenticazione ha avuto successo!
            String token = jwtService.generateToken(request.getEmail());
            return ResponseEntity.ok(token);

        } catch (BadCredentialsException e) {
            // Se le credenziali sono sbagliate (o l'utente non esiste), entriamo qui
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Errore: Credenziali non valide o utente non trovato!");
        } catch (Exception e) {
            // Per qualsiasi altro errore strano
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Errore del server: " + e.getMessage());
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody User user) {
        userService.registerNewUser(user);
        return ResponseEntity.ok("Registrazione avvenuta con successo!");
    }

    //mini DTO per request del login
    @Setter
    @Getter
    public static class AuthRequest {
        private String email;
        private String password;
    }
}