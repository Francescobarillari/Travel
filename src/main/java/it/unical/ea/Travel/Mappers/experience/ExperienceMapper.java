package it.unical.ea.Travel.Mappers.experience;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import it.unical.ea.Travel.DTOs.experience.ExperienceOrganizerDTO;
import it.unical.ea.Travel.DTOs.experience.ExperienceRequestDTO;
import it.unical.ea.Travel.DTOs.experience.ExperienceResponseDTO;
import it.unical.ea.Travel.Entities.experience.Experience;
import it.unical.ea.Travel.Entities.user.User;

@Mapper(componentModel = "spring")
public interface ExperienceMapper {

    ExperienceResponseDTO toResponseDTO(Experience experience);

    ExperienceOrganizerDTO toOrganizerDTO(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "organizer", source = "organizer")
    Experience toEntity(ExperienceRequestDTO request, User organizer);

    @AfterMapping
    default void applyDefaults(ExperienceRequestDTO request, @MappingTarget Experience experience) {
        if (request == null) {
            return;
        }
        if (request.currency() == null || request.currency().isBlank()) {
            experience.setCurrency("EUR");
        }
        if (request.status() == null) {
            experience.setStatus(Experience.ExperienceStatus.DRAFT);
        }
    }
}
