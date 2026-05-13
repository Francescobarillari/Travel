package it.unical.ea.Travel.DTOs.experience;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import it.unical.ea.Travel.Entities.experience.Experience.ExperienceStatus;
import it.unical.ea.Travel.Entities.experience.Experience.ExperienceType;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record ExperienceRequestDTO(
        @NotNull(message = "{organizerId.NotNull.experience}")
        UUID organizerId,
        @NotNull(message = "{type.NotNull.experience}")
        ExperienceType type,
        @NotBlank(message = "{title.NotBlank.experience}")
        @Size(max = 255, message = "{title.Size.experience}")
        String title,
        @Size(max = 5000, message = "{description.Size.experience}")
        String description,
        @NotNull(message = "{basePrice.NotNull.experience}")
        @PositiveOrZero(message = "{basePrice.PositiveOrZero.experience}")
        @Digits(integer = 8, fraction = 2, message = "{basePrice.Digits.experience}")
        BigDecimal basePrice,
        @NotBlank(message = "{currency.NotBlank.experience}")
        @Pattern(regexp = "^[A-Z]{3}$", message = "{currency.Pattern.experience}")
        String currency,
        @Positive(message = "{maxParticipants.Positive.experience}")
        Integer maxParticipants,
        @Positive(message = "{minParticipants.Positive.experience}")
        Integer minParticipants,
        @Positive(message = "{durationMinutes.Positive.experience}")
        Integer durationMinutes,
        LocalDate startDate,
        LocalDate endDate,
        @Size(max = 500, message = "{coverImageUrl.Size.experience}")
        String coverImageUrl,
        @NotNull(message = "{status.NotNull.experience}")
        ExperienceStatus status) {
}
