package it.unical.ea.Travel.Controllers.payment;

import it.unical.ea.Travel.Services.payment.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PayPalWebhookControllerTest {

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private PayPalWebhookController webhookController;

    @Test
    void testHandlePayPalWebhook_Success() {
        Map<String, String> headers = Map.of("paypal-auth-algo", "SHA256withRSA");
        String payload = "{\"event_type\":\"PAYMENT.CAPTURE.COMPLETED\"}";

        when(paymentService.handleWebhookEvent(headers, payload)).thenReturn(true);

        ResponseEntity<String> response = webhookController.handlePayPalWebhook(headers, payload);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("WEBHOOK_PROCESSED", response.getBody());
        verify(paymentService).handleWebhookEvent(headers, payload);
    }

    @Test
    void testHandlePayPalWebhook_Failure() {
        Map<String, String> headers = Map.of("paypal-auth-algo", "SHA256withRSA");
        String payload = "{\"event_type\":\"UNKNOWN_EVENT\"}";

        when(paymentService.handleWebhookEvent(headers, payload)).thenReturn(false);

        ResponseEntity<String> response = webhookController.handlePayPalWebhook(headers, payload);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("WEBHOOK_FAILED", response.getBody());
        verify(paymentService).handleWebhookEvent(headers, payload);
    }
}
