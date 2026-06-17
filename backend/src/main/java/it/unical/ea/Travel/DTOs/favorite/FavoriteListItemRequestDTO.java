package it.unical.ea.Travel.DTOs.favorite;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record FavoriteListItemRequestDTO(
        @NotNull(message = "{favoriteListId.NotNull.favoriteListItem}")
        UUID favoriteListId,
        @NotNull(message = "{experienceId.NotNull.favoriteListItem}")
        UUID experienceId,
        @Size(max = 1000, message = "{notes.Size.favoriteListItem}")
        String notes) {
}
