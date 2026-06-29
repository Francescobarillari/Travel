package it.unical.ea.Travel.Controllers.activity;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import it.unical.ea.dtos.activity.ActivityDto;
import it.unical.ea.Travel.Services.activity.ActivityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@RestController
@RequiredArgsConstructor
@RequestMapping("/activity")
@Tag(name = "Activity", description = "Gestione delle attività turistiche")
public class ActivityController {

    private final ActivityService activityService;

    @Operation(summary = "Crea una nuova attività")
    @PostMapping
    public ActivityDto saveActivity(@Valid @RequestBody ActivityDto request) {
        return enrichImageUrls(activityService.createActivity(request));
    }

    @Operation(summary = "Ottieni un'attività per ID")
    @GetMapping("/{stringId}")
    public ActivityDto getActivity(@Parameter(description = "ID dell'attività", schema = @Schema(format = "uuid"), example = "550e8400-e29b-41d4-a716-446655440000") @PathVariable String stringId) {
        return enrichImageUrls(activityService.getActivity(stringId));
    }

    @Operation(summary = "Ottieni tutte le attività")
    @GetMapping
    public List<ActivityDto> getActivities() {
        return activityService.getAllActivities().stream()
                .map(this::enrichImageUrls)
                .toList();
    }

    @Operation(summary = "Elimina un'attività (soft delete)")
    @DeleteMapping("/{stringId}")
    public void deleteActivity(@Parameter(description = "ID dell'attività", schema = @Schema(format = "uuid"), example = "550e8400-e29b-41d4-a716-446655440000") @PathVariable String stringId) {
        activityService.deleteActivity(stringId);
    }

    // --- Endpoints Immagini ---

    @Operation(summary = "Carica immagini per l'attività", description = "Accetta una lista di file immagine (JPEG, PNG, WebP)")
    @PostMapping(value = "/{stringId}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ActivityDto uploadImages(
            @Parameter(description = "ID dell'attività", schema = @Schema(format = "uuid"), example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable String stringId,
            @RequestPart("files") MultipartFile[] files) {
        ActivityDto updated = activityService.uploadImages(stringId, files);
        return enrichImageUrls(updated);
    }

    @Operation(summary = "Scarica un'immagine dell'attività", description = "Restituisce l'immagine inline. Endpoint pubblico.")
    @GetMapping("/images/{filename}")
    public ResponseEntity<Resource> getImage(@PathVariable String filename) throws IOException {
        Resource resource = activityService.loadImage(filename);

        String contentType = Files.probeContentType(Path.of(resource.getFilename()));
        if (contentType == null) {
            contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @Operation(summary = "Elimina un'immagine specifica dell'attività")
    @DeleteMapping("/{stringId}/images/{filename}")
    public ActivityDto deleteImage(
            @Parameter(description = "ID dell'attività", schema = @Schema(format = "uuid"), example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable String stringId,
            @PathVariable String filename) {
        ActivityDto updated = activityService.deleteImage(stringId, filename);
        return enrichImageUrls(updated);
    }

    // --- Helpers per arricchire URL ---

    private ActivityDto enrichImageUrls(ActivityDto dto) {
        if (dto == null) {
            return null;
        }
        if (dto.getImages() != null && !dto.getImages().isEmpty()) {
            List<String> absoluteUrls = dto.getImages().stream()
                    .map(path -> {
                        if (path.startsWith("http")) return path;
                        String filename = path.substring(path.lastIndexOf("/") + 1);
                        return ServletUriComponentsBuilder.fromCurrentContextPath()
                                .path("/activity/images/")
                                .path(filename)
                                .toUriString();
                    })
                    .toList();
            dto.setImages(absoluteUrls);
        }
        return dto;
    }
}