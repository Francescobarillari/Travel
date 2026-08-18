package it.unical.ea.Travel.Controllers.payment;

import it.unical.ea.Travel.Config.SecurityUtils;
import it.unical.ea.Travel.Services.payment.PaymentService;
import it.unical.ea.dtos.payment.PaymentCaptureRequestDto;
import it.unical.ea.dtos.payment.PaymentVerificationResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/paypal/capture")
    public ResponseEntity<PaymentVerificationResponseDto> captureAndVerifyPayment(
            @RequestBody PaymentCaptureRequestDto request) {
        String userEmail = SecurityUtils.getCurrentUserEmail();
        if (userEmail == null || "SYSTEM".equalsIgnoreCase(userEmail) || "anonymousUser".equalsIgnoreCase(userEmail)) {
            throw new it.unical.ea.Travel.Exception.UnauthorizedAccessException("auth.unauthorized");
        }
        PaymentVerificationResponseDto response = paymentService.captureAndVerifyPayment(request, userEmail);
        return ResponseEntity.ok(response);
    }
}
