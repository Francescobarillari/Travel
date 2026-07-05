package it.unical.ea.Travel.Repositories.localita;

import it.unical.ea.Travel.Entities.localita.Localita;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface LocalitaRepository extends JpaRepository<Localita, UUID> {
    
    @Query("SELECT t FROM Localita t WHERE " +
           "(LOWER(t.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Localita> searchByKeyword(
            @Param("keyword") String keyword, 
            Pageable pageable);
}
