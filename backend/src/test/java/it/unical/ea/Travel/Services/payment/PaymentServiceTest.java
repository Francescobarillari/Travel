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
        when(itineraryBookingRepository.findByPaymentIntentId("ORDER123")).thenReturn(Collections.emptyList());
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
        when(itineraryBookingRepository.findByPaymentIntentId("ORDER123")).thenReturn(Collections.emptyList());
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
    void testHandleWebhookEvent_PriceTampering_ThrowsException() {
        String payload = "{\"event_type\":\"PAYMENT.CAPTURE.COMPLETED\",\"resource\":{\"supplementary_data\":{\"related_ids\":{\"order_id\":\"ORDER123\"}}}}";
        Map<String, String> headers = Map.of("paypal-auth-algo", "SHA256withRSA");

        PayPalOrderDetails details = PayPalOrderDetails.builder()
                .orderId("ORDER123")
                .status("COMPLETED")
                .amount(new java.math.BigDecimal("1.00")) // Attacker paid 1.00 EUR
                .currency("EUR")
                .build();

        it.unical.ea.Travel.Entities.activity.Activity activity = new it.unical.ea.Travel.Entities.activity.Activity();
        activity.setPrice(new java.math.BigDecimal("150.00")); // Real price is 150 EUR

        it.unical.ea.Travel.Entities.activity.ActivityBooking booking = new it.unical.ea.Travel.Entities.activity.ActivityBooking();
        booking.setActivity(activity);
        booking.setPaymentIntentId("ORDER123");

        when(paymentGateway.verifyWebhookSignature(anyMap(), eq(payload))).thenReturn(true);
        when(paymentGateway.getOrderDetails("ORDER123")).thenReturn(details);
        when(itineraryBookingRepository.findByPaymentIntentId("ORDER123")).thenReturn(Collections.emptyList());
        when(activityBookingRepository.findByPaymentIntentId("ORDER123")).thenReturn(java.util.List.of(booking));

        assertThrows(it.unical.ea.Travel.Exception.ApiException.class, () -> {
            paymentService.handleWebhookEvent(headers, payload);
        });
    }
}
