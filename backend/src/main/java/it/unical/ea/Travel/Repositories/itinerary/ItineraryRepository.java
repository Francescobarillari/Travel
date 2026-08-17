package it.unical.ea.Travel.Repositories.itinerary;

import it.unical.ea.Travel.Entities.itinerary.Itinerary;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ItineraryRepository extends JpaRepository<Itinerary, UUID> {

    List<Itinerary> findByCreatorId(UUID creatorId);

    List<Itinerary> findByCreatorIdAndVisibilityIgnoreCase(UUID creatorId, String visibility);

    List<Itinerary> findByVisibilityIgnoreCase(String visibility);

    @Query("SELECT i FROM Itinerary i WHERE UPPER(i.visibility) = 'PUBLIC' OR (i.creator IS NOT NULL AND i.creator.id = :creatorId)")
    List<Itinerary> findPublicOrCreatorItineraries(@Param("creatorId") UUID creatorId);

    @Lock(LockModeType.OPTIMISTIC_FORCE_INCREMENT)
    @Query("SELECT i FROM Itinerary i WHERE i.id = :id")
    Optional<Itinerary> findByIdForUpdate(@Param("id") UUID id);
}
