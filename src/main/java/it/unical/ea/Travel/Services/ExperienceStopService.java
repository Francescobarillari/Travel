package it.unical.ea.Travel.Services;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import it.unical.ea.Travel.DTOs.experience.stop.ExperienceStopRequestDTO;
import it.unical.ea.Travel.DTOs.experience.stop.ExperienceStopResponseDTO;
import it.unical.ea.Travel.Entities.Experience;
import it.unical.ea.Travel.Entities.ExperienceStop;
import it.unical.ea.Travel.Entities.Location;
import it.unical.ea.Travel.Mappers.ExperienceStopMapper;
import it.unical.ea.Travel.Repositories.ExperienceRepository;
import it.unical.ea.Travel.Repositories.ExperienceStopRepository;
import it.unical.ea.Travel.Repositories.LocationRepository;

@Service
public class ExperienceStopService {

    private final ExperienceStopRepository experienceStopRepository;
    private final ExperienceRepository experienceRepository;
    private final LocationRepository locationRepository;

    public ExperienceStopService(
            ExperienceStopRepository experienceStopRepository,
            ExperienceRepository experienceRepository,
            LocationRepository locationRepository) {
        this.experienceStopRepository = experienceStopRepository;
        this.experienceRepository = experienceRepository;
        this.locationRepository = locationRepository;
    }

    public ExperienceStopResponseDTO saveExperienceStop(ExperienceStopRequestDTO request) {
        Experience experience = getExperience(request.experienceId());
        Location location = getLocation(request.locationId());
        ExperienceStop experienceStop = ExperienceStopMapper.toEntity(request, experience, location);
        ExperienceStop savedExperienceStop = experienceStopRepository.save(experienceStop);
        return ExperienceStopMapper.toResponseDTO(savedExperienceStop);
    }

    public ExperienceStopResponseDTO getExperienceStop(String stringId) {
        UUID uuid = UUID.fromString(stringId);
        ExperienceStop experienceStop = experienceStopRepository.findById(uuid)
                .orElseThrow(() -> new RuntimeException("Tappa experience non trovata"));
        return ExperienceStopMapper.toResponseDTO(experienceStop);
    }

    public List<ExperienceStopResponseDTO> getExperienceStops(String experienceId) {
        if (experienceId != null && !experienceId.isBlank()) {
            UUID uuid = UUID.fromString(experienceId);
            return experienceStopRepository.findByExperienceIdOrderBySequenceOrderAsc(uuid)
                    .stream()
                    .map(ExperienceStopMapper::toResponseDTO)
                    .toList();
        }

        return experienceStopRepository.findAll()
                .stream()
                .map(ExperienceStopMapper::toResponseDTO)
                .toList();
    }

    public void updateExperienceStop() {
        return; // da implementare
    }

    public void deleteExperienceStop(String stringId) {
        UUID uuid = UUID.fromString(stringId);
        experienceStopRepository.deleteById(uuid);
    }

    private Experience getExperience(UUID experienceId) {
        if (experienceId == null) {
            throw new RuntimeException("L'experienceId e' obbligatorio");
        }

        return experienceRepository.findByIdAndDeletedAtIsNull(experienceId)
                .orElseThrow(() -> new RuntimeException("Experience non trovata"));
    }

    private Location getLocation(UUID locationId) {
        if (locationId == null) {
            throw new RuntimeException("Il locationId e' obbligatorio");
        }

        return locationRepository.findById(locationId)
                .orElseThrow(() -> new RuntimeException("Location non trovata"));
    }
}
