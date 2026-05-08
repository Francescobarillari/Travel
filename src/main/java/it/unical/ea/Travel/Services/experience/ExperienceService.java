package it.unical.ea.Travel.Services.experience;

import it.unical.ea.Travel.DTOs.experience.ExperienceRequestDTO;
import it.unical.ea.Travel.DTOs.experience.ExperienceResponseDTO;
import it.unical.ea.Travel.Entities.experience.Experience;
import it.unical.ea.Travel.Entities.user.User;
import it.unical.ea.Travel.Mappers.experience.ExperienceMapper;
import it.unical.ea.Travel.Repositories.experience.ExperienceRepository;
import it.unical.ea.Travel.Repositories.user.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ExperienceService {

    private final ExperienceRepository experienceRepository;
    private final UserRepository userRepository;
    private final ExperienceMapper experienceMapper;

    public ExperienceService(
            ExperienceRepository experienceRepository,
            UserRepository userRepository,
            ExperienceMapper experienceMapper) {
        this.experienceRepository = experienceRepository;
        this.userRepository = userRepository;
        this.experienceMapper = experienceMapper;
    }

    public ExperienceResponseDTO saveExperience(ExperienceRequestDTO request) {
        User organizer = getOrganizer(request.organizerId());
        Experience experience = experienceMapper.toEntity(request, organizer);
        Experience savedExperience = experienceRepository.save(experience);
        return experienceMapper.toResponseDTO(savedExperience);
    }

    public ExperienceResponseDTO getExperience(String stringId) {
        UUID uuid = UUID.fromString(stringId);
        Experience experience = experienceRepository.findByIdAndDeletedAtIsNull(uuid)
                .orElseThrow(() -> new RuntimeException("Experience non trovata"));
        return experienceMapper.toResponseDTO(experience);
    }

    public List<ExperienceResponseDTO> getExperiences() {
        return experienceRepository.findByDeletedAtIsNull()
                .stream()
                .map(experienceMapper::toResponseDTO)
                .toList();
    }

    public List<ExperienceResponseDTO> getExperiencesByOrganizer(String organizerId) {
        UUID uuid = UUID.fromString(organizerId);
        return experienceRepository.findByOrganizerIdAndDeletedAtIsNull(uuid)
                .stream()
                .map(experienceMapper::toResponseDTO)
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
