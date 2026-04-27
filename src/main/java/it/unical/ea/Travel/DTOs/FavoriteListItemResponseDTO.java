package it.unical.ea.Travel.DTOs;

import java.time.LocalDateTime;
import java.util.UUID;

public record FavoriteListItemResponseDTO(
        UUID id,
        FavoriteListResponseDTO favoriteList,
        ExperienceResponseDTO experience,
        LocalDateTime addedAt,
        String notes) {
}
