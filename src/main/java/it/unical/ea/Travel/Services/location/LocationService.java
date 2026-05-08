package it.unical.ea.Travel.Services.location;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import it.unical.ea.Travel.DTOs.location.LocationRequestDTO;
import it.unical.ea.Travel.DTOs.location.LocationResponseDTO;
import it.unical.ea.Travel.Entities.location.Location;
import it.unical.ea.Travel.Mappers.location.LocationMapper;
import it.unical.ea.Travel.Repositories.location.LocationRepository;

@Service
public class LocationService {

    private final LocationRepository locationRepository;
    private final LocationMapper locationMapper;

    public LocationService(LocationRepository locationRepository, LocationMapper locationMapper) {
        this.locationRepository = locationRepository;
        this.locationMapper = locationMapper;
    }

    public LocationResponseDTO saveLocation(LocationRequestDTO request) {
        Location location = locationMapper.toEntity(request);
        Location savedLocation = locationRepository.save(location);
        return locationMapper.toResponseDTO(savedLocation);
    }

    public LocationResponseDTO getLocation(String stringId) {
        UUID uuid = UUID.fromString(stringId);
        Location location = locationRepository.findById(uuid)
                .orElseThrow(() -> new RuntimeException("Location non trovata"));
        return locationMapper.toResponseDTO(location);
    }

    public List<LocationResponseDTO> getLocations() {
        return locationRepository.findAll()
                .stream()
                .map(locationMapper::toResponseDTO)
                .toList();
    }

    public void updateLocation() {
        return; // da implementare
    }

    public void deleteLocation(String stringId) {
        UUID uuid = UUID.fromString(stringId);
        locationRepository.deleteById(uuid);
    }
}
