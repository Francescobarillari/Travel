package it.unical.ea.Travel.Repositories.itinerary;

import it.unical.ea.Travel.Entities.itinerary.Itinerary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ItineraryRepository extends JpaRepository<Itinerary, UUID> {

    List<Itinerary> findByCreatorId(UUID creatorId);
}
