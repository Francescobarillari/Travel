package it.unical.ea.Travel.Mappers.activity;

import it.unical.ea.Travel.DTOs.activity.ActivityDto;
import it.unical.ea.Travel.Entities.activity.Activity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ActivityMapper {

    ActivityDto toDTO(Activity activity);

    List<ActivityDto> toDTOList(List<Activity> activities);

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    Activity toEntity(ActivityDto dto);

    List<Activity> toEntityList(List<ActivityDto> dtos);
}