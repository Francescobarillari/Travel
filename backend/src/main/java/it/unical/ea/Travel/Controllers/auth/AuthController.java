package it.unical.ea.Travel.Controllers.auth;

import it.unical.ea.dtos.authDto.LoginRequest;
import it.unical.ea.dtos.authDto.SignupRequest;
import it.unical.ea.dtos.authDto.JwtResponse;
import it.unical.ea.dtos.authDto.ForgotPasswordRequest;
import it.unical.ea.dtos.authDto.ResetPasswordRequest;
import it.unical.ea.dtos.user.UserDTO;
import it.unical.ea.Travel.Services.AuthService;
import it.unical.ea.Travel.Services.keycloak.KeycloakUserAlreadyExistsException;
import it.unical.ea.Travel.Exception.ApiException;
import it.unical.ea.Travel.Services.user.UserService;
import it.unical.ea.Travel.Entities.user.User;
import it.unical.ea.Travel.Services.user.LoginAttemptService;
import it.unical.ea.Travel.Services.keycloak.CaptchaService;
import it.unical.ea.Travel.Services.auth.OtpService;
import it.unical.ea.Travel.Services.mail.EmailService;
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
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;
import it.unical.ea.Travel.Services.storage.FileStorageService;

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
    private final FileStorageService fileStorageService;
    private final OtpService otpService;
    private final EmailService emailService;

    @Operation(summary = "Login utente", description = "Autentica l'utente e restituisce un token JWT")
    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@Valid @RequestBody LoginRequest request) {
        String email = request.getEmail();
        
        // Verifica se è necessario il CAPTCHA a causa di troppi tentativi falliti
        if (loginAttemptService.isCaptchaRequired(email)) {
            if (request.getCaptchaToken() == null || !captchaService.verifyToken(request.getCaptchaToken())) {
                throw new ApiException(HttpStatus.FORBIDDEN, "auth.login.invalidCredentials");
            }
        }

        try {
            JwtResponse tokenResponse = authService.login(request);

            // Verifica approvazione per profili Società
            try {
                User user = userService.getUserByEmail(email);
                if (user.getUserType() == it.unical.ea.enums.UserType.SOCIETA && Boolean.TRUE.equals(user.getBlocked())) {
                    throw new ApiException(HttpStatus.FORBIDDEN, "auth.login.accountBlocked");
                }
                if (user.getUserType() == it.unical.ea.enums.UserType.SOCIETA && !Boolean.TRUE.equals(user.getApproved())) {
                    throw new ApiException(HttpStatus.FORBIDDEN, "auth.login.accountPendingApproval");
                }
            } catch (ApiException apiEx) {
                if (apiEx.getStatus() == HttpStatus.FORBIDDEN) {
                    throw apiEx;
                }
            } catch (Exception ignored) {
                // Utente non presente nel DB locale (es. admin-user), procedi comunque
            }

            // Autenticazione riuscita: azzera i tentativi falliti
            loginAttemptService.loginSucceeded(email);
            return ResponseEntity.ok(tokenResponse);
        } catch (ApiException e) {
            throw e;
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

    @Operation(summary = "Carica un documento di registrazione per la società", description = "Accetta un file immagine (JPEG, PNG, WebP)")
    @PostMapping(value = "/upload-document", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadDocument(@RequestPart("file") MultipartFile file) {
        String filePath = fileStorageService.store(file, "companies/documents");
        return ResponseEntity.ok(filePath);
    }

    private boolean isDuplicateEmailError(DataIntegrityViolationException exception) {
        String errorMessage = exception.getMostSpecificCause().getMessage();
        return errorMessage != null
                && (errorMessage.contains("idx_user_email") || errorMessage.contains("Email gia registrata"));
    }

    @Operation(summary = "Richiesta recupero password", description = "Genera un codice OTP e lo invia via email se l'utente esiste")
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        String email = request.getEmail();
        
        try {
            // Verifica che l'utente esista nel database locale
            User user = userService.getUserByEmail(email);
            
            // Genera codice OTP
            String otp = otpService.generateOtp(email);
            
            // Invia email con codice OTP
            emailService.sendOtpEmail(email, otp);
        } catch (ApiException e) {
            if (e.getStatus() == HttpStatus.NOT_FOUND) {
                // Per motivi di sicurezza/privacy non riveliamo se la mail non esiste, restituiamo comunque OK
                return ResponseEntity.ok(messageSource.getMessage("auth.forgot-password.otpSent", null, LocaleContextHolder.getLocale()));
            }
            throw e;
        } catch (Exception e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "error.internalServerError");
        }
        
        return ResponseEntity.ok(messageSource.getMessage("auth.forgot-password.otpSent", null, LocaleContextHolder.getLocale()));
    }

    @Operation(summary = "Reimpostazione password con OTP", description = "Verifica il codice OTP e reimposta la password in Keycloak")
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        String email = request.getEmail();
        String otp = request.getOtp();
        String newPassword = request.getNewPassword();

        // 1. Verifica che l'utente esista nel database
        try {
            userService.getUserByEmail(email);
        } catch (ApiException e) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "auth.forgot-password.invalidOtp");
        }

        // 2. Valida il codice OTP
        boolean isValid = otpService.validateOtp(email, otp);
        if (!isValid) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "auth.forgot-password.invalidOtp");
        }

        // 3. Valida la complessità della nuova password (min 8 caratteri, una maiuscola, una minuscola, un numero)
        if (newPassword.length() < 8 || !newPassword.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$")) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "validation.password.pattern");
        }

        try {
            // 4. Aggiorna la password in Keycloak
            UserDTO userDto = new UserDTO();
            userDto.setPassword(newPassword);
            userService.updateUser(email, userDto);
            return ResponseEntity.ok(messageSource.getMessage("auth.forgot-password.success", null, LocaleContextHolder.getLocale()));
        } catch (Exception e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "error.internalServerError");
        }
    }
}
