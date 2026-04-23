package it.unical.ea.Travel.Controllers;

import it.unical.ea.Travel.Entities.Experience;
import it.unical.ea.Travel.Services.ExperienceService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/experience")
public class ExperienceController {

    private final ExperienceService experienceService;

    public ExperienceController(ExperienceService experienceService) {
        this.experienceService = experienceService;
    }

    @PostMapping
    public Experience saveExperience(@RequestBody Experience experience) {
        return experienceService.saveExperience(experience);
    }

    @GetMapping("/{stringId}")
    public Experience getExperience(@PathVariable String stringId) {
        return experienceService.getExperience(stringId);
    }

    @GetMapping
    public List<Experience> getExperiences(@RequestParam(required = false) String organizerId) {
        if (organizerId != null && !organizerId.isBlank()) {
            return experienceService.getExperiencesByOrganizer(organizerId);
        }
        return experienceService.getExperiences();
    }

    @DeleteMapping("/{stringId}")
    public void deleteExperience(@PathVariable String stringId) {
        experienceService.deleteExperience(stringId);
    }
}
