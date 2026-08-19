package it.unical.ea.Travel.Repositories.activity;

import it.unical.ea.Travel.Entities.activity.ActivityBooking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ActivityBookingRepository extends JpaRepository<ActivityBooking, UUID> {

    Optional<ActivityBooking> findByUserIdAndActivityId(UUID userId, UUID activityId);

    @Query("SELECT COUNT(DISTINCT ab.user.id) FROM ActivityBooking ab WHERE ab.activity.id = :activityId AND ab.status != it.unical.ea.Travel.Entities.payment.BookingStatus.FAILED")
    long countDirectParticipants(@Param("activityId") UUID activityId);

    List<ActivityBooking> findByActivityId(UUID activityId);

    List<ActivityBooking> findByUserIdAndItineraryId(UUID userId, UUID itineraryId);

    List<ActivityBooking> findByUserId(UUID userId);

    List<ActivityBooking> findByPaymentIntentId(String paymentIntentId);

    @Query("SELECT COUNT(ab) > 0 FROM ActivityBooking ab WHERE ab.user.id = :userId AND ab.activity.template.id = :templateId AND ab.status = :status")
    boolean existsByUserIdAndActivityTemplateIdAndStatus(@Param("userId") UUID userId, @Param("templateId") UUID templateId, @Param("status") it.unical.ea.Travel.Entities.payment.BookingStatus status);

    @Query("SELECT COUNT(ab) > 0 FROM ActivityBooking ab WHERE ab.user.id = :userId AND ab.activity.template.id = :templateId AND ab.status = :status AND (ab.activity.endTime <= :now OR (ab.activity.endTime IS NULL AND ab.activity.startTime <= :now))")
    boolean existsCompletedBookingByTemplate(@Param("userId") UUID userId, @Param("templateId") UUID templateId, @Param("status") it.unical.ea.Travel.Entities.payment.BookingStatus status, @Param("now") java.time.LocalDateTime now);
}

