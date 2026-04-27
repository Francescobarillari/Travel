package it.unical.ea.Travel.DTOs.experience.image;

import java.util.UUID;

public record ExperienceImageRequestDTO(
        UUID experienceId,
        String url,
        Integer displayOrder) {
}
