package it.unical.ea.Travel.DTOs.favorite;

import java.util.UUID;

import it.unical.ea.Travel.Entities.favorite.FavoriteList.Visibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record FavoriteListRequestDTO(
        @NotNull(message = "{ownerId.NotNull.favoriteList}")
        UUID ownerId,
        @NotBlank(message = "{name.NotBlank.favoriteList}")
        String name,
        @Size(max = 1000, message = "{description.Size.favoriteList}")
        String description,
        @NotNull(message = "{visibility.NotNull.favoriteList}")
        Visibility visibility) {
}
