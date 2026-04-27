package it.unical.ea.Travel.Services;

import it.unical.ea.Travel.DTOs.ExperienceRequestDTO;
import it.unical.ea.Travel.DTOs.ExperienceResponseDTO;
import it.unical.ea.Travel.Entities.Experience;
import it.unical.ea.Travel.Entities.User;
import it.unical.ea.Travel.Mappers.ExperienceMapper;
import it.unical.ea.Travel.Repositories.ExperienceRepository;
import it.unical.ea.Travel.Repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ExperienceService {

    private final ExperienceRepository experienceRepository;
    private final UserRepository userRepository;

    public ExperienceService(ExperienceRepository experienceRepository, UserRepository userRepository) {
        this.experienceRepository = experienceRepository;
        this.userRepository = userRepository;
    }

    public ExperienceResponseDTO saveExperience(ExperienceRequestDTO request) {
        User organizer = getOrganizer(request.organizerId());
        Experience experience = ExperienceMapper.toEntity(request, organizer);
        Experience savedExperience = experienceRepository.save(experience);
        return ExperienceMapper.toResponseDTO(savedExperience);
    }

    public ExperienceResponseDTO getExperience(String stringId) {
        UUID uuid = UUID.fromString(stringId);
        Experience experience = experienceRepository.findByIdAndDeletedAtIsNull(uuid)
                .orElseThrow(() -> new RuntimeException("Experience non trovata"));
        return ExperienceMapper.toResponseDTO(experience);
    }

    public List<ExperienceResponseDTO> getExperiences() {
        return experienceRepository.findByDeletedAtIsNull()
                .stream()
                .map(ExperienceMapper::toResponseDTO)
                .toList();
    }

    public List<ExperienceResponseDTO> getExperiencesByOrganizer(String organizerId) {
        UUID uuid = UUID.fromString(organizerId);
        return experienceRepository.findByOrganizerIdAndDeletedAtIsNull(uuid)
                .stream()
                .map(ExperienceMapper::toResponseDTO)
                .toList();
    }

    public void updateExperience() {
        return; // da implementare
    }

    public void deleteExperience(String stringId) {
        UUID uuid = UUID.fromString(stringId);
        Experience experience = experienceRepository.findByIdAndDeletedAtIsNull(uuid)
                .orElseThrow(() -> new RuntimeException("Experience non trovata"));
        experience.setDeletedAt(LocalDateTime.now());
        experienceRepository.save(experience);
    }

    private User getOrganizer(UUID organizerId) {
        if (organizerId == null) {
            throw new RuntimeException("L'organizerId e' obbligatorio");
        }

        return userRepository.findById(organizerId)
                .filter(user -> !user.isDeleted())
                .orElseThrow(() -> new RuntimeException("Organizer non trovato"));
    }
}
