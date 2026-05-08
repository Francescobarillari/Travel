package it.unical.ea.Travel.Mappers.location;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import it.unical.ea.Travel.DTOs.location.LocationRequestDTO;
import it.unical.ea.Travel.DTOs.location.LocationResponseDTO;
import it.unical.ea.Travel.Entities.location.Location;

@Mapper(componentModel = "spring")
public interface LocationMapper {

    LocationResponseDTO toResponseDTO(Location location);

    @Mapping(target = "id", ignore = true)
    Location toEntity(LocationRequestDTO request);
}
