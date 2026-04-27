package it.unical.ea.Travel.DTOs;

import java.util.UUID;

public record FavoriteListItemRequestDTO(
        UUID favoriteListId,
        UUID experienceId,
        String notes) {
}
