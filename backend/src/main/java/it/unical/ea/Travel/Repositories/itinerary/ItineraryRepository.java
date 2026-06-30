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

    @Lock(LockModeType.OPTIMISTIC_FORCE_INCREMENT)
    @Query("SELECT i FROM Itinerary i WHERE i.id = :id")
    Optional<Itinerary> findByIdForUpdate(@Param("id") UUID id);
}
