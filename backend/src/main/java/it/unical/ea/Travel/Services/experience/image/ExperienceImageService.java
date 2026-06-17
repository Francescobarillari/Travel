package it.unical.ea.Travel.Services.experience.image;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import it.unical.ea.Travel.DTOs.experience.image.ExperienceImageRequestDTO;
import it.unical.ea.Travel.DTOs.experience.image.ExperienceImageResponseDTO;
import it.unical.ea.Travel.Entities.experience.Experience;
import it.unical.ea.Travel.Entities.experience.image.ExperienceImage;
import it.unical.ea.Travel.Mappers.experience.image.ExperienceImageMapper;
import it.unical.ea.Travel.Repositories.experience.image.ExperienceImageRepository;
import it.unical.ea.Travel.Repositories.experience.ExperienceRepository;

@Service
public class ExperienceImageService {

    private static final Logger logger = LoggerFactory.getLogger(ExperienceImageService.class);

    private final ExperienceImageRepository experienceImageRepository;
    private final ExperienceRepository experienceRepository;
    private final ExperienceImageMapper experienceImageMapper;

    public ExperienceImageService(
            ExperienceImageRepository experienceImageRepository,
            ExperienceRepository experienceRepository,
            ExperienceImageMapper experienceImageMapper) {
        this.experienceImageRepository = experienceImageRepository;
        this.experienceRepository = experienceRepository;
        this.experienceImageMapper = experienceImageMapper;
    }

    public ExperienceImageResponseDTO saveExperienceImage(ExperienceImageRequestDTO request) {
        logger.info("Creating experience image for experienceId={} displayOrder={}",
                request.experienceId(), request.displayOrder());
        Experience experience = getExperience(request.experienceId());
        ExperienceImage experienceImage = experienceImageMapper.toEntity(request, experience);
        ExperienceImage savedExperienceImage = experienceImageRepository.save(experienceImage);
        logger.info("Created experience image id={} for experienceId={}",
                savedExperienceImage.getId(), experience.getId());
        return experienceImageMapper.toResponseDTO(savedExperienceImage);
    }

    public ExperienceImageResponseDTO getExperienceImage(String stringId) {
        logger.debug("Fetching experience image id={}", stringId);
        UUID uuid = UUID.fromString(stringId);
        ExperienceImage experienceImage = experienceImageRepository.findById(uuid)
                .orElseThrow(() -> {
                    logger.warn("Experience image not found for id={}", uuid);
                    return new RuntimeException("Immagine experience non trovata");
                });
        return experienceImageMapper.toResponseDTO(experienceImage);
    }

    public List<ExperienceImageResponseDTO> getExperienceImages(String experienceId) {
        if (experienceId != null && !experienceId.isBlank()) {
            logger.debug("Fetching experience images for experienceId={}", experienceId);
            UUID uuid = UUID.fromString(experienceId);
            List<ExperienceImageResponseDTO> experienceImages = experienceImageRepository.findByExperienceIdOrderByDisplayOrderAsc(uuid)
                    .stream()
                    .map(experienceImageMapper::toResponseDTO)
                    .toList();
            logger.debug("Fetched {} experience images for experienceId={}",
                    experienceImages.size(), experienceId);
            return experienceImages;
        }

        logger.debug("Fetching all experience images");
        List<ExperienceImageResponseDTO> experienceImages = experienceImageRepository.findAll()
                .stream()
                .map(experienceImageMapper::toResponseDTO)
                .toList();
        logger.debug("Fetched {} experience images", experienceImages.size());
        return experienceImages;
    }

    public void updateExperienceImage() {
        return; // da implementare
    }

    public void deleteExperienceImage(String stringId) {
        logger.info("Deleting experience image id={}", stringId);
        UUID uuid = UUID.fromString(stringId);
        experienceImageRepository.deleteById(uuid);
        logger.info("Deletion request completed for experience image id={}", stringId);
    }

    private Experience getExperience(UUID experienceId) {
        if (experienceId == null) {
            logger.warn("Missing experienceId while handling experience image operation");
            throw new RuntimeException("L'experienceId e' obbligatorio");
        }

        return experienceRepository.findByIdAndDeletedAtIsNull(experienceId)
                .orElseThrow(() -> {
                    logger.warn("Experience not found for id={}", experienceId);
                    return new RuntimeException("Experience non trovata");
                });
    }
}
