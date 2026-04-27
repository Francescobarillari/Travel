package it.unical.ea.Travel.Mappers;

import it.unical.ea.Travel.DTOs.ExperienceOrganizerDTO;
import it.unical.ea.Travel.DTOs.ExperienceRequestDTO;
import it.unical.ea.Travel.DTOs.ExperienceResponseDTO;
import it.unical.ea.Travel.Entities.Experience;
import it.unical.ea.Travel.Entities.User;

public final class ExperienceMapper {

    private ExperienceMapper() {
    }

    public static Experience toEntity(ExperienceRequestDTO request, User organizer) {
        Experience experience = new Experience();
        applyRequestToEntity(request, experience, organizer);
        return experience;
    }

    public static void applyRequestToEntity(ExperienceRequestDTO request, Experience experience, User organizer) {
        experience.setOrganizer(organizer);
        experience.setType(request.type());
        experience.setTitle(request.title());
        experience.setDescription(request.description());
        experience.setBasePrice(request.basePrice());
        experience.setCurrency(request.currency() == null || request.currency().isBlank() ? "EUR" : request.currency());
        experience.setMaxParticipants(request.maxParticipants());
        experience.setMinParticipants(request.minParticipants());
        experience.setDurationMinutes(request.durationMinutes());
        experience.setStartDate(request.startDate());
        experience.setEndDate(request.endDate());
        experience.setCoverImageUrl(request.coverImageUrl());
        experience.setStatus(request.status() == null ? Experience.ExperienceStatus.DRAFT : request.status());
    }

    public static ExperienceResponseDTO toResponseDTO(Experience experience) {
        User organizer = experience.getOrganizer();
        ExperienceOrganizerDTO organizerDTO = new ExperienceOrganizerDTO(
                organizer.getId(),
                organizer.getFirstName(),
                organizer.getLastName(),
                organizer.getAvatarUrl());

        return new ExperienceResponseDTO(
                experience.getId(),
                organizerDTO,
                experience.getType(),
                experience.getTitle(),
                experience.getDescription(),
                experience.getBasePrice(),
                experience.getCurrency(),
                experience.getMaxParticipants(),
                experience.getMinParticipants(),
                experience.getDurationMinutes(),
                experience.getStartDate(),
                experience.getEndDate(),
                experience.getCoverImageUrl(),
                experience.getStatus(),
                experience.getCreatedAt(),
                experience.getUpdatedAt());
    }
}
