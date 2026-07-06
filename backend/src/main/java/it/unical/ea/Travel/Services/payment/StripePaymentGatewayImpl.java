package it.unical.ea.Travel.Services.payment;

import com.stripe.StripeClient;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
@ConditionalOnProperty(name = "stripe.mock", havingValue = "false")
public class StripePaymentGatewayImpl implements PaymentGateway {

    private final StripeClient stripeClient;

    public StripePaymentGatewayImpl(@Value("${stripe.api.key}") String stripeApiKey) {
        this.stripeClient = new StripeClient(stripeApiKey);
        // Log partial key to verify it's loaded correctly at startup
        if (stripeApiKey != null && stripeApiKey.length() > 10) {
            log.info("Stripe initialized with key: {}...{}", stripeApiKey.substring(0, 10), stripeApiKey.substring(stripeApiKey.length() - 4));
        } else {
            log.warn("Stripe API key is missing or too short!");
        }
    }

    @Override
    public String createPaymentIntent(BigDecimal amount, String currency, String description) {
        // Stripe requires amount in the smallest currency unit (e.g. cents)
        long amountInCents = amount.multiply(new BigDecimal(100)).longValue();

        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amountInCents)
                .setCurrency(currency)
                .setDescription(description)
                .addPaymentMethodType("card")
                .build();

        try {
            PaymentIntent intent = stripeClient.paymentIntents().create(params);
            return intent.getClientSecret();
        } catch (StripeException e) {
            log.error("Stripe error: {} (code: {})", e.getMessage(), e.getCode());
            throw new RuntimeException("Failed to create Stripe Payment Intent", e);
        }
    }
}
