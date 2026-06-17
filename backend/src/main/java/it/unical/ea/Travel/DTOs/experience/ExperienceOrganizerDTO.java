package it.unical.ea.Travel.DTOs.experience;

import java.util.UUID;

public record ExperienceOrganizerDTO(
        UUID id,
        String firstName,
        String lastName,
        String avatarUrl) {
}
