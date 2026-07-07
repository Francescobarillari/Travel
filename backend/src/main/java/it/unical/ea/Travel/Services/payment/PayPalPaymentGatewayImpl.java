package it.unical.ea.Travel.Services.payment;

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
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@ConditionalOnProperty(name = "payment.mock", havingValue = "false", matchIfMissing = false)
public class PayPalPaymentGatewayImpl implements PaymentGateway {

    private final String clientId;
    private final String clientSecret;
    private final String baseUrl;
    private final RestTemplate restTemplate;

    public PayPalPaymentGatewayImpl(
            @Value("${paypal.client.id:}") String clientId,
            @Value("${paypal.client.secret:}") String clientSecret,
            @Value("${paypal.mode:sandbox}") String mode) {
        
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.restTemplate = new RestTemplate();
        
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

        // Build the PayPal Order request JSON structure using Maps
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
                return orderId; // We return Order ID. Frontend will use it to open PayPal Checkout.
            }
        } catch (Exception e) {
            log.error("Error creating PayPal Order: {}", e.getMessage());
            throw new RuntimeException("Failed to create PayPal Order", e);
        }

        throw new RuntimeException("Failed to create PayPal Order - Unknown error");
    }
}
