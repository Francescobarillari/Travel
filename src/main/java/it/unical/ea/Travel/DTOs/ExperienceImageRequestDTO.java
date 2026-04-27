package it.unical.ea.Travel.DTOs;

import java.util.UUID;

public record ExperienceImageRequestDTO(
        UUID experienceId,
        String url,
        Integer displayOrder) {
}
