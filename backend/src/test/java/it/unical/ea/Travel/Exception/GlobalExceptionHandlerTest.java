package it.unical.ea.Travel.Exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;

import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Mock
    private MessageSource messageSource;

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler(messageSource);
    }

    @Test
    void testHandleApiException() {
        ApiException ex = new ResourceNotFoundException("activity.notFound");
        when(messageSource.getMessage(eq("activity.notFound"), any(), eq("activity.notFound"), any(Locale.class)))
                .thenReturn("Attività non trovata!");

        ResponseEntity<Map<String, String>> response = exceptionHandler.handleApiException(ex, Locale.ITALIAN);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Attività non trovata!", response.getBody().get("error"));
    }

    @Test
    void testHandleBadCredentialsException() {
        BadCredentialsException ex = new BadCredentialsException("Bad credentials");
        when(messageSource.getMessage(eq("auth.login.invalidCredentials"), any(), anyString(), any(Locale.class)))
                .thenReturn("Credenziali non valide.");

        ResponseEntity<Map<String, String>> response = exceptionHandler.handleBadCredentialsException(ex, Locale.ITALIAN);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Credenziali non valide.", response.getBody().get("error"));
    }

    @Test
    void testHandleAccessDeniedException() {
        AccessDeniedException ex = new AccessDeniedException("Access denied");
        when(messageSource.getMessage(eq("error.unauthorized"), any(), anyString(), any(Locale.class)))
                .thenReturn("Non autorizzato.");

        ResponseEntity<Map<String, String>> response = exceptionHandler.handleAccessDeniedException(ex, Locale.ITALIAN);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Non autorizzato.", response.getBody().get("error"));
    }

    @Test
    void testHandleIllegalArgumentException_InvalidUUID() {
        IllegalArgumentException ex = new IllegalArgumentException("Invalid UUID string: abc");

        ResponseEntity<Map<String, String>> response = exceptionHandler.handleIllegalArgumentException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Formato UUID non valido.", response.getBody().get("error"));
    }

    @Test
    void testHandleDataIntegrityViolationException_DuplicateEmail() {
        org.springframework.dao.DataIntegrityViolationException ex =
                new org.springframework.dao.DataIntegrityViolationException("Conflict", new RuntimeException("ERROR: duplicate key value violates unique constraint \"idx_user_email\""));

        when(messageSource.getMessage(eq("auth.signup.emailAlreadyExists"), any(), anyString(), any(Locale.class)))
                .thenReturn("Esiste già un account con questa email.");

        ResponseEntity<Map<String, String>> response = exceptionHandler.handleDataIntegrityViolationException(ex, Locale.ITALIAN);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Esiste già un account con questa email.", response.getBody().get("error"));
    }

    @Test
    void testHandleDataIntegrityViolationException_GenericConflict() {
        org.springframework.dao.DataIntegrityViolationException ex =
                new org.springframework.dao.DataIntegrityViolationException("General DB error", new RuntimeException("foreign key constraint violated"));

        when(messageSource.getMessage(eq("error.conflict"), any(), anyString(), any(Locale.class)))
                .thenReturn("Conflitto nei dati inviati.");

        ResponseEntity<Map<String, String>> response = exceptionHandler.handleDataIntegrityViolationException(ex, Locale.ITALIAN);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Conflitto nei dati inviati.", response.getBody().get("error"));
    }

    @Test
    void testHandleIllegalStateException() {
        IllegalStateException ex = new IllegalStateException("Invalid state");
        when(messageSource.getMessage(eq("error.invalidState"), any(), anyString(), any(Locale.class)))
                .thenReturn("Operazione non consentita nello stato corrente.");

        ResponseEntity<Map<String, String>> response = exceptionHandler.handleIllegalStateException(ex, Locale.ITALIAN);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Operazione non consentita nello stato corrente.", response.getBody().get("error"));
    }

    @Test
    void testHandleOptimisticLockException() {
        org.springframework.orm.ObjectOptimisticLockingFailureException ex =
                new org.springframework.orm.ObjectOptimisticLockingFailureException("Entity", "123");
        when(messageSource.getMessage(eq("error.optimisticLock"), any(), any(Locale.class)))
                .thenReturn("L'evento o l'itinerario è stato aggiornato da un altro utente.");

        ResponseEntity<Map<String, String>> response = exceptionHandler.handleOptimisticLockException(ex, Locale.ITALIAN);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("L'evento o l'itinerario è stato aggiornato da un altro utente.", response.getBody().get("error"));
    }

    @Test
    void testHandleMaxUploadSizeExceededException() {
        org.springframework.web.multipart.MaxUploadSizeExceededException ex =
                new org.springframework.web.multipart.MaxUploadSizeExceededException(5242880);
        when(messageSource.getMessage(eq("file.maxSizeExceeded"), any(), anyString(), any(Locale.class)))
                .thenReturn("Il file caricato supera la dimensione massima consentita.");

        ResponseEntity<Map<String, String>> response = exceptionHandler.handleMaxUploadSizeExceededException(ex, Locale.ITALIAN);

        assertEquals(HttpStatus.PAYLOAD_TOO_LARGE, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Il file caricato supera la dimensione massima consentita.", response.getBody().get("error"));
    }

    @Test
    void testHandleGenericException() {
        Exception ex = new NullPointerException("Something null");
        when(messageSource.getMessage(eq("error.internalServerError"), any(), anyString(), any(Locale.class)))
                .thenReturn("Errore interno del server.");

        ResponseEntity<Map<String, String>> response = exceptionHandler.handleGenericException(ex, Locale.ITALIAN);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Errore interno del server.", response.getBody().get("error"));
    }
}
