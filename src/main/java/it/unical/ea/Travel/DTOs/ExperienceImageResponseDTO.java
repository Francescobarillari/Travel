package it.unical.ea.Travel.DTOs;

import java.util.UUID;

public record ExperienceImageResponseDTO(
        UUID id,
        UUID experienceId,
        String url,
        Integer displayOrder) {
}
