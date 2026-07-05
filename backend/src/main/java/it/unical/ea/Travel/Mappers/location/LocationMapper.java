package it.unical.ea.Travel.Mappers.location;

import it.unical.ea.Travel.Entities.location.Location;
import it.unical.ea.dtos.location.LocationDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface LocationMapper {

    LocationDto toDto(Location location);

    Location toEntity(LocationDto dto);
}
