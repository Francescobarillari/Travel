package it.unical.ea.Travel.Controllers.itinerary;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

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
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.http.HttpStatus;

import it.unical.ea.dtos.itinerary.CreateItineraryRequest;
import it.unical.ea.dtos.itinerary.ItineraryDto;
import it.unical.ea.Travel.Entities.itinerary.Itinerary;
import it.unical.ea.Travel.Entities.activity.Activity;
import it.unical.ea.Travel.Mappers.itinerary.ItineraryMapper;
import it.unical.ea.Travel.Services.itinerary.ItineraryService;
import it.unical.ea.Travel.Services.activity.ActivityService;
import it.unical.ea.dtos.activity.ActivityDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/itinerary")
@Tag(name = "Itinerary", description = "Gestione degli itinerari di viaggio")
public class ItineraryController {

    private final ItineraryService itineraryService;
    private final ItineraryMapper itineraryMapper;
    private final ActivityService activityService;

    @Operation(summary = "Crea un nuovo itinerario", description = "Crea un itinerario associandolo a un creatore e opzionalmente a delle attività")
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
                request.getActivityIds());

        return toDTO(savedItinerary);
    }

    @Operation(summary = "Ottieni un itinerario per ID")
    @GetMapping("/{stringId}")
    public ItineraryDto getItinerary(
            @Parameter(description = "ID dell'itinerario", schema = @Schema(format = "uuid"), example = "550e8400-e29b-41d4-a716-446655440000") @PathVariable String stringId) {
        Itinerary itinerary = itineraryService.getItinerary(stringId);
        return toDTO(itinerary);
    }

    @Operation(summary = "Ottieni tutti gli itinerari")
    @GetMapping
    public List<ItineraryDto> getItineraries() {
        return itineraryService.getAllItineraries().stream()
                .map(this::toDTO)
                .toList();
    }

    @Operation(summary = "Ottieni gli itinerari di un creatore", description = "Restituisce tutti gli itinerari creati da un utente specifico")
    @GetMapping("/creator/{creatorId}")
    public List<ItineraryDto> getItinerariesByCreator(
            @Parameter(description = "ID del creatore", schema = @Schema(format = "uuid"), example = "550e8400-e29b-41d4-a716-446655440000") @PathVariable String creatorId) {
        return itineraryService.getItinerariesByCreator(creatorId).stream()
                .map(this::toDTO)
                .toList();
    }

    @Operation(summary = "Elimina un itinerario (soft delete)")
    @DeleteMapping("/{stringId}")
    public void deleteItinerary(
            @Parameter(description = "ID dell'itinerario", schema = @Schema(format = "uuid"), example = "550e8400-e29b-41d4-a716-446655440000") @PathVariable String stringId) {
        itineraryService.deleteItinerary(stringId);
    }

    // --- Endpoint immagine ---

    @Operation(summary = "Carica un'immagine per l'itinerario", description = "Accetta file JPEG, PNG o WebP. Se esiste già un'immagine, viene sostituita.")
    @PostMapping(value = "/{stringId}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ItineraryDto uploadImage(
            @Parameter(description = "ID dell'itinerario", schema = @Schema(format = "uuid"), example = "550e8400-e29b-41d4-a716-446655440000") @PathVariable String stringId,
            @Parameter(description = "File immagine (JPEG, PNG, WebP)", schema = @Schema(type = "string", format = "binary")) @RequestPart("file") MultipartFile file) {
        Itinerary updated = itineraryService.uploadImage(stringId, file);
        return toDTO(updated);
    }

    @Operation(summary = "Scarica l'immagine dell'itinerario", description = "Restituisce l'immagine inline con il content-type corretto. Endpoint pubblico.")
    @GetMapping("/{stringId}/image")
    public ResponseEntity<Resource> getImage(
            @Parameter(description = "ID dell'itinerario", schema = @Schema(format = "uuid"), example = "550e8400-e29b-41d4-a716-446655440000") @PathVariable String stringId)
            throws IOException {
        Resource resource = itineraryService.getImage(stringId);

        // Determina il content type dal file
        String contentType = Files.probeContentType(Path.of(resource.getURI()));
        if (contentType == null) {
            contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @Operation(summary = "Elimina l'immagine dell'itinerario")
    @DeleteMapping("/{stringId}/image")
    public ItineraryDto deleteImage(
            @Parameter(description = "ID dell'itinerario", schema = @Schema(format = "uuid"), example = "550e8400-e29b-41d4-a716-446655440000") @PathVariable String stringId) {
        Itinerary updated = itineraryService.deleteImage(stringId);
        return toDTO(updated);
    }

    @Operation(summary = "Prenota un itinerario", description = "Prenota l'itinerario ed iscrive l'utente autenticato a tutte le attività collegate")
    @PostMapping("/{stringId}/book")
    public ResponseEntity<Void> bookItinerary(
            @Parameter(description = "ID dell'itinerario", schema = @Schema(format = "uuid"), example = "550e8400-e29b-41d4-a716-446655440000") @PathVariable String stringId,
            @AuthenticationPrincipal Jwt jwt) {
        String email = jwt.getClaimAsString("email");
        itineraryService.bookItinerary(stringId, email);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "Cancella la prenotazione di un itinerario", description = "Annulla l'iscrizione all'itinerario ed a tutte le sue attività per l'utente autenticato")
    @DeleteMapping("/{stringId}/book")
    public ResponseEntity<Void> cancelItineraryBooking(
            @Parameter(description = "ID dell'itinerario", schema = @Schema(format = "uuid"), example = "550e8400-e29b-41d4-a716-446655440000") @PathVariable String stringId,
            @AuthenticationPrincipal Jwt jwt) {
        String email = jwt.getClaimAsString("email");
        itineraryService.cancelItineraryBooking(stringId, email);
        return ResponseEntity.noContent().build();
    }

    // --- Helper per costruire imageUrl ---

    private ItineraryDto toDTO(Itinerary itinerary) {
        ItineraryDto dto = itineraryMapper.toDTO(itinerary);

        if (dto.getActivities() != null && itinerary.getActivities() != null) {
            for (ActivityDto actDto : dto.getActivities()) {
                Activity act = itinerary.getActivities().stream()
                        .filter(a -> a.getId().equals(actDto.getId()))
                        .findFirst().orElse(null);
                if (act != null) {
                    actDto.setCurrentParticipants(activityService.calculateCurrentParticipants(act));
                }
            }
        }

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
