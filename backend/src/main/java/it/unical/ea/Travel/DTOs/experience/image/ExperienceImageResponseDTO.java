package it.unical.ea.Travel.DTOs.experience.image;

import java.util.UUID;

public record ExperienceImageResponseDTO(
        UUID id,
        UUID experienceId,
        String url,
        Integer displayOrder) {
}
