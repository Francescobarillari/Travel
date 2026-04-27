package it.unical.ea.Travel.Mappers.favorite;

import it.unical.ea.Travel.DTOs.favorite.FavoriteListRequestDTO;
import it.unical.ea.Travel.DTOs.favorite.FavoriteListResponseDTO;
import it.unical.ea.Travel.Entities.favorite.FavoriteList;
import it.unical.ea.Travel.Entities.user.User;

public final class FavoriteListMapper {

    private FavoriteListMapper() {
    }

    public static FavoriteList toEntity(FavoriteListRequestDTO request, User owner) {
        FavoriteList favoriteList = new FavoriteList();
        applyRequestToEntity(request, favoriteList, owner);
        return favoriteList;
    }

    public static void applyRequestToEntity(FavoriteListRequestDTO request, FavoriteList favoriteList, User owner) {
        favoriteList.setOwner(owner);
        favoriteList.setName(request.name());
        favoriteList.setDescription(request.description());
        favoriteList.setVisibility(request.visibility() == null ? FavoriteList.Visibility.PRIVATE : request.visibility());
    }

    public static FavoriteListResponseDTO toResponseDTO(FavoriteList favoriteList) {
        return new FavoriteListResponseDTO(
                favoriteList.getId(),
                favoriteList.getOwner().getId(),
                favoriteList.getName(),
                favoriteList.getDescription(),
                favoriteList.getVisibility(),
                favoriteList.getCreatedAt(),
                favoriteList.getUpdatedAt());
    }
}
