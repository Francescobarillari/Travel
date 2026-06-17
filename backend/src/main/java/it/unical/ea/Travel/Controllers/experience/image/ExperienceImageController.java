package it.unical.ea.Travel.Controllers.experience.image;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import it.unical.ea.Travel.DTOs.experience.image.ExperienceImageRequestDTO;
import it.unical.ea.Travel.DTOs.experience.image.ExperienceImageResponseDTO;
import it.unical.ea.Travel.Services.experience.image.ExperienceImageService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/experience-image")
public class ExperienceImageController {

    private final ExperienceImageService experienceImageService;

    public ExperienceImageController(ExperienceImageService experienceImageService) {
        this.experienceImageService = experienceImageService;
    }

    @PostMapping
    public ExperienceImageResponseDTO saveExperienceImage(@Valid @RequestBody ExperienceImageRequestDTO request) {
        return experienceImageService.saveExperienceImage(request);
    }

    @GetMapping("/{stringId}")
    public ExperienceImageResponseDTO getExperienceImage(@PathVariable String stringId) {
        return experienceImageService.getExperienceImage(stringId);
    }

    @GetMapping
    public List<ExperienceImageResponseDTO> getExperienceImages(@RequestParam(required = false) String experienceId) {
        return experienceImageService.getExperienceImages(experienceId);
    }

    @DeleteMapping("/{stringId}")
    public void deleteExperienceImage(@PathVariable String stringId) {
        experienceImageService.deleteExperienceImage(stringId);
    }
}
