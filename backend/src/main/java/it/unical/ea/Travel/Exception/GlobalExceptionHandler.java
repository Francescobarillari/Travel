package it.unical.ea.Travel.Exception;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Locale;
import java.util.Map;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final MessageSource messageSource;

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Map<String, String>> handleApiException(ApiException ex, Locale locale) {
        // Recupera il messaggio localizzato usando il locale corrente della richiesta
        String translatedMessage = messageSource.getMessage(ex.getMessageKey(), null, locale);
        
        // Restituisce un JSON del tipo {"error": "messaggio tradotto"} e il corretto status HTTP
        return new ResponseEntity<>(Map.of("error", translatedMessage), ex.getStatus());
    }
    
}

