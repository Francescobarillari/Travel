package it.unical.ea.Travel.Mappers.experience.stop;

import it.unical.ea.Travel.DTOs.experience.stop.ExperienceStopRequestDTO;
import it.unical.ea.Travel.DTOs.experience.stop.ExperienceStopResponseDTO;
import it.unical.ea.Travel.Entities.experience.Experience;
import it.unical.ea.Travel.Entities.experience.stop.ExperienceStop;
import it.unical.ea.Travel.Entities.location.Location;

public final class ExperienceStopMapper {

    private ExperienceStopMapper() {
    }

    public static ExperienceStop toEntity(
            ExperienceStopRequestDTO request,
            Experience experience,
            Location location) {
        ExperienceStop experienceStop = new ExperienceStop();
        applyRequestToEntity(request, experienceStop, experience, location);
        return experienceStop;
    }

    public static void applyRequestToEntity(
            ExperienceStopRequestDTO request,
            ExperienceStop experienceStop,
            Experience experience,
            Location location) {
        experienceStop.setExperience(experience);
        experienceStop.setSequenceOrder(request.sequenceOrder());
        experienceStop.setTitle(request.title());
        experienceStop.setDescription(request.description());
        experienceStop.setLocation(location);
        experienceStop.setArrivalTime(request.arrivalTime());
        experienceStop.setDepartureTime(request.departureTime());
        experienceStop.setDurationMinutes(request.durationMinutes());
    }

    public static ExperienceStopResponseDTO toResponseDTO(ExperienceStop experienceStop) {
        return new ExperienceStopResponseDTO(
                experienceStop.getId(),
                experienceStop.getExperience() != null ? experienceStop.getExperience().getId() : null,
                experienceStop.getSequenceOrder(),
                experienceStop.getTitle(),
                experienceStop.getDescription(),
                experienceStop.getLocation() != null ? experienceStop.getLocation().getId() : null,
                experienceStop.getLocation() != null ? experienceStop.getLocation().getName() : null,
                experienceStop.getArrivalTime(),
                experienceStop.getDepartureTime(),
                experienceStop.getDurationMinutes());
    }
}
