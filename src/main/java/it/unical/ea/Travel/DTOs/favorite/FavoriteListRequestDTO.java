package it.unical.ea.Travel.DTOs.favorite;

import java.util.UUID;

import it.unical.ea.Travel.Entities.favorite.FavoriteList.Visibility;

public record FavoriteListRequestDTO(
        UUID ownerId,
        String name,
        String description,
        Visibility visibility) {
}
