package it.unical.ea.Travel.Repositories.itinerary;

import it.unical.ea.Travel.Entities.itinerary.ItineraryJoinRequest;
import it.unical.ea.enums.JoinRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ItineraryJoinRequestRepository extends JpaRepository<ItineraryJoinRequest, UUID> {

    Optional<ItineraryJoinRequest> findByUserIdAndItineraryId(UUID userId, UUID itineraryId);

    List<ItineraryJoinRequest> findByItineraryIdOrderByCreatedAtDesc(UUID itineraryId);

    List<ItineraryJoinRequest> findByItineraryIdAndStatusOrderByCreatedAtDesc(UUID itineraryId, JoinRequestStatus status);

    List<ItineraryJoinRequest> findByUserIdAndStatusOrderByCreatedAtDesc(UUID userId, JoinRequestStatus status);

    boolean existsByUserIdAndItineraryIdAndStatus(UUID userId, UUID itineraryId, JoinRequestStatus status);

    long countByItineraryIdAndStatus(UUID itineraryId, JoinRequestStatus status);

    @Query("SELECT r FROM ItineraryJoinRequest r JOIN FETCH r.user WHERE r.itinerary.id = :itineraryId AND r.status = :status")
    List<ItineraryJoinRequest> findByItineraryIdAndStatusWithUser(@Param("itineraryId") UUID itineraryId, @Param("status") JoinRequestStatus status);

    @Query("SELECT r FROM ItineraryJoinRequest r JOIN FETCH r.itinerary WHERE r.user.id = :userId AND r.status = :status")
    List<ItineraryJoinRequest> findByUserIdAndStatusWithItinerary(@Param("userId") UUID userId, @Param("status") JoinRequestStatus status);
}
