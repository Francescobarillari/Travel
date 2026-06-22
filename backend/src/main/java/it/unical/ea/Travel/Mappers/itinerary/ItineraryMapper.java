package it.unical.ea.Travel.Mappers.itinerary;

import it.unical.ea.Travel.DTOs.activity.ActivityDto;
import it.unical.ea.Travel.DTOs.itinerary.ItineraryDto;
import it.unical.ea.Travel.Entities.activity.Activity;
import it.unical.ea.Travel.Entities.itinerary.Itinerary;
import it.unical.ea.Travel.Entities.user.User;
import it.unical.ea.Travel.Mappers.activity.ActivityMapper;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class ItineraryMapper {

    // Costruttore privato per impedire l'istanziamento della classe
    private ItineraryMapper() {
    }

    // Metodo per convertire da Entity a DTO
    public static ItineraryDto toDTO(Itinerary itinerary) {
        if (itinerary == null) {
            return null;
        }

        ItineraryDto dto = new ItineraryDto();
        dto.setId(itinerary.getId());
        dto.setTitle(itinerary.getTitle());
        dto.setDescription(itinerary.getDescription());
        dto.setStartDateTime(itinerary.getStartDateTime());
        dto.setEndDateTime(itinerary.getEndDateTime());
        dto.setCreatedAt(itinerary.getCreatedAt());

        // Mappa il creatore come UUID
        if (itinerary.getCreator() != null) {
            dto.setCreatorId(itinerary.getCreator().getId());
        }

        // Mappa le activity come lista di ActivityDto
        if (itinerary.getActivities() != null) {
            List<ActivityDto> activityDtos = itinerary.getActivities().stream()
                    .map(ActivityMapper::toDTO)
                    .collect(Collectors.toList());
            dto.setActivities(activityDtos);
        } else {
            dto.setActivities(Collections.emptyList());
        }

        return dto;
    }

    // Metodo per convertire da DTO a Entity
    // Richiede il User creatore e la lista di Activity già risolte dal service
    public static Itinerary toEntity(ItineraryDto dto, User creator, List<Activity> activities) {
        if (dto == null) {
            return null;
        }

        Itinerary entity = new Itinerary();
        entity.setId(dto.getId());
        entity.setTitle(dto.getTitle());
        entity.setDescription(dto.getDescription());
        entity.setStartDateTime(dto.getStartDateTime());
        entity.setEndDateTime(dto.getEndDateTime());
        entity.setCreator(creator);
        entity.setActivities(activities != null ? activities : Collections.emptyList());

        return entity;
    }
}
