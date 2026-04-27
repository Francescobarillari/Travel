package it.unical.ea.Travel.DTOs;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import it.unical.ea.Travel.Entities.Experience.ExperienceStatus;
import it.unical.ea.Travel.Entities.Experience.ExperienceType;

public record ExperienceRequestDTO(
        UUID organizerId,
        ExperienceType type,
        String title,
        String description,
        BigDecimal basePrice,
        String currency,
        Integer maxParticipants,
        Integer minParticipants,
        Integer durationMinutes,
        LocalDate startDate,
        LocalDate endDate,
        String coverImageUrl,
        ExperienceStatus status) {
}
