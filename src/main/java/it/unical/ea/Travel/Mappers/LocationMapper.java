package it.unical.ea.Travel.Mappers;

import it.unical.ea.Travel.DTOs.location.LocationRequestDTO;
import it.unical.ea.Travel.DTOs.location.LocationResponseDTO;
import it.unical.ea.Travel.Entities.Location;

public final class LocationMapper {

    private LocationMapper() {
    }

    public static Location toEntity(LocationRequestDTO request) {
        Location location = new Location();
        applyRequestToEntity(request, location);
        return location;
    }

    public static void applyRequestToEntity(LocationRequestDTO request, Location location) {
        location.setName(request.name());
        location.setAddress(request.address());
        location.setCity(request.city());
        location.setCountry(request.country());
        location.setLatitude(request.latitude());
        location.setLongitude(request.longitude());
    }

    public static LocationResponseDTO toResponseDTO(Location location) {
        return new LocationResponseDTO(
                location.getId(),
                location.getName(),
                location.getAddress(),
                location.getCity(),
                location.getCountry(),
                location.getLatitude(),
                location.getLongitude());
    }
}
