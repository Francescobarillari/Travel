package it.unical.ea.Travel.Services.payment;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.unical.ea.Travel.Entities.activity.ActivityBooking;
import it.unical.ea.Travel.Entities.itinerary.ItineraryBooking;
import it.unical.ea.Travel.Entities.payment.BookingStatus;
import it.unical.ea.Travel.Exception.ApiException;
import it.unical.ea.Travel.Repositories.activity.ActivityBookingRepository;
import it.unical.ea.Travel.Repositories.itinerary.ItineraryBookingRepository;
import it.unical.ea.Travel.Services.activity.ActivityService;
import it.unical.ea.Travel.Services.audit.AuditLogService;
import it.unical.ea.Travel.Services.itinerary.ItineraryService;
import it.unical.ea.dtos.payment.PaymentCaptureRequestDto;
import it.unical.ea.dtos.payment.PaymentVerificationResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentGateway paymentGateway;
    private final ItineraryBookingRepository itineraryBookingRepository;
    private final ActivityBookingRepository activityBookingRepository;
    private final ItineraryService itineraryService;
    private final ActivityService activityService;
    private final AuditLogService auditLogService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final java.util.regex.Pattern ORDER_ID_PATTERN = java.util.regex.Pattern.compile("^[a-zA-Z0-9_-]{1,64}$");

    @Transactional
    public PaymentVerificationResponseDto captureAndVerifyPayment(PaymentCaptureRequestDto request, String userEmail) {
        if (request == null || request.getOrderId() == null || !ORDER_ID_PATTERN.matcher(request.getOrderId().trim()).matches()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "payment.invalidOrderId");
        }
        if (userEmail == null || "SYSTEM".equalsIgnoreCase(userEmail) || "anonymousUser".equalsIgnoreCase(userEmail)) {
            throw new it.unical.ea.Travel.Exception.UnauthorizedAccessException("auth.unauthorized");
        }

        String orderId = request.getOrderId().trim();
        String bookingType = request.getBookingType(); // "ITINERARY" or "ACTIVITY"

        log.info("Initiating server-to-server payment capture & verification for orderId: {}, bookingType: {}", orderId, bookingType);

        // 1. Fetch full details (status, amount, currency) from PayPal
        PayPalOrderDetails orderDetails = paymentGateway.getOrderDetails(orderId);
        String status = orderDetails.getStatus() != null ? orderDetails.getStatus() : "UNKNOWN";
        log.info("PayPal Order status for orderId {}: {}", orderId, status);

        // 2. Validate amount and currency to prevent price tampering
        BigDecimal expectedAmount = getExpectedPriceForOrder(orderId, request.getBookingId(), bookingType);
        if (expectedAmount != null && orderDetails.getAmount() != null) {
            BigDecimal expectedScaled = expectedAmount.setScale(2, java.math.RoundingMode.HALF_UP);
            BigDecimal paidScaled = orderDetails.getAmount().setScale(2, java.math.RoundingMode.HALF_UP);
            if (expectedScaled.compareTo(paidScaled) != 0) {
                log.error("SECURITY ALERT: Payment amount mismatch for orderId {}. Expected: {}, Paid: {}",
                        orderId, expectedScaled, paidScaled);
                markBookingAsFailed(orderId, request.getBookingId(), bookingType);
                auditLogService.log("PAYMENT_AMOUNT_MISMATCH", "PayPalOrder", orderId,
                        "Price tampering detected! Expected: " + expectedScaled + ", Paid: " + paidScaled);
                throw new ApiException(HttpStatus.BAD_REQUEST, "payment.amountMismatch");
            }
        }
        if (orderDetails.getCurrency() != null && !orderDetails.getCurrency().equalsIgnoreCase("EUR")) {
            log.error("SECURITY ALERT: Payment currency mismatch for orderId {}. Currency: {}",
                    orderId, orderDetails.getCurrency());
            markBookingAsFailed(orderId, request.getBookingId(), bookingType);
            throw new ApiException(HttpStatus.BAD_REQUEST, "payment.currencyMismatch");
        }

        boolean isCompleted = "COMPLETED".equalsIgnoreCase(status);

        // If status is APPROVED or CREATED, perform server-to-server capture
        if (!isCompleted && ("APPROVED".equalsIgnoreCase(status) || "CREATED".equalsIgnoreCase(status))) {
            boolean captured = paymentGateway.captureOrder(orderId);
            if (captured) {
                isCompleted = true;
                status = "COMPLETED";
            } else {
                status = paymentGateway.getOrderStatus(orderId);
                isCompleted = "COMPLETED".equalsIgnoreCase(status);
            }
        }

        // If payment completed or mock gateway returned true
        if (isCompleted || "COMPLETED".equalsIgnoreCase(status)) {
            String bookingId = confirmBookingByOrder(orderId, request.getBookingId(), bookingType, userEmail, false);
            auditLogService.log("PAYMENT_VERIFIED", "PayPalOrder", orderId, "Payment verified and captured server-to-server for booking: " + bookingId);
            return PaymentVerificationResponseDto.builder()
                    .success(true)
                    .orderId(orderId)
                    .bookingId(bookingId)
                    .bookingStatus(BookingStatus.CONFIRMED.name())
                    .message("Payment captured and booking confirmed successfully")
                    .build();
        } else {
            markBookingAsFailed(orderId, request.getBookingId(), bookingType);
            auditLogService.log("PAYMENT_FAILED", "PayPalOrder", orderId, "Payment verification failed for orderId: " + orderId + ", status: " + status);
            return PaymentVerificationResponseDto.builder()
                    .success(false)
                    .orderId(orderId)
                    .bookingId(request.getBookingId())
                    .bookingStatus(BookingStatus.FAILED.name())
                    .message("Payment verification failed with PayPal status: " + status)
                    .build();
        }
    }

    private BigDecimal getExpectedPriceForOrder(String orderId, String fallbackBookingId, String bookingType) {
        if ("ACTIVITY".equalsIgnoreCase(bookingType)) {
            List<ActivityBooking> activityBookings = activityBookingRepository.findByPaymentIntentId(orderId);
            if (!activityBookings.isEmpty()) {
                return calculateActivityPrice(activityBookings.get(0));
            }
            List<ItineraryBooking> itineraryBookings = itineraryBookingRepository.findByPaymentIntentId(orderId);
            if (!itineraryBookings.isEmpty()) {
                return calculateItineraryPrice(itineraryBookings.get(0));
            }
        } else {
            List<ItineraryBooking> itineraryBookings = itineraryBookingRepository.findByPaymentIntentId(orderId);
            if (!itineraryBookings.isEmpty()) {
                return calculateItineraryPrice(itineraryBookings.get(0));
            }
            List<ActivityBooking> activityBookings = activityBookingRepository.findByPaymentIntentId(orderId);
            if (!activityBookings.isEmpty()) {
                return calculateActivityPrice(activityBookings.get(0));
            }
        }

        if (fallbackBookingId != null && !fallbackBookingId.isBlank()) {
            try {
                UUID uuid = UUID.fromString(fallbackBookingId);
                if ("ITINERARY".equalsIgnoreCase(bookingType)) {
                    var ib = itineraryBookingRepository.findById(uuid);
                    if (ib.isPresent()) return calculateItineraryPrice(ib.get());
                } else if ("ACTIVITY".equalsIgnoreCase(bookingType)) {
                    var ab = activityBookingRepository.findById(uuid);
                    if (ab.isPresent()) return calculateActivityPrice(ab.get());
                }
            } catch (Exception ignored) {}
        }
        return null;
    }

    private BigDecimal calculateItineraryPrice(ItineraryBooking booking) {
        BigDecimal total = BigDecimal.ZERO;
        if (booking.getItinerary() != null && booking.getItinerary().getActivities() != null) {
            for (it.unical.ea.Travel.Entities.activity.Activity a : booking.getItinerary().getActivities()) {
                if (a.getPrice() != null) {
                    total = total.add(a.getPrice());
                }
            }
        }
        return total;
    }

    private BigDecimal calculateActivityPrice(ActivityBooking booking) {
        if (booking.getActivity() != null && booking.getActivity().getPrice() != null) {
            return booking.getActivity().getPrice();
        }
        return BigDecimal.ZERO;
    }

    @Transactional
    public boolean handleWebhookEvent(Map<String, String> headers, String payload) {
        log.info("Processing PayPal Webhook event...");

        boolean isSignatureValid = paymentGateway.verifyWebhookSignature(headers, payload);
        if (!isSignatureValid) {
            log.error("PayPal Webhook signature verification failed!");
            throw new ApiException(HttpStatus.BAD_REQUEST, "payment.invalidWebhookSignature");
        }

        try {
            JsonNode root = objectMapper.readTree(payload);
            String eventType = root.path("event_type").asText();
            JsonNode resource = root.path("resource");

            log.info("Received PayPal Webhook event: {}", eventType);

            String orderId = extractOrderIdFromResource(resource);

            if (orderId == null || orderId.isBlank()) {
                log.warn("Webhook payload does not contain orderId in resource: {}", eventType);
                return true; // Acknowledge webhook
            }

            switch (eventType) {
                case "CHECKOUT.ORDER.APPROVED":
                    log.info("Webhook received CHECKOUT.ORDER.APPROVED for Order ID: {}. Validating and triggering server capture...", orderId);
                    PayPalOrderDetails approvedDetails = paymentGateway.getOrderDetails(orderId);
                    validateOrderPrice(orderId, approvedDetails);

                    boolean captured = paymentGateway.captureOrder(orderId);
                    if (captured) {
                        confirmBookingByOrder(orderId, null, null, "SYSTEM", true);
                        auditLogService.log("WEBHOOK_PAYMENT_CONFIRMED", "PayPalWebhook", orderId, "Booking captured and confirmed via Webhook event: " + eventType);
                    } else {
                        log.warn("Failed to capture order {} on APPROVED webhook event", orderId);
                    }
                    break;

                case "PAYMENT.CAPTURE.COMPLETED":
                    log.info("Webhook received PAYMENT.CAPTURE.COMPLETED for Order ID: {}. Validating and confirming...", orderId);
                    PayPalOrderDetails completedDetails = paymentGateway.getOrderDetails(orderId);
                    validateOrderPrice(orderId, completedDetails);

                    confirmBookingByOrder(orderId, null, null, "SYSTEM", true);
                    auditLogService.log("WEBHOOK_PAYMENT_CONFIRMED", "PayPalWebhook", orderId, "Booking confirmed via Webhook event: " + eventType);
                    break;

                case "PAYMENT.CAPTURE.DENIED":
                case "PAYMENT.CAPTURE.DECLINED":
                case "CHECKOUT.ORDER.VOIDED":
                    log.warn("Webhook marking payment failed for Order ID: {}", orderId);
                    markBookingAsFailed(orderId, null, null);
                    auditLogService.log("WEBHOOK_PAYMENT_FAILED", "PayPalWebhook", orderId, "Booking marked failed via Webhook event: " + eventType);
                    break;

                default:
                    log.info("Unhandled PayPal webhook event type: {}", eventType);
                    break;
            }
            return true;
        } catch (ApiException ex) {
            throw ex;
        } catch (Exception e) {
            log.error("Error processing PayPal webhook payload: {}", e.getMessage(), e);
            throw new ApiException(HttpStatus.BAD_REQUEST, "payment.webhookProcessingError");
        }
    }

    private void validateOrderPrice(String orderId, PayPalOrderDetails orderDetails) {
        BigDecimal expectedAmount = getExpectedPriceForOrder(orderId, null, null);
        if (expectedAmount != null && orderDetails != null && orderDetails.getAmount() != null) {
            BigDecimal expectedScaled = expectedAmount.setScale(2, java.math.RoundingMode.HALF_UP);
            BigDecimal paidScaled = orderDetails.getAmount().setScale(2, java.math.RoundingMode.HALF_UP);
            if (expectedScaled.compareTo(paidScaled) != 0) {
                log.error("SECURITY ALERT: Payment amount mismatch in webhook for orderId {}. Expected: {}, Paid: {}",
                        orderId, expectedScaled, paidScaled);
                markBookingAsFailed(orderId, null, null);
                auditLogService.log("WEBHOOK_AMOUNT_MISMATCH", "PayPalWebhook", orderId,
                        "Price tampering detected in Webhook! Expected: " + expectedScaled + ", Paid: " + paidScaled);
                throw new ApiException(HttpStatus.BAD_REQUEST, "payment.amountMismatch");
            }
        }
        if (orderDetails != null && orderDetails.getCurrency() != null && !orderDetails.getCurrency().equalsIgnoreCase("EUR")) {
            log.error("SECURITY ALERT: Payment currency mismatch in webhook for orderId {}. Currency: {}",
                    orderId, orderDetails.getCurrency());
            markBookingAsFailed(orderId, null, null);
            throw new ApiException(HttpStatus.BAD_REQUEST, "payment.currencyMismatch");
        }
    }

    private String confirmBookingByOrder(String orderId, String fallbackBookingId, String bookingType, String userEmail, boolean isSystemWebhook) {
        if ("ACTIVITY".equalsIgnoreCase(bookingType)) {
            // 1. Try finding ActivityBooking by orderId first
            List<ActivityBooking> activityBookings = activityBookingRepository.findByPaymentIntentId(orderId);
            if (!activityBookings.isEmpty()) {
                for (ActivityBooking ab : activityBookings) {
                    verifyBookingOwnership(ab.getUser(), userEmail, isSystemWebhook);
                    activityService.confirmActivityBooking(ab.getId().toString());
                }
                return activityBookings.get(0).getId().toString();
            }

            // 2. Try finding ItineraryBooking by orderId
            List<ItineraryBooking> itineraryBookings = itineraryBookingRepository.findByPaymentIntentId(orderId);
            if (!itineraryBookings.isEmpty()) {
                for (ItineraryBooking ib : itineraryBookings) {
                    verifyBookingOwnership(ib.getUser(), userEmail, isSystemWebhook);
                    itineraryService.confirmItineraryBooking(ib.getId().toString());
                }
                return itineraryBookings.get(0).getId().toString();
            }
        } else {
            // 1. Try finding ItineraryBooking by orderId first
            List<ItineraryBooking> itineraryBookings = itineraryBookingRepository.findByPaymentIntentId(orderId);
            if (!itineraryBookings.isEmpty()) {
                for (ItineraryBooking ib : itineraryBookings) {
                    verifyBookingOwnership(ib.getUser(), userEmail, isSystemWebhook);
                    itineraryService.confirmItineraryBooking(ib.getId().toString());
                }
                return itineraryBookings.get(0).getId().toString();
            }

            // 2. Try finding ActivityBooking by orderId
            List<ActivityBooking> activityBookings = activityBookingRepository.findByPaymentIntentId(orderId);
            if (!activityBookings.isEmpty()) {
                for (ActivityBooking ab : activityBookings) {
                    verifyBookingOwnership(ab.getUser(), userEmail, isSystemWebhook);
                    activityService.confirmActivityBooking(ab.getId().toString());
                }
                return activityBookings.get(0).getId().toString();
            }
        }

        // 3. Fallback using fallbackBookingId with strict validation to prevent replay/ID injection attacks
        if (fallbackBookingId != null && !fallbackBookingId.isBlank()) {
            try {
                UUID uuid = UUID.fromString(fallbackBookingId);
                if ("ITINERARY".equalsIgnoreCase(bookingType)) {
                    ItineraryBooking ib = itineraryBookingRepository.findById(uuid)
                            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "itinerary.booking.notFound"));
                    
                    verifyBookingOwnership(ib.getUser(), userEmail, isSystemWebhook);

                    // Prevent reusing orderId if already bound to another intent
                    if (ib.getPaymentIntentId() != null && !ib.getPaymentIntentId().isBlank() && !ib.getPaymentIntentId().equals(orderId)) {
                        log.error("SECURITY ALERT: Order ID mismatch for booking {}. Existing: {}, Provided: {}",
                                fallbackBookingId, ib.getPaymentIntentId(), orderId);
                        throw new ApiException(HttpStatus.BAD_REQUEST, "payment.orderMismatch");
                    }
                    ib.setPaymentIntentId(orderId);
                    itineraryBookingRepository.save(ib);
                    itineraryService.confirmItineraryBooking(ib.getId().toString());
                    return fallbackBookingId;
                } else if ("ACTIVITY".equalsIgnoreCase(bookingType)) {
                    ActivityBooking ab = activityBookingRepository.findById(uuid)
                            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "activity.booking.notFound"));

                    verifyBookingOwnership(ab.getUser(), userEmail, isSystemWebhook);

                    if (ab.getPaymentIntentId() != null && !ab.getPaymentIntentId().isBlank() && !ab.getPaymentIntentId().equals(orderId)) {
                        log.error("SECURITY ALERT: Order ID mismatch for booking {}. Existing: {}, Provided: {}",
                                fallbackBookingId, ab.getPaymentIntentId(), orderId);
                        throw new ApiException(HttpStatus.BAD_REQUEST, "payment.orderMismatch");
                    }
                    ab.setPaymentIntentId(orderId);
                    activityBookingRepository.save(ab);
                    activityService.confirmActivityBooking(ab.getId().toString());
                    return fallbackBookingId;
                }
            } catch (ApiException ex) {
                throw ex;
            } catch (Exception e) {
                log.error("Error confirming booking by fallback ID {}: {}", fallbackBookingId, e.getMessage());
            }
        }

        log.warn("No booking found matching paymentIntentId/orderId: {}", orderId);
        return fallbackBookingId != null ? fallbackBookingId : orderId;
    }

    private void verifyBookingOwnership(it.unical.ea.Travel.Entities.user.User bookingUser, String userEmail, boolean isSystemWebhook) {
        if (isSystemWebhook) {
            return; // Authorized for system/webhooks
        }
        if (userEmail == null || "SYSTEM".equalsIgnoreCase(userEmail) || "anonymousUser".equalsIgnoreCase(userEmail)) {
            throw new it.unical.ea.Travel.Exception.UnauthorizedAccessException("auth.unauthorized");
        }
        if (bookingUser != null && bookingUser.getEmail() != null) {
            if (!bookingUser.getEmail().equalsIgnoreCase(userEmail)) {
                log.error("SECURITY ALERT: User {} attempted to confirm booking owned by {}", userEmail, bookingUser.getEmail());
                throw new ApiException(HttpStatus.FORBIDDEN, "payment.forbiddenBookingOwnership");
            }
        }
    }

    private void markBookingAsFailed(String orderId, String fallbackBookingId, String bookingType) {
        List<ItineraryBooking> itineraryBookings = itineraryBookingRepository.findByPaymentIntentId(orderId);
        for (ItineraryBooking ib : itineraryBookings) {
            ib.setStatus(BookingStatus.FAILED);
            itineraryBookingRepository.save(ib);
        }

        List<ActivityBooking> activityBookings = activityBookingRepository.findByPaymentIntentId(orderId);
        for (ActivityBooking ab : activityBookings) {
            ab.setStatus(BookingStatus.FAILED);
            activityBookingRepository.save(ab);
        }

        if (fallbackBookingId != null && !fallbackBookingId.isBlank()) {
            try {
                UUID uuid = UUID.fromString(fallbackBookingId);
                if ("ITINERARY".equalsIgnoreCase(bookingType)) {
                    itineraryBookingRepository.findById(uuid).ifPresent(b -> {
                        b.setStatus(BookingStatus.FAILED);
                        itineraryBookingRepository.save(b);
                    });
                } else if ("ACTIVITY".equalsIgnoreCase(bookingType)) {
                    activityBookingRepository.findById(uuid).ifPresent(b -> {
                        b.setStatus(BookingStatus.FAILED);
                        activityBookingRepository.save(b);
                    });
                }
            } catch (Exception ignored) {}
        }
    }

    private String extractOrderIdFromResource(JsonNode resource) {
        if (resource == null) return null;
        String id = null;
        if (resource.has("id") && "order".equalsIgnoreCase(resource.path("intent").asText())) {
            id = resource.path("id").asText();
        } else if (resource.has("supplementary_data")) {
            JsonNode relatedIds = resource.path("supplementary_data").path("related_ids");
            if (relatedIds.has("order_id")) {
                id = relatedIds.path("order_id").asText();
            }
        }
        if (id == null && resource.has("id")) {
            id = resource.path("id").asText();
        }
        if (id != null && ORDER_ID_PATTERN.matcher(id.trim()).matches()) {
            return id.trim();
        }
        return null;
    }
}
