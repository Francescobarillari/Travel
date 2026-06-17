package it.unical.ea.Travel.DTOs.experience.stop;

import java.time.LocalTime;
import java.util.UUID;

public record ExperienceStopResponseDTO(
        UUID id,
        UUID experienceId,
        Integer sequenceOrder,
        String title,
        String description,
        UUID locationId,
        String locationName,
        LocalTime arrivalTime,
        LocalTime departureTime,
        Integer durationMinutes) {
}
