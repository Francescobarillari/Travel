package it.unical.ea.Travel.Mappers.experience.image;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import it.unical.ea.Travel.DTOs.experience.image.ExperienceImageRequestDTO;
import it.unical.ea.Travel.DTOs.experience.image.ExperienceImageResponseDTO;
import it.unical.ea.Travel.Entities.experience.Experience;
import it.unical.ea.Travel.Entities.experience.image.ExperienceImage;

@Mapper(componentModel = "spring")
public interface ExperienceImageMapper {

    @Mapping(target = "experienceId", source = "experience.id")
    ExperienceImageResponseDTO toResponseDTO(ExperienceImage experienceImage);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "experience", source = "experience")
    ExperienceImage toEntity(ExperienceImageRequestDTO request, Experience experience);
}
