package it.unical.ea.Travel.Services;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import it.unical.ea.Travel.DTOs.location.LocationRequestDTO;
import it.unical.ea.Travel.DTOs.location.LocationResponseDTO;
import it.unical.ea.Travel.Entities.Location;
import it.unical.ea.Travel.Mappers.LocationMapper;
import it.unical.ea.Travel.Repositories.LocationRepository;

@Service
public class LocationService {

    private final LocationRepository locationRepository;

    public LocationService(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    public LocationResponseDTO saveLocation(LocationRequestDTO request) {
        Location location = LocationMapper.toEntity(request);
        Location savedLocation = locationRepository.save(location);
        return LocationMapper.toResponseDTO(savedLocation);
    }

    public LocationResponseDTO getLocation(String stringId) {
        UUID uuid = UUID.fromString(stringId);
        Location location = locationRepository.findById(uuid)
                .orElseThrow(() -> new RuntimeException("Location non trovata"));
        return LocationMapper.toResponseDTO(location);
    }

    public List<LocationResponseDTO> getLocations() {
        return locationRepository.findAll()
                .stream()
                .map(LocationMapper::toResponseDTO)
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
