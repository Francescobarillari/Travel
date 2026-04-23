package it.unical.ea.Travel.Repositories;

import it.unical.ea.Travel.Entities.Experience;
import it.unical.ea.Travel.Entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ExperienceRepository extends JpaRepository<Experience, UUID> {

    List<Experience> findByDeletedAtIsNull();

    List<Experience> findByOrganizerAndDeletedAtIsNull(User organizer);

    List<Experience> findByOrganizerIdAndDeletedAtIsNull(UUID organizerId);
}
