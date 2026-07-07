package it.unical.ea.Travel.Repositories.activity;

import it.unical.ea.Travel.Entities.activity.ActivityTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ActivityTemplateRepository extends JpaRepository<ActivityTemplate, UUID> {
    List<ActivityTemplate> findByApproved(Boolean approved);

    @Query("SELECT a FROM ActivityTemplate a WHERE " +
           "(LOWER(a.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(a.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(a.location) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<ActivityTemplate> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
}
