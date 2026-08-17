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
        PaymentCaptureRequestDto request = new PaymentCaptureRequestDto("ORDER123", "BOOKING123", "ITINERARY");

        when(paymentGateway.getOrderStatus("ORDER123")).thenReturn("APPROVED");
        when(paymentGateway.captureOrder("ORDER123")).thenReturn(true);
        when(itineraryBookingRepository.findByPaymentIntentId("ORDER123")).thenReturn(Collections.emptyList());
        when(activityBookingRepository.findByPaymentIntentId("ORDER123")).thenReturn(Collections.emptyList());

        PaymentVerificationResponseDto response = paymentService.captureAndVerifyPayment(request, "user@test.com");

        assertTrue(response.isSuccess());
        assertEquals("ORDER123", response.getOrderId());
        assertEquals(BookingStatus.CONFIRMED.name(), response.getBookingStatus());
        verify(paymentGateway).captureOrder("ORDER123");
        verify(itineraryService).confirmItineraryBooking("BOOKING123");
    }

    @Test
    void testCaptureAndVerifyPayment_Failure() {
        PaymentCaptureRequestDto request = new PaymentCaptureRequestDto("ORDER123", "BOOKING123", "ACTIVITY");

        when(paymentGateway.getOrderStatus("ORDER123")).thenReturn("VOIDED");
        when(itineraryBookingRepository.findByPaymentIntentId("ORDER123")).thenReturn(Collections.emptyList());
        when(activityBookingRepository.findByPaymentIntentId("ORDER123")).thenReturn(Collections.emptyList());

        PaymentVerificationResponseDto response = paymentService.captureAndVerifyPayment(request, "user@test.com");

        assertFalse(response.isSuccess());
        assertEquals(BookingStatus.FAILED.name(), response.getBookingStatus());
    }
}
