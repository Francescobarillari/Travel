package it.unical.ea.Travel.Controllers.payment;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.PaymentIntent;
import com.stripe.model.StripeObject;
import com.stripe.net.Webhook;
import it.unical.ea.Travel.Entities.activity.ActivityBooking;
import it.unical.ea.Travel.Entities.itinerary.ItineraryBooking;
import it.unical.ea.Travel.Entities.payment.BookingStatus;
import it.unical.ea.Travel.Repositories.activity.ActivityBookingRepository;
import it.unical.ea.Travel.Repositories.itinerary.ItineraryBookingRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/webhook/stripe")
public class StripeWebhookController {

    @Value("${stripe.webhook.secret}")
    private String endpointSecret;

    private final ItineraryBookingRepository itineraryBookingRepository;
    private final ActivityBookingRepository activityBookingRepository;

    public StripeWebhookController(ItineraryBookingRepository itineraryBookingRepository,
                                   ActivityBookingRepository activityBookingRepository) {
        this.itineraryBookingRepository = itineraryBookingRepository;
        this.activityBookingRepository = activityBookingRepository;
    }

    @PostMapping
    public ResponseEntity<String> handleStripeEvent(@RequestBody String payload, @RequestHeader("Stripe-Signature") String sigHeader) {
        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, endpointSecret);
        } catch (SignatureVerificationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid payload");
        }

        EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
        StripeObject stripeObject = null;
        if (dataObjectDeserializer.getObject().isPresent()) {
            stripeObject = dataObjectDeserializer.getObject().get();
        }

        if (event.getType().equals("payment_intent.succeeded")) {
            if (stripeObject instanceof PaymentIntent paymentIntent) {
                String paymentIntentId = paymentIntent.getId();
                confirmBookings(paymentIntentId);
            }
        } else if (event.getType().equals("payment_intent.payment_failed")) {
            if (stripeObject instanceof PaymentIntent paymentIntent) {
                String paymentIntentId = paymentIntent.getId();
                failBookings(paymentIntentId);
            }
        }

        return ResponseEntity.ok("Success");
    }

    private void confirmBookings(String paymentIntentId) {
        updateBookingStatus(paymentIntentId, BookingStatus.CONFIRMED);
    }

    private void failBookings(String paymentIntentId) {
        updateBookingStatus(paymentIntentId, BookingStatus.FAILED);
    }

    private void updateBookingStatus(String paymentIntentId, BookingStatus status) {
        List<ItineraryBooking> itineraryBookings = itineraryBookingRepository.findByPaymentIntentId(paymentIntentId);
        for (ItineraryBooking ib : itineraryBookings) {
            ib.setStatus(status);
            itineraryBookingRepository.save(ib);
        }

        List<ActivityBooking> activityBookings = activityBookingRepository.findByPaymentIntentId(paymentIntentId);
        for (ActivityBooking ab : activityBookings) {
            ab.setStatus(status);
            activityBookingRepository.save(ab);
        }
    }
}
