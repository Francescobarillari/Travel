package it.unical.ea.Travel.Services.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@ConditionalOnProperty(name = "payment.mock", havingValue = "false", matchIfMissing = false)
public class PayPalPaymentGatewayImpl implements PaymentGateway {

    private final String clientId;
    private final String clientSecret;
    private final String webhookId;
    private final String baseUrl;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public PayPalPaymentGatewayImpl(
            @Value("${paypal.client.id:}") String clientId,
            @Value("${paypal.client.secret:}") String clientSecret,
            @Value("${paypal.webhook.id:}") String webhookId,
            @Value("${paypal.mode:sandbox}") String mode) {
        
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.webhookId = webhookId;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
        
        if ("live".equalsIgnoreCase(mode)) {
            this.baseUrl = "https://api-m.paypal.com";
        } else {
            this.baseUrl = "https://api-m.sandbox.paypal.com";
        }

        if (clientId != null && !clientId.isEmpty() && !"YOUR_SANDBOX_CLIENT_ID".equals(clientId)) {
            log.info("PayPal initialized in {} mode with Client ID: {}...", mode, clientId.substring(0, Math.min(clientId.length(), 8)));
        } else {
            log.warn("PayPal Client ID is missing or using placeholder! Payments might fail.");
        }
    }

    private String getAccessToken() {
        String url = baseUrl + "/v1/oauth2/token";

        HttpHeaders headers = new HttpHeaders();
        String auth = clientId + ":" + clientSecret;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
        headers.setBasicAuth(encodedAuth);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return (String) response.getBody().get("access_token");
            }
        } catch (Exception e) {
            log.error("Error fetching PayPal access token: {}", e.getMessage());
            throw new RuntimeException("Failed to authenticate with PayPal", e);
        }
        throw new RuntimeException("Failed to fetch PayPal access token");
    }

    @Override
    public String createPaymentIntent(BigDecimal amount, String currency, String description) {
        String token = getAccessToken();
        String url = baseUrl + "/v2/checkout/orders";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> amountMap = Map.of(
                "currency_code", currency.toUpperCase(),
                "value", amount.setScale(2, RoundingMode.HALF_UP).toString()
        );

        Map<String, Object> purchaseUnit = Map.of(
                "reference_id", "booking_" + System.currentTimeMillis(),
                "description", description,
                "amount", amountMap
        );

        Map<String, Object> requestBody = Map.of(
                "intent", "CAPTURE",
                "purchase_units", List.of(purchaseUnit)
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            if ((response.getStatusCode() == HttpStatus.CREATED || response.getStatusCode() == HttpStatus.OK) 
                && response.getBody() != null) {
                
                String orderId = (String) response.getBody().get("id");
                log.info("Created PayPal Order ID: {}", orderId);
                return orderId;
            }
        } catch (Exception e) {
            log.error("Error creating PayPal Order: {}", e.getMessage());
            throw new RuntimeException("Failed to create PayPal Order", e);
        }

        throw new RuntimeException("Failed to create PayPal Order - Unknown error");
    }

    @Override
    public boolean captureOrder(String orderId) {
        String token = getAccessToken();
        String url = baseUrl + "/v2/checkout/orders/" + orderId + "/capture";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(Map.of(), headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            if ((response.getStatusCode() == HttpStatus.CREATED || response.getStatusCode() == HttpStatus.OK)
                    && response.getBody() != null) {
                String status = (String) response.getBody().get("status");
                log.info("Captured PayPal Order ID: {} with status: {}", orderId, status);
                return "COMPLETED".equalsIgnoreCase(status);
            }
        } catch (Exception e) {
            log.error("Error capturing PayPal Order ID {}: {}", orderId, e.getMessage());
        }
        return false;
    }

    @Override
    public String getOrderStatus(String orderId) {
        String token = getAccessToken();
        String url = baseUrl + "/v2/checkout/orders/" + orderId;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, request, Map.class);
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return (String) response.getBody().get("status");
            }
        } catch (Exception e) {
            log.error("Error getting PayPal Order status for ID {}: {}", orderId, e.getMessage());
        }
        return "UNKNOWN";
    }

    @Override
    public boolean verifyWebhookSignature(Map<String, String> headers, String body) {
        if (webhookId == null || webhookId.isBlank()) {
            log.warn("PAYPAL_WEBHOOK_ID is not configured. Skipping webhook signature verification for development.");
            return true;
        }

        try {
            String token = getAccessToken();
            String url = baseUrl + "/v1/notifications/verify-webhook-signature";

            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setBearerAuth(token);
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);

            Map<String, String> normalizedHeaders = new HashMap<>();
            headers.forEach((k, v) -> normalizedHeaders.put(k.toLowerCase(), v));

            Object webhookEventJson = objectMapper.readValue(body, Object.class);

            Map<String, Object> requestBody = Map.of(
                    "auth_algo", normalizedHeaders.getOrDefault("paypal-auth-algo", ""),
                    "cert_url", normalizedHeaders.getOrDefault("paypal-cert-url", ""),
                    "transmission_id", normalizedHeaders.getOrDefault("paypal-transmission-id", ""),
                    "transmission_sig", normalizedHeaders.getOrDefault("paypal-transmission-sig", ""),
                    "transmission_time", normalizedHeaders.getOrDefault("paypal-transmission-time", ""),
                    "webhook_id", webhookId,
                    "webhook_event", webhookEventJson
            );

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, httpHeaders);
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                String verificationStatus = (String) response.getBody().get("verification_status");
                return "SUCCESS".equalsIgnoreCase(verificationStatus);
            }
        } catch (Exception e) {
            log.error("Error verifying PayPal webhook signature: {}", e.getMessage());
        }
        return false;
    }
}

