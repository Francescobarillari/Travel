package it.unical.ea.Travel.Exception;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final MessageSource messageSource;

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Map<String, String>> handleApiException(ApiException ex, Locale locale) {
        String translatedMessage = messageSource.getMessage(ex.getMessageKey(), null, ex.getMessageKey(), locale);
        return new ResponseEntity<>(Map.of("error", translatedMessage), ex.getStatus());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(
            MethodArgumentNotValidException ex, Locale locale) {

        Map<String, String> fieldErrors = new HashMap<>();

        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        Map<String, Object> response = new HashMap<>();
        response.put("errors", fieldErrors);

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException ex) {
        String message = ex.getMessage();
        if (message != null && message.contains("Invalid UUID string")) {
            return new ResponseEntity<>(Map.of("error", "Formato UUID non valido."), HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(Map.of("error", message != null ? message : "Richiesta non valida."), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleIllegalStateException(IllegalStateException ex, Locale locale) {
        log.warn("IllegalStateException captured: {}", ex.getMessage());
        String translatedMessage = messageSource.getMessage("error.invalidState", null, "Operazione non consentita nello stato corrente.", locale);
        return new ResponseEntity<>(Map.of("error", translatedMessage), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, String>> handleAccessDeniedException(AccessDeniedException ex, Locale locale) {
        String translatedMessage = messageSource.getMessage("error.unauthorized", null, "Non sei autorizzato ad eseguire questa operazione.", locale);
        return new ResponseEntity<>(Map.of("error", translatedMessage), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(org.springframework.orm.ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<Map<String, String>> handleOptimisticLockException(
            org.springframework.orm.ObjectOptimisticLockingFailureException ex, Locale locale) {
        String translatedMessage = messageSource.getMessage("error.optimisticLock", null, locale);
        return new ResponseEntity<>(Map.of("error", translatedMessage), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(org.springframework.web.multipart.MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, String>> handleMaxUploadSizeExceededException(
            org.springframework.web.multipart.MaxUploadSizeExceededException ex, Locale locale) {
        String translatedMessage = messageSource.getMessage("file.maxSizeExceeded", null, "Il file caricato supera la dimensione massima consentita.", locale);
        return new ResponseEntity<>(Map.of("error", translatedMessage), HttpStatus.PAYLOAD_TOO_LARGE);
    }

    @ExceptionHandler(org.springframework.security.authentication.BadCredentialsException.class)
    public ResponseEntity<Map<String, String>> handleBadCredentialsException(
            org.springframework.security.authentication.BadCredentialsException ex, Locale locale) {
        log.warn("BadCredentialsException captured: {}", ex.getMessage());
        String translatedMessage = messageSource.getMessage("auth.login.invalidCredentials", null, "Credenziali non valide.", locale);
        return new ResponseEntity<>(Map.of("error", translatedMessage), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> handleDataIntegrityViolationException(
            org.springframework.dao.DataIntegrityViolationException ex, Locale locale) {
        log.warn("DataIntegrityViolationException captured: {}", ex.getMessage());
        String message = ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : "";
        if (message != null && (message.contains("idx_user_email") || message.contains("Email gia registrata") || message.contains("duplicate key"))) {
            String translatedMessage = messageSource.getMessage("auth.signup.emailAlreadyExists", null, "Esiste già un account con questa email.", locale);
            return new ResponseEntity<>(Map.of("error", translatedMessage), HttpStatus.CONFLICT);
        }
        String translatedMessage = messageSource.getMessage("error.conflict", null, "Conflitto nei dati inviati.", locale);
        return new ResponseEntity<>(Map.of("error", translatedMessage), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(jakarta.validation.ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolationException(
            jakarta.validation.ConstraintViolationException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getConstraintViolations().forEach(violation -> {
            String property = violation.getPropertyPath().toString();
            errors.put(property, violation.getMessage());
        });
        return new ResponseEntity<>(Map.of("errors", errors), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception ex, Locale locale) {
        log.error("Unhandled exception captured by GlobalExceptionHandler: {}", ex.getMessage(), ex);
        String translatedMessage = messageSource.getMessage("error.internalServerError", null, "Si è verificato un errore interno del server.", locale);
        return new ResponseEntity<>(Map.of("error", translatedMessage), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
