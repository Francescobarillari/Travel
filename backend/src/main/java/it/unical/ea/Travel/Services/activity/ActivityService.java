package it.unical.ea.Travel.Services.activity;

import it.unical.ea.dtos.activity.ActivityDto;
import it.unical.ea.Travel.Entities.activity.Activity;
import it.unical.ea.Travel.Mappers.activity.ActivityMapper;
import it.unical.ea.Travel.Repositories.activity.ActivityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ActivityService {

    private final ActivityRepository activityRepository;
    private final ActivityMapper activityMapper;

    public List<ActivityDto> getAllActivities() {
        List<Activity> activities = activityRepository.findAll();
        
        return activityMapper.toDTOList(activities); 
    }

    public ActivityDto createActivity(ActivityDto activityDto) {
        
        Activity activity = activityMapper.toEntity(activityDto);
        
        
        Activity savedActivity = activityRepository.save(activity);
        
        
        return activityMapper.toDTO(savedActivity);
    }

    
    public ActivityDto getActivity(String stringId) {
        UUID uuid = UUID.fromString(stringId);
        
        Activity activity = activityRepository.findById(uuid)
                .orElseThrow(() -> new RuntimeException("Attività non trovata con ID: " + stringId));
                
        return activityMapper.toDTO(activity);
    }

    
    public void deleteActivity(String stringId) {
        UUID uuid = UUID.fromString(stringId);
        activityRepository.deleteById(uuid);
    }
}