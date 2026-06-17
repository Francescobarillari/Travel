package it.unical.ea.Travel.Controllers.experience.stop;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import it.unical.ea.Travel.DTOs.experience.stop.ExperienceStopRequestDTO;
import it.unical.ea.Travel.DTOs.experience.stop.ExperienceStopResponseDTO;
import it.unical.ea.Travel.Services.experience.stop.ExperienceStopService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/experience-stop")
public class ExperienceStopController {

    private final ExperienceStopService experienceStopService;

    public ExperienceStopController(ExperienceStopService experienceStopService) {
        this.experienceStopService = experienceStopService;
    }

    @PostMapping
    public ExperienceStopResponseDTO saveExperienceStop(@Valid @RequestBody ExperienceStopRequestDTO request) {
        return experienceStopService.saveExperienceStop(request);
    }

    @GetMapping("/{stringId}")
    public ExperienceStopResponseDTO getExperienceStop(@PathVariable String stringId) {
        return experienceStopService.getExperienceStop(stringId);
    }

    @GetMapping
    public List<ExperienceStopResponseDTO> getExperienceStops(@RequestParam(required = false) String experienceId) {
        return experienceStopService.getExperienceStops(experienceId);
    }

    @DeleteMapping("/{stringId}")
    public void deleteExperienceStop(@PathVariable String stringId) {
        experienceStopService.deleteExperienceStop(stringId);
    }
}
