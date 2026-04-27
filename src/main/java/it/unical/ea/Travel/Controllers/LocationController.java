package it.unical.ea.Travel.Controllers;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import it.unical.ea.Travel.DTOs.location.LocationRequestDTO;
import it.unical.ea.Travel.DTOs.location.LocationResponseDTO;
import it.unical.ea.Travel.Services.LocationService;

@RestController
@RequestMapping("/location")
public class LocationController {

    private final LocationService locationService;

    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    @PostMapping
    public LocationResponseDTO saveLocation(@RequestBody LocationRequestDTO request) {
        return locationService.saveLocation(request);
    }

    @GetMapping("/{stringId}")
    public LocationResponseDTO getLocation(@PathVariable String stringId) {
        return locationService.getLocation(stringId);
    }

    @GetMapping
    public List<LocationResponseDTO> getLocations() {
        return locationService.getLocations();
    }

    @DeleteMapping("/{stringId}")
    public void deleteLocation(@PathVariable String stringId) {
        locationService.deleteLocation(stringId);
    }
}
