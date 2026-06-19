package it.unical.ea.Travel.Services.activity;

import it.unical.ea.Travel.Entities.activity.Activity;
import it.unical.ea.Travel.Repositories.activity.ActivityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ActivityService {

    private final ActivityRepository activityRepository;

    @Autowired
    public ActivityService(ActivityRepository activityRepository) {
        this.activityRepository = activityRepository;
    }

    // Riceve tutte le attività dal database
    public List<Activity> getAllActivities() {
        return activityRepository.findAll();
    }

    // Salva una nuova attività nel database
    public Activity createActivity(Activity activity) {
        return activityRepository.save(activity);
    }

    // Cerca una singola attività tramite il suo ID
    public Activity getActivity(String stringId) {
        // Converte la stringa ricevuta dal controller in un UUID valido per il database
        UUID uuid = UUID.fromString(stringId);
        
        return activityRepository.findById(uuid)
                .orElseThrow(() -> new RuntimeException("Attività non trovata con ID: " + stringId));
    }

    // Rimuove un'attività (grazie al Soft Delete, imposterà automaticamente deleted_at)
    public void deleteActivity(String stringId) {
        UUID uuid = UUID.fromString(stringId);
        activityRepository.deleteById(uuid);
    }
}