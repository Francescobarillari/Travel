package it.unical.ea.Travel.Repositories.location;

import it.unical.ea.Travel.Entities.location.Location;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface LocationRepository extends JpaRepository<Location, UUID>, org.springframework.data.jpa.repository.JpaSpecificationExecutor<Location> {
    
    @Query("SELECT t FROM Location t WHERE " +
           "(LOWER(t.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Location> searchByKeyword(
            @Param("keyword") String keyword, 
            Pageable pageable);

    Optional<Location> findByNameIgnoreCase(String name);
}
