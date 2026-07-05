package it.unical.ea.Travel.Mappers.activity;

import it.unical.ea.dtos.activity.ActivityDto;
import it.unical.ea.Travel.Entities.activity.Activity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE)
public interface ActivityMapper {

    @Mapping(target = "currentParticipants", ignore = true)
    @Mapping(target = "organizer", expression = "java(activity.getOrganizer() != null ? (activity.getOrganizer().getCompanyName() != null ? activity.getOrganizer().getCompanyName() : activity.getOrganizer().getFirstName() + \" \" + activity.getOrganizer().getLastName()) : \"\")")
    ActivityDto toDTO(Activity activity);

    List<ActivityDto> toDTOList(List<Activity> activities);

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "organizer", ignore = true)
    @Mapping(target = "localita", ignore = true)
    Activity toEntity(ActivityDto dto);

    List<Activity> toEntityList(List<ActivityDto> dtos);
}