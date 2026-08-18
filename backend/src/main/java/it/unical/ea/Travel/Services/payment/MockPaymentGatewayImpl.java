package it.unical.ea.Travel.Services.payment;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Service
@ConditionalOnProperty(name = "payment.mock", havingValue = "true", matchIfMissing = true)
public class MockPaymentGatewayImpl implements PaymentGateway {
    @Override
    public String createPaymentIntent(BigDecimal amount, String currency, String description) {
        return "mock_order_" + UUID.randomUUID().toString();
    }

    @Override
    public boolean captureOrder(String orderId) {
        return true;
    }

    @Override
    public String getOrderStatus(String orderId) {
        return "COMPLETED";
    }

    @Override
    public PayPalOrderDetails getOrderDetails(String orderId) {
        return PayPalOrderDetails.builder()
                .orderId(orderId)
                .status("COMPLETED")
                .amount(null) // Mock will match any requested amount if null or dummy
                .currency("EUR")
                .build();
    }

    @Override
    public boolean verifyWebhookSignature(Map<String, String> headers, String body) {
        return true;
    }
}

