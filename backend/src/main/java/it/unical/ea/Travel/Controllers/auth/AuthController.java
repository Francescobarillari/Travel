package it.unical.ea.Travel.Controllers.auth;

import it.unical.ea.Travel.DTOs.authDto.LoginRequest;
import it.unical.ea.Travel.DTOs.authDto.SignupRequest;
import it.unical.ea.Travel.Services.AuthService;
import it.unical.ea.Travel.Services.keycloak.KeycloakUserAlreadyExistsException;
import it.unical.ea.Travel.Services.user.UserService;
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
public class AuthController {

    private final UserService userService;
    private final AuthService authService;
    private final MessageSource messageSource;

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest request) {
        try {
            String token = authService.login(request);
            return ResponseEntity.ok(token);
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(messageSource.getMessage("auth.login.invalidCredentials", null, LocaleContextHolder.getLocale()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(messageSource.getMessage("auth.login.error", null, LocaleContextHolder.getLocale()));
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@Valid @RequestBody SignupRequest request) {
        try {
            userService.saveUser(request);
            return ResponseEntity.ok().body(messageSource.getMessage("auth.signup.success", null, LocaleContextHolder.getLocale()));
        } catch (DataIntegrityViolationException e) {
            if (!isDuplicateEmailError(e)) {
                throw e;
            }
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(messageSource.getMessage("auth.signup.emailAlreadyExists", null,
                            LocaleContextHolder.getLocale()));
        } catch (KeycloakUserAlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(messageSource.getMessage("auth.signup.emailAlreadyExists", null,
                            LocaleContextHolder.getLocale()));
        }
    }

    private boolean isDuplicateEmailError(DataIntegrityViolationException exception) {
        String errorMessage = exception.getMostSpecificCause().getMessage();
        return errorMessage != null
                && (errorMessage.contains("idx_user_email") || errorMessage.contains("Email gia registrata"));
    }
}
