package it.unical.ea.Travel.Repositories.activity;

import it.unical.ea.Travel.Entities.activity.Activity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ActivityRepository extends JpaRepository<Activity, UUID> {
    List<Activity> findByApproved(Boolean approved);
    
    @Lock(LockModeType.OPTIMISTIC_FORCE_INCREMENT)
    @Query("SELECT a FROM Activity a WHERE a.id = :id")
    Optional<Activity> findByIdForUpdate(@Param("id") UUID id);

    @Query("SELECT a FROM Activity a WHERE " +
           "(LOWER(a.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(a.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(a.location) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:minPrice IS NULL OR a.price >= :minPrice) " +
           "AND (:maxPrice IS NULL OR a.price <= :maxPrice)")
    Page<Activity> searchByKeyword(
            @Param("keyword") String keyword, 
            @Param("minPrice") Double minPrice, 
            @Param("maxPrice") Double maxPrice, 
            Pageable pageable);
}