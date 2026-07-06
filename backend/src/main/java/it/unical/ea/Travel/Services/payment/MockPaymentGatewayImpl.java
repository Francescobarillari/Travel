package it.unical.ea.Travel.Services.payment;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;

@Service
@ConditionalOnProperty(name = "stripe.mock", havingValue = "true", matchIfMissing = true)
public class MockPaymentGatewayImpl implements PaymentGateway {
    @Override
    public String createPaymentIntent(BigDecimal amount, String currency, String description) {
        return null;
    }
}
