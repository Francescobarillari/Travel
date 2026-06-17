package it.unical.ea.Travel.DTOs.favorite;

import java.time.LocalDateTime;
import java.util.UUID;

import it.unical.ea.Travel.DTOs.experience.ExperienceResponseDTO;

public record FavoriteListItemResponseDTO(
        UUID id,
        FavoriteListResponseDTO favoriteList,
        ExperienceResponseDTO experience,
        LocalDateTime addedAt,
        String notes) {
}
