package it.unical.ea.Travel.Services.payment;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class StripePaymentGatewayImpl implements PaymentGateway {

    @Value("${stripe.api.key}")
    private String stripeApiKey;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeApiKey;
    }

    @Override
    public String createPaymentIntent(BigDecimal amount, String currency, String description) {
        // Stripe requires amount in the smallest currency unit (e.g. cents)
        long amountInCents = amount.multiply(new BigDecimal(100)).longValue();

        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amountInCents)
                .setCurrency(currency)
                .setDescription(description)
                // For demonstration, we could add metadata to store booking info,
                // but we will rely on the paymentIntentId saved in our DB instead.
                .build();

        try {
            PaymentIntent intent = PaymentIntent.create(params);
            return intent.getClientSecret();
        } catch (StripeException e) {
            throw new RuntimeException("Failed to create Stripe Payment Intent", e);
        }
    }
}
