package it.unical.ea.Travel.Services.experience.stop;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import it.unical.ea.Travel.DTOs.experience.stop.ExperienceStopRequestDTO;
import it.unical.ea.Travel.DTOs.experience.stop.ExperienceStopResponseDTO;
import it.unical.ea.Travel.Entities.experience.Experience;
import it.unical.ea.Travel.Entities.experience.stop.ExperienceStop;
import it.unical.ea.Travel.Entities.location.Location;
import it.unical.ea.Travel.Mappers.experience.stop.ExperienceStopMapper;
import it.unical.ea.Travel.Repositories.experience.ExperienceRepository;
import it.unical.ea.Travel.Repositories.experience.stop.ExperienceStopRepository;
import it.unical.ea.Travel.Repositories.location.LocationRepository;

@Service
public class ExperienceStopService {

    private static final Logger logger = LoggerFactory.getLogger(ExperienceStopService.class);

    private final ExperienceStopRepository experienceStopRepository;
    private final ExperienceRepository experienceRepository;
    private final LocationRepository locationRepository;
    private final ExperienceStopMapper experienceStopMapper;

    public ExperienceStopService(
            ExperienceStopRepository experienceStopRepository,
            ExperienceRepository experienceRepository,
            LocationRepository locationRepository,
            ExperienceStopMapper experienceStopMapper) {
        this.experienceStopRepository = experienceStopRepository;
        this.experienceRepository = experienceRepository;
        this.locationRepository = locationRepository;
        this.experienceStopMapper = experienceStopMapper;
    }

    public ExperienceStopResponseDTO saveExperienceStop(ExperienceStopRequestDTO request) {
        logger.info("Creating experience stop for experienceId={} locationId={} sequenceOrder={}",
                request.experienceId(), request.locationId(), request.sequenceOrder());
        Experience experience = getExperience(request.experienceId());
        Location location = getLocation(request.locationId());
        ExperienceStop experienceStop = experienceStopMapper.toEntity(request, experience, location);
        ExperienceStop savedExperienceStop = experienceStopRepository.save(experienceStop);
        logger.info("Created experience stop id={} for experienceId={} locationId={}",
                savedExperienceStop.getId(), experience.getId(), location.getId());
        return experienceStopMapper.toResponseDTO(savedExperienceStop);
    }

    public ExperienceStopResponseDTO getExperienceStop(String stringId) {
        logger.debug("Fetching experience stop id={}", stringId);
        UUID uuid = UUID.fromString(stringId);
        ExperienceStop experienceStop = experienceStopRepository.findById(uuid)
                .orElseThrow(() -> {
                    logger.warn("Experience stop not found for id={}", uuid);
                    return new RuntimeException("Tappa experience non trovata");
                });
        return experienceStopMapper.toResponseDTO(experienceStop);
    }

    public List<ExperienceStopResponseDTO> getExperienceStops(String experienceId) {
        if (experienceId != null && !experienceId.isBlank()) {
            logger.debug("Fetching experience stops for experienceId={}", experienceId);
            UUID uuid = UUID.fromString(experienceId);
            List<ExperienceStopResponseDTO> experienceStops = experienceStopRepository.findByExperienceIdOrderBySequenceOrderAsc(uuid)
                    .stream()
                    .map(experienceStopMapper::toResponseDTO)
                    .toList();
            logger.debug("Fetched {} experience stops for experienceId={}",
                    experienceStops.size(), experienceId);
            return experienceStops;
        }

        logger.debug("Fetching all experience stops");
        List<ExperienceStopResponseDTO> experienceStops = experienceStopRepository.findAll()
                .stream()
                .map(experienceStopMapper::toResponseDTO)
                .toList();
        logger.debug("Fetched {} experience stops", experienceStops.size());
        return experienceStops;
    }

    public void updateExperienceStop() {
        return; // da implementare
    }

    public void deleteExperienceStop(String stringId) {
        logger.info("Deleting experience stop id={}", stringId);
        UUID uuid = UUID.fromString(stringId);
        experienceStopRepository.deleteById(uuid);
        logger.info("Deletion request completed for experience stop id={}", stringId);
    }

    private Experience getExperience(UUID experienceId) {
        if (experienceId == null) {
            logger.warn("Missing experienceId while handling experience stop operation");
            throw new RuntimeException("L'experienceId e' obbligatorio");
        }

        return experienceRepository.findByIdAndDeletedAtIsNull(experienceId)
                .orElseThrow(() -> {
                    logger.warn("Experience not found for id={}", experienceId);
                    return new RuntimeException("Experience non trovata");
                });
    }

    private Location getLocation(UUID locationId) {
        if (locationId == null) {
            logger.warn("Missing locationId while handling experience stop operation");
            throw new RuntimeException("Il locationId e' obbligatorio");
        }

        return locationRepository.findById(locationId)
                .orElseThrow(() -> {
                    logger.warn("Location not found for id={}", locationId);
                    return new RuntimeException("Location non trovata");
                });
    }
}
