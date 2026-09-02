package it.unical.ea.Travel.Services.payment;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PayPalPaymentGatewayImplTest {

    @Test
    void testVerifyWebhookSignature_MissingWebhookId_SkipVerifyFalse_ReturnsFalse() {
        PayPalPaymentGatewayImpl gateway = new PayPalPaymentGatewayImpl(
                "client_id", "client_secret", null, false, "sandbox"
        );

        Map<String, String> headers = Map.of("paypal-auth-algo", "SHA256withRSA");
        String payload = "{\"event_type\":\"PAYMENT.CAPTURE.COMPLETED\"}";

        boolean verified = gateway.verifyWebhookSignature(headers, payload);
        assertFalse(verified, "Should reject webhook when webhookId is missing and skipVerify is false");
    }

    @Test
    void testVerifyWebhookSignature_MissingWebhookId_LiveMode_ReturnsFalse() {
        PayPalPaymentGatewayImpl gateway = new PayPalPaymentGatewayImpl(
                "client_id", "client_secret", "", true, "live"
        );

        Map<String, String> headers = Map.of("paypal-auth-algo", "SHA256withRSA");
        String payload = "{\"event_type\":\"PAYMENT.CAPTURE.COMPLETED\"}";

        boolean verified = gateway.verifyWebhookSignature(headers, payload);
        assertFalse(verified, "Should always reject unconfigured webhook in live mode");
    }

    @Test
    void testVerifyWebhookSignature_MissingWebhookId_SkipVerifyTrue_Sandbox_ReturnsTrue() {
        PayPalPaymentGatewayImpl gateway = new PayPalPaymentGatewayImpl(
                "client_id", "client_secret", "", true, "sandbox"
        );

        Map<String, String> headers = Map.of("paypal-auth-algo", "SHA256withRSA");
        String payload = "{\"event_type\":\"PAYMENT.CAPTURE.COMPLETED\"}";

        boolean verified = gateway.verifyWebhookSignature(headers, payload);
        assertTrue(verified, "Should allow webhook bypass only when explicitly configured with skipVerify=true in sandbox");
    }

    @Test
    void testVerifyWebhookSignature_WithWebhookId_SuccessFromPayPal() {
        PayPalPaymentGatewayImpl gateway = spy(new PayPalPaymentGatewayImpl(
                "client_id", "client_secret", "WH-12345", false, "sandbox"
        ));

        doReturn("mock-access-token").when(gateway).getAccessToken();

        RestTemplate mockRestTemplate = mock(RestTemplate.class);
        Map<String, Object> responseBody = Map.of("verification_status", "SUCCESS");
        ResponseEntity<Map> responseEntity = new ResponseEntity<>(responseBody, HttpStatus.OK);

        when(mockRestTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(responseEntity);

        ReflectionTestUtils.setField(gateway, "restTemplate", mockRestTemplate);

        Map<String, String> headers = Map.of(
                "paypal-auth-algo", "SHA256withRSA",
                "paypal-cert-url", "https://api.paypal.com/cert.pem",
                "paypal-transmission-id", "trans-123",
                "paypal-transmission-sig", "sig-123",
                "paypal-transmission-time", "2026-09-02T10:00:00Z"
        );
        String payload = "{\"id\":\"WH-EVT-1\",\"event_type\":\"CHECKOUT.ORDER.APPROVED\"}";

        boolean verified = gateway.verifyWebhookSignature(headers, payload);
        assertTrue(verified);
    }

    @Test
    void testVerifyWebhookSignature_WithWebhookId_FailureFromPayPal() {
        PayPalPaymentGatewayImpl gateway = spy(new PayPalPaymentGatewayImpl(
                "client_id", "client_secret", "WH-12345", false, "sandbox"
        ));

        doReturn("mock-access-token").when(gateway).getAccessToken();

        RestTemplate mockRestTemplate = mock(RestTemplate.class);
        Map<String, Object> responseBody = Map.of("verification_status", "FAILURE");
        ResponseEntity<Map> responseEntity = new ResponseEntity<>(responseBody, HttpStatus.OK);

        when(mockRestTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(responseEntity);

        ReflectionTestUtils.setField(gateway, "restTemplate", mockRestTemplate);

        Map<String, String> headers = Map.of("paypal-transmission-id", "trans-123");
        String payload = "{\"id\":\"WH-EVT-1\",\"event_type\":\"CHECKOUT.ORDER.APPROVED\"}";

        boolean verified = gateway.verifyWebhookSignature(headers, payload);
        assertFalse(verified);
    }
}
