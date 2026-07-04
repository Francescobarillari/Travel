package it.unical.ea.Travel.Mappers.trip;

import it.unical.ea.Travel.Entities.trip.Trip;
import it.unical.ea.dtos.trip.TripDto;
import org.springframework.stereotype.Component;

@Component
public class TripMapper {

    public TripDto toDto(Trip trip) {
        if (trip == null) {
            return null;
        }

        TripDto dto = new TripDto();
        dto.setId(trip.getId());
        dto.setTitle(trip.getTitle());
        dto.setLocation(trip.getLocation());
        dto.setDescription(trip.getDescription());
        dto.setImageUrl(trip.getImageUrl());
        dto.setPrice(trip.getPrice());
        dto.setDuration(trip.getDuration());
        
        if (trip.getOrganizer() != null) {
            dto.setOrganizer(trip.getOrganizer().getFirstName() + " " + trip.getOrganizer().getLastName());
        }

        return dto;
    }

    public Trip toEntity(TripDto dto) {
        if (dto == null) {
            return null;
        }

        Trip trip = new Trip();
        trip.setId(dto.getId());
        trip.setTitle(dto.getTitle());
        trip.setLocation(dto.getLocation());
        trip.setDescription(dto.getDescription());
        trip.setImageUrl(dto.getImageUrl());
        trip.setPrice(dto.getPrice());
        trip.setDuration(dto.getDuration());

        return trip;
    }
}
