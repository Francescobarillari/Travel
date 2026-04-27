package it.unical.ea.Travel.DTOs;

import java.time.LocalTime;
import java.util.UUID;

public record ExperienceStopRequestDTO(
        UUID experienceId,
        Integer sequenceOrder,
        String title,
        String description,
        UUID locationId,
        LocalTime arrivalTime,
        LocalTime departureTime,
        Integer durationMinutes) {
}
