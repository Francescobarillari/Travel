package it.unical.ea.Travel.Controllers.auth;

import it.unical.ea.dtos.authDto.LoginRequest;
import it.unical.ea.dtos.authDto.SignupRequest;
import it.unical.ea.Travel.Services.AuthService;
import it.unical.ea.Travel.Services.keycloak.KeycloakUserAlreadyExistsException;
import it.unical.ea.Travel.Exception.ApiException;
import it.unical.ea.Travel.Services.user.UserService;
import it.unical.ea.Travel.Services.user.LoginAttemptService;
import it.unical.ea.Travel.Services.keycloak.CaptchaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.context.MessageSource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "Autenticazione e registrazione utenti")
public class AuthController {

    private final UserService userService;
    private final AuthService authService;
    private final MessageSource messageSource;
    private final LoginAttemptService loginAttemptService;
    private final CaptchaService captchaService;

    @Operation(summary = "Login utente", description = "Autentica l'utente e restituisce un token JWT")
    @PostMapping("/login")
    public ResponseEntity<String> login(@Valid @RequestBody LoginRequest request) {
        String email = request.getEmail();
        
        // Verifica se è necessario il CAPTCHA a causa di troppi tentativi falliti
        if (loginAttemptService.isCaptchaRequired(email)) {
            if (request.getCaptchaToken() == null || !captchaService.verifyToken(request.getCaptchaToken())) {
                throw new ApiException(HttpStatus.FORBIDDEN, "auth.login.invalidCredentials");
            }
        }

        try {
            String token = authService.login(request);
            // Autenticazione riuscita: azzera i tentativi falliti
            loginAttemptService.loginSucceeded(email);
            return ResponseEntity.ok(token);
        } catch (BadCredentialsException e) {
            // Incrementa i tentativi falliti in caso di credenziali errate
            loginAttemptService.loginFailed(email);
            throw new ApiException(HttpStatus.UNAUTHORIZED, "auth.login.invalidCredentials");
        } catch (Exception e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "auth.login.error");
        }
    }

    @Operation(summary = "Registrazione utente", description = "Registra un nuovo utente (Viaggiatore o Società) e lo crea anche su Keycloak")
    @PostMapping("/signup")
    public ResponseEntity<String> signup(@Valid @RequestBody SignupRequest request) {
        // La registrazione richiede sempre un CAPTCHA valido
        if (request.getCaptchaToken() == null || !captchaService.verifyToken(request.getCaptchaToken())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "auth.signup.captchaInvalid");
        }

        try {
            userService.saveUser(request);
            return ResponseEntity.ok()
                    .body(messageSource.getMessage("auth.signup.success", null, LocaleContextHolder.getLocale()));
        } catch (DataIntegrityViolationException e) {
            if (!isDuplicateEmailError(e)) {
                throw e; // Rilancia l'eccezione database generica
            }
            throw new ApiException(HttpStatus.CONFLICT, "auth.signup.emailAlreadyExists");
        } catch (KeycloakUserAlreadyExistsException e) {
            throw new ApiException(HttpStatus.CONFLICT, "auth.signup.emailAlreadyExists");
        }
    }

    private boolean isDuplicateEmailError(DataIntegrityViolationException exception) {
        String errorMessage = exception.getMostSpecificCause().getMessage();
        return errorMessage != null
                && (errorMessage.contains("idx_user_email") || errorMessage.contains("Email gia registrata"));
    }
}
