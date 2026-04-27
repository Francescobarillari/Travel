package it.unical.ea.Travel.Mappers;

import it.unical.ea.Travel.DTOs.FavoriteListRequestDTO;
import it.unical.ea.Travel.DTOs.FavoriteListResponseDTO;
import it.unical.ea.Travel.Entities.FavoriteList;
import it.unical.ea.Travel.Entities.User;

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
