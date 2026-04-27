package it.unical.ea.Travel.Mappers.favorite;

import it.unical.ea.Travel.DTOs.favorite.FavoriteListItemRequestDTO;
import it.unical.ea.Travel.DTOs.favorite.FavoriteListItemResponseDTO;
import it.unical.ea.Travel.Entities.experience.Experience;
import it.unical.ea.Travel.Entities.favorite.FavoriteList;
import it.unical.ea.Travel.Entities.favorite.FavoriteListItem;
import it.unical.ea.Travel.Mappers.experience.ExperienceMapper;

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
