package it.unical.ea.Travel.Services;

import it.unical.ea.Travel.Entities.Experience;
import it.unical.ea.Travel.Repositories.ExperienceRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ExperienceService {

    private final ExperienceRepository experienceRepository;

    public ExperienceService(ExperienceRepository experienceRepository) {
        this.experienceRepository = experienceRepository;
    }

    public Experience saveExperience(Experience experience) {
        return experienceRepository.save(experience);
    }

    public Experience getExperience(String stringId) {
        UUID uuid = UUID.fromString(stringId);
        return experienceRepository.findById(uuid)
                .filter(experience -> !experience.isDeleted())
                .orElseThrow(() -> new RuntimeException("Experience non trovata"));
    }

    public List<Experience> getExperiences() {
        return experienceRepository.findByDeletedAtIsNull();
    }

    public List<Experience> getExperiencesByOrganizer(String organizerId) {
        UUID uuid = UUID.fromString(organizerId);
        return experienceRepository.findByOrganizerIdAndDeletedAtIsNull(uuid);
    }

    public void updateExperience() {
        return; // da implementare
    }

    public void deleteExperience(String stringId) {
        Experience experience = getExperience(stringId);
        experience.setDeletedAt(LocalDateTime.now());
        experienceRepository.save(experience);
    }
}
