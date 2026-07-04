package it.unical.ea.Travel.Repositories.trip;

import it.unical.ea.Travel.Entities.trip.Trip;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface TripRepository extends JpaRepository<Trip, UUID> {
    
    @EntityGraph(attributePaths = {"organizer"})
    @Query("SELECT t FROM Trip t WHERE " +
           "(LOWER(t.location) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:minPrice IS NULL OR (SELECT COALESCE(SUM(a.price), 0) FROM t.standardItinerary i JOIN i.activities a) >= :minPrice) " +
           "AND (:maxPrice IS NULL OR (SELECT COALESCE(SUM(a.price), 0) FROM t.standardItinerary i JOIN i.activities a) <= :maxPrice)")
    Page<Trip> searchByKeyword(
            @Param("keyword") String keyword, 
            @Param("minPrice") Double minPrice, 
            @Param("maxPrice") Double maxPrice, 
            Pageable pageable);
}
