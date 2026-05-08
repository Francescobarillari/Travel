package it.unical.ea.Travel.Mappers.experience.stop;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import it.unical.ea.Travel.DTOs.experience.stop.ExperienceStopRequestDTO;
import it.unical.ea.Travel.DTOs.experience.stop.ExperienceStopResponseDTO;
import it.unical.ea.Travel.Entities.experience.Experience;
import it.unical.ea.Travel.Entities.experience.stop.ExperienceStop;
import it.unical.ea.Travel.Entities.location.Location;

@Mapper(componentModel = "spring")
public interface ExperienceStopMapper {

    @Mapping(target = "experienceId", source = "experience.id")
    @Mapping(target = "locationId", source = "location.id")
    @Mapping(target = "locationName", source = "location.name")
    ExperienceStopResponseDTO toResponseDTO(ExperienceStop experienceStop);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "experience", source = "experience")
    @Mapping(target = "location", source = "location")
    @Mapping(target = "sequenceOrder", source = "request.sequenceOrder")
    @Mapping(target = "title", source = "request.title")
    @Mapping(target = "description", source = "request.description")
    @Mapping(target = "arrivalTime", source = "request.arrivalTime")
    @Mapping(target = "departureTime", source = "request.departureTime")
    @Mapping(target = "durationMinutes", source = "request.durationMinutes")
    ExperienceStop toEntity(
            ExperienceStopRequestDTO request,
            Experience experience,
            Location location);
}
