package it.unical.ea.Travel.Mappers.experience.image;

import it.unical.ea.Travel.DTOs.experience.image.ExperienceImageRequestDTO;
import it.unical.ea.Travel.DTOs.experience.image.ExperienceImageResponseDTO;
import it.unical.ea.Travel.Entities.experience.Experience;
import it.unical.ea.Travel.Entities.experience.image.ExperienceImage;

public final class ExperienceImageMapper {

    private ExperienceImageMapper() {
    }

    public static ExperienceImage toEntity(ExperienceImageRequestDTO request, Experience experience) {
        ExperienceImage experienceImage = new ExperienceImage();
        applyRequestToEntity(request, experienceImage, experience);
        return experienceImage;
    }

    public static void applyRequestToEntity(
            ExperienceImageRequestDTO request,
            ExperienceImage experienceImage,
            Experience experience) {
        experienceImage.setExperience(experience);
        experienceImage.setUrl(request.url());
        experienceImage.setDisplayOrder(request.displayOrder());
    }

    public static ExperienceImageResponseDTO toResponseDTO(ExperienceImage experienceImage) {
        return new ExperienceImageResponseDTO(
                experienceImage.getId(),
                experienceImage.getExperience() != null ? experienceImage.getExperience().getId() : null,
                experienceImage.getUrl(),
                experienceImage.getDisplayOrder());
    }
}
