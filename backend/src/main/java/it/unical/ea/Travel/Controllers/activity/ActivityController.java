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

import it.unical.ea.Travel.DTOs.activity.ActivityDto;
import it.unical.ea.Travel.Services.activity.ActivityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/activity")
@Tag(name = "Activity", description = "Gestione delle attività turistiche")
public class ActivityController {

    private final ActivityService activityService;

    @Operation(summary = "Crea una nuova attività")
    @PostMapping
    public ActivityDto saveActivity(@Valid @RequestBody ActivityDto request) {
        
        return activityService.createActivity(request);
    }

    @Operation(summary = "Ottieni un'attività per ID")
    @GetMapping("/{stringId}")
    public ActivityDto getActivity(@Parameter(description = "ID dell'attività", schema = @Schema(format = "uuid"), example = "550e8400-e29b-41d4-a716-446655440000") @PathVariable String stringId) {
        return activityService.getActivity(stringId);
    }

    @Operation(summary = "Ottieni tutte le attività")
    @GetMapping
    public List<ActivityDto> getActivities() {
        
        return activityService.getAllActivities();
    }

    @Operation(summary = "Elimina un'attività (soft delete)")
    @DeleteMapping("/{stringId}")
    public void deleteActivity(@Parameter(description = "ID dell'attività", schema = @Schema(format = "uuid"), example = "550e8400-e29b-41d4-a716-446655440000") @PathVariable String stringId) {
        activityService.deleteActivity(stringId);
    }
}