package it.unical.ea.Travel.Mappers.favorite;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import it.unical.ea.Travel.DTOs.favorite.FavoriteListRequestDTO;
import it.unical.ea.Travel.DTOs.favorite.FavoriteListResponseDTO;
import it.unical.ea.Travel.Entities.favorite.FavoriteList;
import it.unical.ea.Travel.Entities.user.User;

@Mapper(componentModel = "spring")
public interface FavoriteListMapper {

    @Mapping(target = "ownerId", source = "owner.id")
    FavoriteListResponseDTO toResponseDTO(FavoriteList favoriteList);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "owner", source = "owner")
    FavoriteList toEntity(FavoriteListRequestDTO request, User owner);

    @AfterMapping
    default void applyDefaults(FavoriteListRequestDTO request, @MappingTarget FavoriteList favoriteList) {
        if (request.visibility() == null) {
            favoriteList.setVisibility(FavoriteList.Visibility.PRIVATE);
        }
    }
}
