package it.unical.ea.Travel.Mappers.activity;

import it.unical.ea.dtos.activity.ActivityDto;
import it.unical.ea.Travel.Entities.activity.Activity;

public final class ActivityMapper {

    // Costruttore privato per impedire l'istanziamento della classe
    private ActivityMapper() {
    }

    // Metodo per convertire da Entity a DTO
    public static ActivityDto toDTO(Activity activity) {
        if (activity == null) {
            return null;
        }

        ActivityDto dto = new ActivityDto();
        dto.setId(activity.getId());
        dto.setTitle(activity.getTitle());
        dto.setDescription(activity.getDescription());
        dto.setCreatedAt(activity.getCreatedAt());

        return dto;
    }

    // Metodo per convertire da DTO a Entity (utile per quando si crea una nuova
    // attività)
    public static Activity toEntity(ActivityDto dto) {
        if (dto == null) {
            return null;
        }

        Activity entity = new Activity();
        entity.setId(dto.getId());
        entity.setTitle(dto.getTitle());
        entity.setDescription(dto.getDescription());

        return entity;
    }
}