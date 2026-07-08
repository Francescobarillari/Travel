package it.unical.ea.Travel.Repositories.itinerary;

import it.unical.ea.Travel.Entities.itinerary.ItineraryBooking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;
import java.util.Optional;

@Repository
public interface ItineraryBookingRepository extends JpaRepository<ItineraryBooking, UUID> {
    Optional<ItineraryBooking> findByUserIdAndItineraryId(UUID userId, UUID itineraryId);
    java.util.List<ItineraryBooking> findByPaymentIntentId(String paymentIntentId);
    java.util.List<ItineraryBooking> findByUserId(UUID userId);
}
