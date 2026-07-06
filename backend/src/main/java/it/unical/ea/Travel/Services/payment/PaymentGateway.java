package it.unical.ea.Travel.Services.payment;

import java.math.BigDecimal;

public interface PaymentGateway {
    /**
     * Creates a payment intent and returns the client secret required by the client SDK.
     */
    String createPaymentIntent(BigDecimal amount, String currency, String description);
}
