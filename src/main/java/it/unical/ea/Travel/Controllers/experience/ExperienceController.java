package it.unical.ea.Travel.Controllers.experience;

import it.unical.ea.Travel.DTOs.experience.ExperienceRequestDTO;
import it.unical.ea.Travel.DTOs.experience.ExperienceResponseDTO;
import it.unical.ea.Travel.Services.experience.ExperienceService;
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
    public ExperienceResponseDTO saveExperience(@RequestBody ExperienceRequestDTO request) {
        return experienceService.saveExperience(request);
    }

    @GetMapping("/{stringId}")
    public ExperienceResponseDTO getExperience(@PathVariable String stringId) {
        return experienceService.getExperience(stringId);
    }

    @GetMapping
    public List<ExperienceResponseDTO> getExperiences(@RequestParam(required = false) String organizerId) {
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
