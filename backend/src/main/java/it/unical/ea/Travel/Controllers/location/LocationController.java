package it.unical.ea.Travel.Controllers.location;

import it.unical.ea.Travel.Services.location.LocationService;
import it.unical.ea.dtos.location.LocationDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/location")
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;

    @GetMapping("/search")
    public ResponseEntity<Page<LocationDto>> searchLocation(
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "false") boolean includeExternal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<LocationDto> results = locationService.searchLocation(query, includeExternal, page, size);
        results.forEach(this::enrichImageUrl);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LocationDto> getLocationById(@PathVariable UUID id) {
        return locationService.getLocationById(id)
                .map(this::enrichImageUrl)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    private LocationDto enrichImageUrl(LocationDto dto) {
        if (dto == null) {
            return null;
        }
        if (dto.getImageUrl() != null && !dto.getImageUrl().isEmpty() && !dto.getImageUrl().startsWith("http")) {
            String filename = dto.getImageUrl().substring(dto.getImageUrl().lastIndexOf("/") + 1);
            String url = org.springframework.web.servlet.support.ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/activity/images/")
                    .path(filename)
                    .toUriString();
            dto.setImageUrl(url);
        }
        return dto;
    }
}
