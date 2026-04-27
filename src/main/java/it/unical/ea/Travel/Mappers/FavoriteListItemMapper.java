package it.unical.ea.Travel.Mappers;

import it.unical.ea.Travel.DTOs.FavoriteListItemRequestDTO;
import it.unical.ea.Travel.DTOs.FavoriteListItemResponseDTO;
import it.unical.ea.Travel.Entities.Experience;
import it.unical.ea.Travel.Entities.FavoriteList;
import it.unical.ea.Travel.Entities.FavoriteListItem;

public final class FavoriteListItemMapper {

    private FavoriteListItemMapper() {
    }

    public static FavoriteListItem toEntity(
            FavoriteListItemRequestDTO request,
            FavoriteList favoriteList,
            Experience experience) {
        FavoriteListItem favoriteListItem = new FavoriteListItem();
        applyRequestToEntity(request, favoriteListItem, favoriteList, experience);
        return favoriteListItem;
    }

    public static void applyRequestToEntity(
            FavoriteListItemRequestDTO request,
            FavoriteListItem favoriteListItem,
            FavoriteList favoriteList,
            Experience experience) {
        favoriteListItem.setFavoriteList(favoriteList);
        favoriteListItem.setExperience(experience);
        favoriteListItem.setNotes(request.notes());
    }

    public static FavoriteListItemResponseDTO toResponseDTO(FavoriteListItem favoriteListItem) {
        return new FavoriteListItemResponseDTO(
                favoriteListItem.getId(),
                FavoriteListMapper.toResponseDTO(favoriteListItem.getFavoriteList()),
                ExperienceMapper.toResponseDTO(favoriteListItem.getExperience()),
                favoriteListItem.getAddedAt(),
                favoriteListItem.getNotes());
    }
}
