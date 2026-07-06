package it.unical.ea.Travel.Exception;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final MessageSource messageSource;

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Map<String, String>> handleApiException(ApiException ex, Locale locale) {
        // Recupera il messaggio localizzato usando il locale corrente della richiesta, con fallback sulla chiave stessa
        String translatedMessage = messageSource.getMessage(ex.getMessageKey(), null, ex.getMessageKey(), locale);
        
        // Restituisce un JSON del tipo {"error": "messaggio tradotto"} e il corretto status HTTP
        return new ResponseEntity<>(Map.of("error", translatedMessage), ex.getStatus());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(
            MethodArgumentNotValidException ex, Locale locale) {

        Map<String, String> fieldErrors = new HashMap<>();

        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            // Il messaggio è già risolto dal MessageSource grazie all'uso delle chiavi {message.key}
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
}


