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
    boolean existsByUserIdAndItineraryIdAndStatus(UUID userId, UUID itineraryId, it.unical.ea.Travel.Entities.payment.BookingStatus status);

    @org.springframework.data.jpa.repository.Query("SELECT COUNT(ib) > 0 FROM ItineraryBooking ib WHERE ib.user.id = :userId AND ib.itinerary.id = :itineraryId AND ib.status = :status AND (ib.itinerary.endDateTime <= :now OR (ib.itinerary.endDateTime IS NULL AND ib.itinerary.startDateTime <= :now))")
    boolean existsCompletedBookingByItinerary(@org.springframework.data.repository.query.Param("userId") UUID userId, @org.springframework.data.repository.query.Param("itineraryId") UUID itineraryId, @org.springframework.data.repository.query.Param("status") it.unical.ea.Travel.Entities.payment.BookingStatus status, @org.springframework.data.repository.query.Param("now") java.time.LocalDateTime now);
}

