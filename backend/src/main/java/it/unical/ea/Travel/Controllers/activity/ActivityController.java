package it.unical.ea.Travel.Controllers.activity;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import it.unical.ea.Travel.DTOs.activity.ActivityDto;
import it.unical.ea.Travel.Entities.activity.Activity;
import it.unical.ea.Travel.Mappers.activity.ActivityMapper;
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
        Activity activity = ActivityMapper.toEntity(request);
        Activity savedActivity = activityService.createActivity(activity);
        return ActivityMapper.toDTO(savedActivity);
    }

    @GetMapping("/{stringId}")
    public ActivityDto getActivity(@PathVariable String stringId) {
        Activity activity = activityService.getActivity(stringId);
        return ActivityMapper.toDTO(activity);
    }

    @GetMapping
    public List<ActivityDto> getActivities() {
        return activityService.getAllActivities().stream()
                .map(ActivityMapper::toDTO)
                .collect(Collectors.toList());
    }

    @DeleteMapping("/{stringId}")
    public void deleteActivity(@PathVariable String stringId) {
        activityService.deleteActivity(stringId);
    }
}