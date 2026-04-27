package it.unical.ea.Travel.Repositories;

import it.unical.ea.Travel.Entities.Experience;
import it.unical.ea.Travel.Entities.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ExperienceRepository extends JpaRepository<Experience, UUID> {

    @EntityGraph(attributePaths = "organizer")
    Optional<Experience> findByIdAndDeletedAtIsNull(UUID id);

    @EntityGraph(attributePaths = "organizer")
    List<Experience> findByDeletedAtIsNull();

    @EntityGraph(attributePaths = "organizer")
    List<Experience> findByOrganizerAndDeletedAtIsNull(User organizer);

    @EntityGraph(attributePaths = "organizer")
    List<Experience> findByOrganizerIdAndDeletedAtIsNull(UUID organizerId);
}
