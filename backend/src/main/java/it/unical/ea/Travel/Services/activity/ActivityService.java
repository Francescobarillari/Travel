package it.unical.ea.Travel.Services.activity;

import it.unical.ea.dtos.activity.ActivityDto;
import it.unical.ea.Travel.Entities.activity.Activity;
import it.unical.ea.Travel.Exception.ApiException;
import it.unical.ea.Travel.Mappers.activity.ActivityMapper;
import it.unical.ea.Travel.Repositories.activity.ActivityRepository;
import it.unical.ea.Travel.Services.storage.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ActivityService {

    private static final String ACTIVITIES_SUBDIR = "activities";

    private final ActivityRepository activityRepository;
    private final ActivityMapper activityMapper;
    private final FileStorageService fileStorageService;

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
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "activity.notFound"));
                
        return activityMapper.toDTO(activity);
    }

    
    public void deleteActivity(String stringId) {
        UUID uuid = UUID.fromString(stringId);
        activityRepository.deleteById(uuid);
    }

    // Carica una o più immagini per l'attività specificata
    @org.springframework.transaction.annotation.Transactional
    public ActivityDto uploadImages(String activityId, MultipartFile[] files) {
        UUID uuid = UUID.fromString(activityId);
        Activity activity = activityRepository.findById(uuid)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "activity.notFound"));

        if (activity.getImages() == null) {
            activity.setImages(new java.util.ArrayList<>());
        }

        for (MultipartFile file : files) {
            String relativePath = fileStorageService.store(file, ACTIVITIES_SUBDIR);
            activity.getImages().add(relativePath);
        }

        Activity saved = activityRepository.save(activity);
        return activityMapper.toDTO(saved);
    }

    // Carica l'immagine dell'attività come risorsa
    public Resource loadImage(String filename) {
        return fileStorageService.load(ACTIVITIES_SUBDIR + "/" + filename);
    }

    // Elimina una specifica immagine associata all'attività
    @org.springframework.transaction.annotation.Transactional
    public ActivityDto deleteImage(String activityId, String filename) {
        UUID uuid = UUID.fromString(activityId);
        Activity activity = activityRepository.findById(uuid)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "activity.notFound"));

        String targetPath = ACTIVITIES_SUBDIR + "/" + filename;
        if (activity.getImages() != null && activity.getImages().contains(targetPath)) {
            fileStorageService.delete(targetPath);
            activity.getImages().remove(targetPath);
            Activity saved = activityRepository.save(activity);
            return activityMapper.toDTO(saved);
        } else {
            throw new ApiException(HttpStatus.NOT_FOUND, "activity.imageNotFound");
        }
    }
}