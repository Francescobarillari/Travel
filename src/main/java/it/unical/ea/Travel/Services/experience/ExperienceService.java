package it.unical.ea.Travel.Services.experience;

import it.unical.ea.Travel.DTOs.experience.ExperienceRequestDTO;
import it.unical.ea.Travel.DTOs.experience.ExperienceResponseDTO;
import it.unical.ea.Travel.Entities.experience.Experience;
import it.unical.ea.Travel.Entities.user.User;
import it.unical.ea.Travel.Mappers.experience.ExperienceMapper;
import it.unical.ea.Travel.Repositories.experience.ExperienceRepository;
import it.unical.ea.Travel.Repositories.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ExperienceService {

    private static final Logger logger = LoggerFactory.getLogger(ExperienceService.class);

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
        logger.info("Creating experience for organizerId={} title='{}' type={} status={}",
                request.organizerId(), request.title(), request.type(), request.status());
        User organizer = getOrganizer(request.organizerId());
        Experience experience = experienceMapper.toEntity(request, organizer);
        Experience savedExperience = experienceRepository.save(experience);
        logger.info("Created experience id={} for organizerId={}",
                savedExperience.getId(), organizer.getId());
        return experienceMapper.toResponseDTO(savedExperience);
    }

    public ExperienceResponseDTO getExperience(String stringId) {
        logger.debug("Fetching experience id={}", stringId);
        UUID uuid = UUID.fromString(stringId);
        Experience experience = experienceRepository.findByIdAndDeletedAtIsNull(uuid)
                .orElseThrow(() -> {
                    logger.warn("Experience not found for id={}", uuid);
                    return new RuntimeException("Experience non trovata");
                });
        return experienceMapper.toResponseDTO(experience);
    }

    public List<ExperienceResponseDTO> getExperiences() {
        logger.debug("Fetching all experiences");
        List<ExperienceResponseDTO> experiences = experienceRepository.findByDeletedAtIsNull()
                .stream()
                .map(experienceMapper::toResponseDTO)
                .toList();
        logger.debug("Fetched {} experiences", experiences.size());
        return experiences;
    }

    public List<ExperienceResponseDTO> getExperiencesByOrganizer(String organizerId) {
        logger.debug("Fetching experiences for organizerId={}", organizerId);
        UUID uuid = UUID.fromString(organizerId);
        List<ExperienceResponseDTO> experiences = experienceRepository.findByOrganizerIdAndDeletedAtIsNull(uuid)
                .stream()
                .map(experienceMapper::toResponseDTO)
                .toList();
        logger.debug("Fetched {} experiences for organizerId={}", experiences.size(), organizerId);
        return experiences;
    }

    public void updateExperience() {
        return; // da implementare
    }

    public void deleteExperience(String stringId) {
        logger.info("Soft deleting experience id={}", stringId);
        UUID uuid = UUID.fromString(stringId);
        Experience experience = experienceRepository.findByIdAndDeletedAtIsNull(uuid)
                .orElseThrow(() -> {
                    logger.warn("Experience not found for id={}", uuid);
                    return new RuntimeException("Experience non trovata");
                });
        experience.setDeletedAt(LocalDateTime.now());
        experienceRepository.save(experience);
        logger.info("Soft deleted experience id={}", experience.getId());
    }

    private User getOrganizer(UUID organizerId) {
        if (organizerId == null) {
            logger.warn("Missing organizerId while handling experience operation");
            throw new RuntimeException("L'organizerId e' obbligatorio");
        }

        return userRepository.findById(organizerId)
                .filter(user -> !user.isDeleted())
                .orElseThrow(() -> {
                    logger.warn("Organizer not found or deleted for organizerId={}", organizerId);
                    return new RuntimeException("Organizer non trovato");
                });
    }
}
