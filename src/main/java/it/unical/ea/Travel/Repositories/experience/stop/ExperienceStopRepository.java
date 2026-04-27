package it.unical.ea.Travel.Repositories.experience.stop;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import it.unical.ea.Travel.Entities.experience.stop.ExperienceStop;

@Repository
public interface ExperienceStopRepository extends JpaRepository<ExperienceStop, UUID> {

    @EntityGraph(attributePaths = {"experience", "location"})
    Optional<ExperienceStop> findById(UUID id);

    @EntityGraph(attributePaths = {"experience", "location"})
    List<ExperienceStop> findByExperienceIdOrderBySequenceOrderAsc(UUID experienceId);
}
