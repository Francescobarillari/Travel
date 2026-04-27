package it.unical.ea.Travel.Repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import it.unical.ea.Travel.Entities.ExperienceImage;

@Repository
public interface ExperienceImageRepository extends JpaRepository<ExperienceImage, UUID> {

    @EntityGraph(attributePaths = "experience")
    Optional<ExperienceImage> findById(UUID id);

    @EntityGraph(attributePaths = "experience")
    List<ExperienceImage> findByExperienceIdOrderByDisplayOrderAsc(UUID experienceId);
}
