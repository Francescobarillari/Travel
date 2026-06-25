package it.unical.ea.Travel.Controllers.activity;

import java.util.List;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;

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
public class ActivityController {

    private final ActivityService activityService;

    @PostMapping
    public ActivityDto saveActivity(@Valid @RequestBody ActivityDto request) {
        
        return activityService.createActivity(request);
    }

    @GetMapping("/{stringId}")
    public ActivityDto getActivity(@Parameter(schema = @Schema(format = "uuid")) @PathVariable String stringId) {
        
        return activityService.getActivity(stringId);
    }

    @GetMapping
    public List<ActivityDto> getActivities() {
        
        return activityService.getAllActivities();
    }

    @DeleteMapping("/{stringId}")
    public void deleteActivity(@Parameter(schema = @Schema(format = "uuid")) @PathVariable String stringId) {
        activityService.deleteActivity(stringId);
    }
}