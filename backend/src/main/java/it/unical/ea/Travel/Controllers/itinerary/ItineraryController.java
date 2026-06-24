package it.unical.ea.Travel.Controllers.itinerary;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import io.swagger.v3.oas.annotations.Parameter;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import it.unical.ea.Travel.DTOs.itinerary.CreateItineraryRequest;
import it.unical.ea.Travel.DTOs.itinerary.ItineraryDto;
import it.unical.ea.Travel.Entities.itinerary.Itinerary;
import it.unical.ea.Travel.Mappers.itinerary.ItineraryMapper;
import it.unical.ea.Travel.Services.itinerary.ItineraryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/itinerary")
public class ItineraryController {

    private final ItineraryService itineraryService;
    private final ItineraryMapper itineraryMapper;

    @PostMapping
    public ItineraryDto saveItinerary(@Valid @RequestBody CreateItineraryRequest request) {
        Itinerary itinerary = new Itinerary();
        itinerary.setTitle(request.getTitle());
        itinerary.setDescription(request.getDescription());
        itinerary.setStartDateTime(request.getStartDateTime());
        itinerary.setEndDateTime(request.getEndDateTime());

        Itinerary savedItinerary = itineraryService.createItinerary(
                itinerary,
                request.getCreatorId(),
                request.getActivityIds()
        );

        return toDTO(savedItinerary);
    }

    @GetMapping("/{stringId}")
    public ItineraryDto getItinerary(@Parameter(example = "550e8400-e29b-41d4-a716-446655440000") @PathVariable String stringId) {
        Itinerary itinerary = itineraryService.getItinerary(stringId);
        return toDTO(itinerary);
    }

    @GetMapping
    public List<ItineraryDto> getItineraries() {
        return itineraryService.getAllItineraries().stream()
                .map(this::toDTO)
                .toList();
    }

    @GetMapping("/creator/{creatorId}")
    public List<ItineraryDto> getItinerariesByCreator(@Parameter(example = "550e8400-e29b-41d4-a716-446655440000") @PathVariable String creatorId) {
        return itineraryService.getItinerariesByCreator(creatorId).stream()
                .map(this::toDTO)
                .toList();
    }

    @DeleteMapping("/{stringId}")
    public void deleteItinerary(@Parameter(example = "550e8400-e29b-41d4-a716-446655440000") @PathVariable String stringId) {
        itineraryService.deleteItinerary(stringId);
    }

    // --- Endpoint immagine ---

    @PostMapping("/{stringId}/image")
    public ItineraryDto uploadImage(@Parameter(example = "550e8400-e29b-41d4-a716-446655440000") @PathVariable String stringId,
                                    @RequestParam("file") MultipartFile file) {
        Itinerary updated = itineraryService.uploadImage(stringId, file);
        return toDTO(updated);
    }

    @GetMapping("/{stringId}/image")
    public ResponseEntity<Resource> getImage(@Parameter(example = "550e8400-e29b-41d4-a716-446655440000") @PathVariable String stringId) throws IOException {
        Resource resource = itineraryService.getImage(stringId);

        // Determina il content type dal file
        String contentType = Files.probeContentType(Path.of(resource.getFilename()));
        if (contentType == null) {
            contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @DeleteMapping("/{stringId}/image")
    public ItineraryDto deleteImage(@Parameter(example = "550e8400-e29b-41d4-a716-446655440000") @PathVariable String stringId) {
        Itinerary updated = itineraryService.deleteImage(stringId);
        return toDTO(updated);
    }

    // --- Helper per costruire imageUrl ---

    private ItineraryDto toDTO(Itinerary itinerary) {
        ItineraryDto dto = itineraryMapper.toDTO(itinerary);

        if (itinerary.getImagePath() != null) {
            String imageUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/itinerary/")
                    .path(itinerary.getId().toString())
                    .path("/image")
                    .toUriString();
            dto.setImageUrl(imageUrl);
        }

        return dto;
    }
}

