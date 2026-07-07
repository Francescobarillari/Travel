package it.unical.ea.Travel.Mappers.activity;

import it.unical.ea.dtos.activity.ActivityDto;
import it.unical.ea.Travel.Entities.activity.Activity;
import it.unical.ea.Travel.Entities.activity.ActivityTemplate;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE)
public interface ActivityMapper {

    @Mapping(target = "currentParticipants", ignore = true)
    @Mapping(target = "templateId", source = "template.id")
    @Mapping(target = "name", source = "template.name")
    @Mapping(target = "description", source = "template.description")
    @Mapping(target = "location", source = "template.location")
    @Mapping(target = "images", source = "template.images")
    @Mapping(target = "tags", source = "template.tags")
    @Mapping(target = "organizer", expression = "java(activity.getTemplate() != null && activity.getTemplate().getOrganizer() != null ? (activity.getTemplate().getOrganizer().getCompanyName() != null ? activity.getTemplate().getOrganizer().getCompanyName() : activity.getTemplate().getOrganizer().getFirstName() + \" \" + activity.getTemplate().getOrganizer().getLastName()) : \"\")")
    ActivityDto toDTO(Activity activity);

    List<ActivityDto> toDTOList(List<Activity> activities);

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "template", ignore = true)
    Activity toEntity(ActivityDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "organizer", ignore = true)
    @Mapping(target = "locationEntity", ignore = true)
    ActivityTemplate toTemplateEntity(ActivityDto dto);

    List<Activity> toEntityList(List<ActivityDto> dtos);
}