package it.unical.ea.Travel.Services.experience.image;

import java.util.List;
import java.util.UUID;

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

    private final ExperienceImageRepository experienceImageRepository;
    private final ExperienceRepository experienceRepository;

    public ExperienceImageService(
            ExperienceImageRepository experienceImageRepository,
            ExperienceRepository experienceRepository) {
        this.experienceImageRepository = experienceImageRepository;
        this.experienceRepository = experienceRepository;
    }

    public ExperienceImageResponseDTO saveExperienceImage(ExperienceImageRequestDTO request) {
        Experience experience = getExperience(request.experienceId());
        ExperienceImage experienceImage = ExperienceImageMapper.toEntity(request, experience);
        ExperienceImage savedExperienceImage = experienceImageRepository.save(experienceImage);
        return ExperienceImageMapper.toResponseDTO(savedExperienceImage);
    }

    public ExperienceImageResponseDTO getExperienceImage(String stringId) {
        UUID uuid = UUID.fromString(stringId);
        ExperienceImage experienceImage = experienceImageRepository.findById(uuid)
                .orElseThrow(() -> new RuntimeException("Immagine experience non trovata"));
        return ExperienceImageMapper.toResponseDTO(experienceImage);
    }

    public List<ExperienceImageResponseDTO> getExperienceImages(String experienceId) {
        if (experienceId != null && !experienceId.isBlank()) {
            UUID uuid = UUID.fromString(experienceId);
            return experienceImageRepository.findByExperienceIdOrderByDisplayOrderAsc(uuid)
                    .stream()
                    .map(ExperienceImageMapper::toResponseDTO)
                    .toList();
        }

        return experienceImageRepository.findAll()
                .stream()
                .map(ExperienceImageMapper::toResponseDTO)
                .toList();
    }

    public void updateExperienceImage() {
        return; // da implementare
    }

    public void deleteExperienceImage(String stringId) {
        UUID uuid = UUID.fromString(stringId);
        experienceImageRepository.deleteById(uuid);
    }

    private Experience getExperience(UUID experienceId) {
        if (experienceId == null) {
            throw new RuntimeException("L'experienceId e' obbligatorio");
        }

        return experienceRepository.findByIdAndDeletedAtIsNull(experienceId)
                .orElseThrow(() -> new RuntimeException("Experience non trovata"));
    }
}
