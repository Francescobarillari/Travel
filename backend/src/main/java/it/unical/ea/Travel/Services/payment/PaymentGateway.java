package it.unical.ea.Travel.Services.payment;

import java.math.BigDecimal;
import java.util.Map;

public interface PaymentGateway {
    /**
     * Creates a payment intent/order and returns the client secret (order ID) required by the client SDK.
     */
    String createPaymentIntent(BigDecimal amount, String currency, String description);

    /**
     * Captures funds for an approved PayPal order server-to-server.
     */
    boolean captureOrder(String orderId);

    /**
     * Fetches the current status of an order from PayPal REST API.
     */
    String getOrderStatus(String orderId);

    /**
     * Verifies the authenticity of a PayPal Webhook signature.
     */
    boolean verifyWebhookSignature(Map<String, String> headers, String body);
}

