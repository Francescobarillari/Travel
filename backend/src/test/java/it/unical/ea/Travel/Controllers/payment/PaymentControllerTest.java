package it.unical.ea.Travel.Controllers.payment;

import it.unical.ea.Travel.Config.SecurityUtils;
import it.unical.ea.Travel.Exception.UnauthorizedAccessException;
import it.unical.ea.Travel.Services.payment.PaymentService;
import it.unical.ea.dtos.payment.PaymentCaptureRequestDto;
import it.unical.ea.dtos.payment.PaymentVerificationResponseDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentControllerTest {

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private PaymentController paymentController;

    @Test
    void testCaptureAndVerifyPayment_Authenticated_Success() {
        PaymentCaptureRequestDto request = new PaymentCaptureRequestDto("ORDER123", "BOOKING123", "ACTIVITY");
        PaymentVerificationResponseDto expectedResponse = PaymentVerificationResponseDto.builder()
                .success(true)
                .orderId("ORDER123")
                .bookingId("BOOKING123")
                .bookingStatus("CONFIRMED")
                .build();

        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getCurrentUserEmail).thenReturn("user@example.com");
            when(paymentService.captureAndVerifyPayment(request, "user@example.com")).thenReturn(expectedResponse);

            ResponseEntity<PaymentVerificationResponseDto> response = paymentController.captureAndVerifyPayment(request);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertTrue(response.getBody().isSuccess());
            assertEquals("ORDER123", response.getBody().getOrderId());
            verify(paymentService).captureAndVerifyPayment(request, "user@example.com");
        }
    }

    @Test
    void testCaptureAndVerifyPayment_Unauthenticated_ThrowsUnauthorized() {
        PaymentCaptureRequestDto request = new PaymentCaptureRequestDto("ORDER123", "BOOKING123", "ACTIVITY");

        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getCurrentUserEmail).thenReturn(null);

            assertThrows(UnauthorizedAccessException.class, () -> {
                paymentController.captureAndVerifyPayment(request);
            });

            mockedSecurityUtils.when(SecurityUtils::getCurrentUserEmail).thenReturn("anonymousUser");

            assertThrows(UnauthorizedAccessException.class, () -> {
                paymentController.captureAndVerifyPayment(request);
            });
        }
    }
}
