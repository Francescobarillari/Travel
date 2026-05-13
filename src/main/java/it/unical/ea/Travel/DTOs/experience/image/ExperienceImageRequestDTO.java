package it.unical.ea.Travel.DTOs.experience.image;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record ExperienceImageRequestDTO(
        @NotNull(message = "{experienceId.NotNull.experienceImage}")
        UUID experienceId,
        @NotBlank(message = "{url.NotBlank.experienceImage}")
        @Size(max = 500, message = "{url.Size.experienceImage}")
        String url,
        @NotNull(message = "{displayOrder.NotNull.experienceImage}")
        @PositiveOrZero(message = "{displayOrder.PositiveOrZero.experienceImage}")
        Integer displayOrder) {
}
