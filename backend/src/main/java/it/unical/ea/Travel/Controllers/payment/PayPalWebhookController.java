package it.unical.ea.Travel.Controllers.payment;

import it.unical.ea.Travel.Services.payment.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/payments/paypal")
@RequiredArgsConstructor
public class PayPalWebhookController {

    private final PaymentService paymentService;

    @PostMapping("/webhook")
    public ResponseEntity<String> handlePayPalWebhook(
            @RequestHeader Map<String, String> headers,
            @RequestBody String payload) {
        log.info("Received PayPal Webhook HTTP Request");
        boolean result = paymentService.handleWebhookEvent(headers, payload);
        if (result) {
            return ResponseEntity.ok("WEBHOOK_PROCESSED");
        } else {
            return ResponseEntity.badRequest().body("WEBHOOK_FAILED");
        }
    }
}
