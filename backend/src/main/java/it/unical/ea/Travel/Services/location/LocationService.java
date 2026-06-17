package it.unical.ea.Travel.Services.location;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import it.unical.ea.Travel.DTOs.location.LocationRequestDTO;
import it.unical.ea.Travel.DTOs.location.LocationResponseDTO;
import it.unical.ea.Travel.Entities.location.Location;
import it.unical.ea.Travel.Mappers.location.LocationMapper;
import it.unical.ea.Travel.Repositories.location.LocationRepository;

@Service
public class LocationService {

    private static final Logger logger = LoggerFactory.getLogger(LocationService.class);

    private final LocationRepository locationRepository;
    private final LocationMapper locationMapper;

    public LocationService(LocationRepository locationRepository, LocationMapper locationMapper) {
        this.locationRepository = locationRepository;
        this.locationMapper = locationMapper;
    }

    public LocationResponseDTO saveLocation(LocationRequestDTO request) {
        logger.info("Creating location name='{}' city='{}' country='{}'",
                request.name(), request.city(), request.country());
        Location location = locationMapper.toEntity(request);
        Location savedLocation = locationRepository.save(location);
        logger.info("Created location id={} name='{}'",
                savedLocation.getId(), savedLocation.getName());
        return locationMapper.toResponseDTO(savedLocation);
    }

    public LocationResponseDTO getLocation(String stringId) {
        logger.debug("Fetching location id={}", stringId);
        UUID uuid = UUID.fromString(stringId);
        Location location = locationRepository.findById(uuid)
                .orElseThrow(() -> {
                    logger.warn("Location not found for id={}", uuid);
                    return new RuntimeException("Location non trovata");
                });
        return locationMapper.toResponseDTO(location);
    }

    public List<LocationResponseDTO> getLocations() {
        logger.debug("Fetching all locations");
        List<LocationResponseDTO> locations = locationRepository.findAll()
                .stream()
                .map(locationMapper::toResponseDTO)
                .toList();
        logger.debug("Fetched {} locations", locations.size());
        return locations;
    }

    public void updateLocation() {
        return; // da implementare
    }

    public void deleteLocation(String stringId) {
        logger.info("Deleting location id={}", stringId);
        UUID uuid = UUID.fromString(stringId);
        locationRepository.deleteById(uuid);
        logger.info("Deletion request completed for location id={}", stringId);
    }
}
