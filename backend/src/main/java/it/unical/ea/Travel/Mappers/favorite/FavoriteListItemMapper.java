package it.unical.ea.Travel.Mappers.favorite;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import it.unical.ea.Travel.DTOs.favorite.FavoriteListItemRequestDTO;
import it.unical.ea.Travel.DTOs.favorite.FavoriteListItemResponseDTO;
import it.unical.ea.Travel.Entities.experience.Experience;
import it.unical.ea.Travel.Entities.favorite.FavoriteList;
import it.unical.ea.Travel.Entities.favorite.FavoriteListItem;
import it.unical.ea.Travel.Mappers.experience.ExperienceMapper;

@Mapper(componentModel = "spring", uses = { FavoriteListMapper.class, ExperienceMapper.class })
public interface FavoriteListItemMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "addedAt", ignore = true)
    @Mapping(target = "notes", source = "request.notes")
    FavoriteListItem toEntity(
            FavoriteListItemRequestDTO request,
            FavoriteList favoriteList,
            Experience experience);

    FavoriteListItemResponseDTO toResponseDTO(FavoriteListItem favoriteListItem);
}
