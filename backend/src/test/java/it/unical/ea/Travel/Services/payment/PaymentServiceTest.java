package it.unical.ea.Travel.Services.payment;

import it.unical.ea.Travel.Entities.payment.BookingStatus;
import it.unical.ea.Travel.Repositories.activity.ActivityBookingRepository;
import it.unical.ea.Travel.Repositories.itinerary.ItineraryBookingRepository;
import it.unical.ea.Travel.Services.activity.ActivityService;
import it.unical.ea.Travel.Services.audit.AuditLogService;
import it.unical.ea.Travel.Services.itinerary.ItineraryService;
import it.unical.ea.dtos.payment.PaymentCaptureRequestDto;
import it.unical.ea.dtos.payment.PaymentVerificationResponseDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentGateway paymentGateway;
    @Mock
    private ItineraryBookingRepository itineraryBookingRepository;
    @Mock
    private ActivityBookingRepository activityBookingRepository;
    @Mock
    private ItineraryService itineraryService;
    @Mock
    private ActivityService activityService;
    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    void testCaptureAndVerifyPayment_Success() {
        PaymentCaptureRequestDto request = new PaymentCaptureRequestDto("ORDER123", null, "ITINERARY");

        PayPalOrderDetails details = PayPalOrderDetails.builder()
                .orderId("ORDER123")
                .status("APPROVED")
                .amount(null)
                .currency("EUR")
                .build();

        it.unical.ea.Travel.Entities.user.User user = new it.unical.ea.Travel.Entities.user.User();
        user.setEmail("user@test.com");

        it.unical.ea.Travel.Entities.itinerary.ItineraryBooking booking = new it.unical.ea.Travel.Entities.itinerary.ItineraryBooking();
        java.util.UUID bookingId = java.util.UUID.randomUUID();
        booking.setId(bookingId);
        booking.setUser(user);
        booking.setPaymentIntentId("ORDER123");

        when(paymentGateway.getOrderDetails("ORDER123")).thenReturn(details);
        when(paymentGateway.captureOrder("ORDER123")).thenReturn(true);
        when(itineraryBookingRepository.findByPaymentIntentId("ORDER123")).thenReturn(java.util.List.of(booking));

        PaymentVerificationResponseDto response = paymentService.captureAndVerifyPayment(request, "user@test.com");

        assertTrue(response.isSuccess());
        assertEquals("ORDER123", response.getOrderId());
        assertEquals(BookingStatus.CONFIRMED.name(), response.getBookingStatus());
        verify(paymentGateway).captureOrder("ORDER123");
        verify(itineraryService).confirmItineraryBooking(bookingId.toString());
    }

    @Test
    void testCaptureAndVerifyPayment_Failure() {
        PaymentCaptureRequestDto request = new PaymentCaptureRequestDto("ORDER123", "BOOKING123", "ACTIVITY");

        PayPalOrderDetails details = PayPalOrderDetails.builder()
                .orderId("ORDER123")
                .status("VOIDED")
                .amount(null)
                .currency("EUR")
                .build();

        when(paymentGateway.getOrderDetails("ORDER123")).thenReturn(details);
        when(itineraryBookingRepository.findByPaymentIntentId("ORDER123")).thenReturn(Collections.emptyList());
        when(activityBookingRepository.findByPaymentIntentId("ORDER123")).thenReturn(Collections.emptyList());

        PaymentVerificationResponseDto response = paymentService.captureAndVerifyPayment(request, "user@test.com");

        assertFalse(response.isSuccess());
        assertEquals(BookingStatus.FAILED.name(), response.getBookingStatus());
    }

    @Test
    void testCaptureAndVerifyPayment_PriceTampering_ThrowsException() {
        PaymentCaptureRequestDto request = new PaymentCaptureRequestDto("ORDER123", "BOOKING123", "ACTIVITY");

        PayPalOrderDetails details = PayPalOrderDetails.builder()
                .orderId("ORDER123")
                .status("APPROVED")
                .amount(new java.math.BigDecimal("0.01")) // Attacker paid 0.01 EUR
                .currency("EUR")
                .build();

        it.unical.ea.Travel.Entities.activity.Activity activity = new it.unical.ea.Travel.Entities.activity.Activity();
        activity.setPrice(new java.math.BigDecimal("100.00")); // Real price is 100 EUR

        it.unical.ea.Travel.Entities.activity.ActivityBooking booking = new it.unical.ea.Travel.Entities.activity.ActivityBooking();
        booking.setActivity(activity);
        booking.setPaymentIntentId("ORDER123");

        when(paymentGateway.getOrderDetails("ORDER123")).thenReturn(details);
        when(activityBookingRepository.findByPaymentIntentId("ORDER123")).thenReturn(java.util.List.of(booking));

        assertThrows(it.unical.ea.Travel.Exception.ApiException.class, () -> {
            paymentService.captureAndVerifyPayment(request, "user@test.com");
        });
    }

    @Test
    void testCaptureAndVerifyPayment_OwnershipMismatch_ThrowsForbidden() {
        PaymentCaptureRequestDto request = new PaymentCaptureRequestDto("ORDER123", null, "ACTIVITY");

        PayPalOrderDetails details = PayPalOrderDetails.builder()
                .orderId("ORDER123")
                .status("COMPLETED")
                .amount(null)
                .currency("EUR")
                .build();

        it.unical.ea.Travel.Entities.user.User legitimateOwner = new it.unical.ea.Travel.Entities.user.User();
        legitimateOwner.setEmail("legit@test.com");

        it.unical.ea.Travel.Entities.activity.ActivityBooking booking = new it.unical.ea.Travel.Entities.activity.ActivityBooking();
        booking.setUser(legitimateOwner);
        booking.setPaymentIntentId("ORDER123");

        when(paymentGateway.getOrderDetails("ORDER123")).thenReturn(details);
        when(activityBookingRepository.findByPaymentIntentId("ORDER123")).thenReturn(java.util.List.of(booking));

        assertThrows(it.unical.ea.Travel.Exception.ApiException.class, () -> {
            // Attacker attacker@test.com attempts to confirm legit@test.com's booking
            paymentService.captureAndVerifyPayment(request, "attacker@test.com");
        });
    }

    @Test
    void testHandleWebhookEvent_Approved_CapturesAndConfirms() {
        String payload = "{\"event_type\":\"CHECKOUT.ORDER.APPROVED\",\"resource\":{\"id\":\"ORDER123\"}}";
        Map<String, String> headers = Map.of("paypal-auth-algo", "SHA256withRSA");

        PayPalOrderDetails details = PayPalOrderDetails.builder()
                .orderId("ORDER123")
                .status("APPROVED")
                .amount(null)
                .currency("EUR")
                .build();

        it.unical.ea.Travel.Entities.user.User user = new it.unical.ea.Travel.Entities.user.User();
        user.setEmail("user@test.com");

        it.unical.ea.Travel.Entities.itinerary.ItineraryBooking booking = new it.unical.ea.Travel.Entities.itinerary.ItineraryBooking();
        java.util.UUID bookingId = java.util.UUID.randomUUID();
        booking.setId(bookingId);
        booking.setUser(user);
        booking.setPaymentIntentId("ORDER123");

        when(paymentGateway.verifyWebhookSignature(anyMap(), eq(payload))).thenReturn(true);
        when(paymentGateway.getOrderDetails("ORDER123")).thenReturn(details);
        when(paymentGateway.captureOrder("ORDER123")).thenReturn(true);
        when(itineraryBookingRepository.findByPaymentIntentId("ORDER123")).thenReturn(java.util.List.of(booking));

        boolean result = paymentService.handleWebhookEvent(headers, payload);

        assertTrue(result);
        verify(paymentGateway).captureOrder("ORDER123");
        verify(itineraryService).confirmItineraryBooking(bookingId.toString());
    }

    @Test
    void testCaptureAndVerifyPayment_CurrencyMismatch_ThrowsException() {
        PaymentCaptureRequestDto request = new PaymentCaptureRequestDto("ORDER123", "BOOKING123", "ACTIVITY");

        PayPalOrderDetails details = PayPalOrderDetails.builder()
                .orderId("ORDER123")
                .status("APPROVED")
                .amount(new java.math.BigDecimal("100.00"))
                .currency("USD") // Currency is USD instead of EUR
                .build();

        it.unical.ea.Travel.Entities.activity.Activity activity = new it.unical.ea.Travel.Entities.activity.Activity();
        activity.setPrice(new java.math.BigDecimal("100.00"));

        it.unical.ea.Travel.Entities.activity.ActivityBooking booking = new it.unical.ea.Travel.Entities.activity.ActivityBooking();
        booking.setActivity(activity);
        booking.setPaymentIntentId("ORDER123");

        when(paymentGateway.getOrderDetails("ORDER123")).thenReturn(details);
        when(activityBookingRepository.findByPaymentIntentId("ORDER123")).thenReturn(java.util.List.of(booking));

        it.unical.ea.Travel.Exception.ApiException ex = assertThrows(it.unical.ea.Travel.Exception.ApiException.class, () -> {
            paymentService.captureAndVerifyPayment(request, "user@test.com");
        });
        assertEquals(org.springframework.http.HttpStatus.BAD_REQUEST, ex.getStatus());
        assertEquals("payment.currencyMismatch", ex.getMessageKey());
    }

    @Test
    void testCaptureAndVerifyPayment_InvalidOrderId_ThrowsBadRequest() {
        PaymentCaptureRequestDto request = new PaymentCaptureRequestDto("ORDER!@#$%^&*()", null, "ACTIVITY");

        it.unical.ea.Travel.Exception.ApiException ex = assertThrows(it.unical.ea.Travel.Exception.ApiException.class, () -> {
            paymentService.captureAndVerifyPayment(request, "user@test.com");
        });
        assertEquals(org.springframework.http.HttpStatus.BAD_REQUEST, ex.getStatus());
        assertEquals("payment.invalidOrderId", ex.getMessageKey());
    }

    @Test
    void testCaptureAndVerifyPayment_UnauthenticatedUser_ThrowsUnauthorized() {
        PaymentCaptureRequestDto request = new PaymentCaptureRequestDto("ORDER123", null, "ACTIVITY");

        assertThrows(it.unical.ea.Travel.Exception.UnauthorizedAccessException.class, () -> {
            paymentService.captureAndVerifyPayment(request, "anonymousUser");
        });

        assertThrows(it.unical.ea.Travel.Exception.UnauthorizedAccessException.class, () -> {
            paymentService.captureAndVerifyPayment(request, null);
        });
    }

    @Test
    void testCaptureAndVerifyPayment_FallbackBookingId_Success() {
        java.util.UUID bookingUuid = java.util.UUID.randomUUID();
        PaymentCaptureRequestDto request = new PaymentCaptureRequestDto("ORDER123", bookingUuid.toString(), "ACTIVITY");

        PayPalOrderDetails details = PayPalOrderDetails.builder()
                .orderId("ORDER123")
                .status("APPROVED")
                .amount(null)
                .currency("EUR")
                .build();

        it.unical.ea.Travel.Entities.user.User user = new it.unical.ea.Travel.Entities.user.User();
        user.setEmail("user@test.com");

        it.unical.ea.Travel.Entities.activity.ActivityBooking booking = new it.unical.ea.Travel.Entities.activity.ActivityBooking();
        booking.setId(bookingUuid);
        booking.setUser(user);

        when(paymentGateway.getOrderDetails("ORDER123")).thenReturn(details);
        when(paymentGateway.captureOrder("ORDER123")).thenReturn(true);
        when(itineraryBookingRepository.findByPaymentIntentId("ORDER123")).thenReturn(Collections.emptyList());
        when(activityBookingRepository.findByPaymentIntentId("ORDER123")).thenReturn(Collections.emptyList());
        when(activityBookingRepository.findById(bookingUuid)).thenReturn(java.util.Optional.of(booking));

        PaymentVerificationResponseDto response = paymentService.captureAndVerifyPayment(request, "user@test.com");

        assertTrue(response.isSuccess());
        assertEquals(bookingUuid.toString(), response.getBookingId());
        assertEquals(BookingStatus.CONFIRMED.name(), response.getBookingStatus());
        assertEquals("ORDER123", booking.getPaymentIntentId());
        verify(activityBookingRepository).save(booking);
        verify(activityService).confirmActivityBooking(bookingUuid.toString());
    }

    @Test
    void testCaptureAndVerifyPayment_FallbackBookingId_Mismatch_ThrowsBadRequest() {
        java.util.UUID bookingUuid = java.util.UUID.randomUUID();
        PaymentCaptureRequestDto request = new PaymentCaptureRequestDto("ORDER123", bookingUuid.toString(), "ACTIVITY");

        PayPalOrderDetails details = PayPalOrderDetails.builder()
                .orderId("ORDER123")
                .status("COMPLETED")
                .amount(null)
                .currency("EUR")
                .build();

        it.unical.ea.Travel.Entities.user.User user = new it.unical.ea.Travel.Entities.user.User();
        user.setEmail("user@test.com");

        it.unical.ea.Travel.Entities.activity.ActivityBooking booking = new it.unical.ea.Travel.Entities.activity.ActivityBooking();
        booking.setId(bookingUuid);
        booking.setUser(user);
        booking.setPaymentIntentId("DIFFERENT_EXISTING_ORDER");

        when(paymentGateway.getOrderDetails("ORDER123")).thenReturn(details);
        when(itineraryBookingRepository.findByPaymentIntentId("ORDER123")).thenReturn(Collections.emptyList());
        when(activityBookingRepository.findByPaymentIntentId("ORDER123")).thenReturn(Collections.emptyList());
        when(activityBookingRepository.findById(bookingUuid)).thenReturn(java.util.Optional.of(booking));

        it.unical.ea.Travel.Exception.ApiException ex = assertThrows(it.unical.ea.Travel.Exception.ApiException.class, () -> {
            paymentService.captureAndVerifyPayment(request, "user@test.com");
        });
        assertEquals(org.springframework.http.HttpStatus.BAD_REQUEST, ex.getStatus());
        assertEquals("payment.orderMismatch", ex.getMessageKey());
    }

    @Test
    void testHandleWebhookEvent_Denied_MarksAsFailed() {
        String payload = "{\"event_type\":\"PAYMENT.CAPTURE.DENIED\",\"resource\":{\"id\":\"ORDER123\"}}";
        Map<String, String> headers = Map.of("paypal-auth-algo", "SHA256withRSA");

        it.unical.ea.Travel.Entities.activity.ActivityBooking booking = new it.unical.ea.Travel.Entities.activity.ActivityBooking();
        booking.setPaymentIntentId("ORDER123");

        when(paymentGateway.verifyWebhookSignature(anyMap(), eq(payload))).thenReturn(true);
        when(activityBookingRepository.findByPaymentIntentId("ORDER123")).thenReturn(java.util.List.of(booking));
        when(itineraryBookingRepository.findByPaymentIntentId("ORDER123")).thenReturn(Collections.emptyList());

        boolean result = paymentService.handleWebhookEvent(headers, payload);

        assertTrue(result);
        assertEquals(BookingStatus.FAILED, booking.getStatus());
        verify(activityBookingRepository).save(booking);
    }

    @Test
    void testHandleWebhookEvent_InvalidSignature_ThrowsBadRequest() {
        String payload = "{\"event_type\":\"CHECKOUT.ORDER.APPROVED\",\"resource\":{\"id\":\"ORDER123\"}}";
        Map<String, String> headers = Map.of("paypal-auth-algo", "invalid");

        when(paymentGateway.verifyWebhookSignature(anyMap(), eq(payload))).thenReturn(false);

        it.unical.ea.Travel.Exception.ApiException ex = assertThrows(it.unical.ea.Travel.Exception.ApiException.class, () -> {
            paymentService.handleWebhookEvent(headers, payload);
        });
        assertEquals(org.springframework.http.HttpStatus.BAD_REQUEST, ex.getStatus());
        assertEquals("payment.invalidWebhookSignature", ex.getMessageKey());
    }

    @Test
    void testHandleWebhookEvent_NoOrderId_ReturnsTrueAcknowledge() {
        String payload = "{\"event_type\":\"CUSTOMER.DISPUTE.CREATED\",\"resource\":{}}";
        Map<String, String> headers = Map.of("paypal-auth-algo", "SHA256withRSA");

        when(paymentGateway.verifyWebhookSignature(anyMap(), eq(payload))).thenReturn(true);

        boolean result = paymentService.handleWebhookEvent(headers, payload);

        assertTrue(result);
    }
}
