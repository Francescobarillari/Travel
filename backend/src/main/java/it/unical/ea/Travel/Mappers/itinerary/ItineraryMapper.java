package it.unical.ea.Travel.Mappers.itinerary;

import it.unical.ea.Travel.DTOs.itinerary.ItineraryDto;
import it.unical.ea.Travel.Entities.activity.Activity;
import it.unical.ea.Travel.Entities.itinerary.Itinerary;
import it.unical.ea.Travel.Entities.user.User;
import it.unical.ea.Travel.Mappers.activity.ActivityMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = {ActivityMapper.class})
public interface ItineraryMapper {

    @Mapping(target = "creatorId", source = "creator.id")
    @Mapping(target = "imageUrl", ignore = true)
    ItineraryDto toDTO(Itinerary itinerary);

    @Mapping(target = "id", source = "dto.id")
    @Mapping(target = "title", source = "dto.title")
    @Mapping(target = "description", source = "dto.description")
    @Mapping(target = "startDateTime", source = "dto.startDateTime")
    @Mapping(target = "endDateTime", source = "dto.endDateTime")
    @Mapping(target = "creator", source = "creator")
    @Mapping(target = "activities", source = "activities")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "imagePath", ignore = true)
    Itinerary toEntity(ItineraryDto dto, User creator, List<Activity> activities);

}