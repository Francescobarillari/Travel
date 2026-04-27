package it.unical.ea.Travel.DTOs;

import java.time.LocalDateTime;
import java.util.UUID;

import it.unical.ea.Travel.Entities.FavoriteList.Visibility;

public record FavoriteListResponseDTO(
        UUID id,
        UUID ownerId,
        String name,
        String description,
        Visibility visibility,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
