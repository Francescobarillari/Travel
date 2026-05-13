package it.unical.ea.Travel.DTOs.experience.stop;

import java.time.LocalTime;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record ExperienceStopRequestDTO(
        @NotNull(message = "{experienceId.NotNull.experienceStop}")
        UUID experienceId,
        @NotNull(message = "{sequenceOrder.NotNull.experienceStop}")
        @Positive(message = "{sequenceOrder.Positive.experienceStop}")
        Integer sequenceOrder,
        @NotBlank(message = "{title.NotBlank.experienceStop}")
        @Size(max = 255, message = "{title.Size.experienceStop}")
        String title,
        @Size(max = 5000, message = "{description.Size.experienceStop}")
        String description,
        @NotNull(message = "{locationId.NotNull.experienceStop}")
        UUID locationId,
        LocalTime arrivalTime,
        LocalTime departureTime,
        @Positive(message = "{durationMinutes.Positive.experienceStop}")
        Integer durationMinutes) {
}
