package it.unical.ea.Travel.DTOs.itinerary;

import it.unical.ea.Travel.DTOs.activity.ActivityDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class ItineraryDto {
    private UUID id;
    private String title;
    private String description;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private UUID creatorId;
    private List<ActivityDto> activities;
    private LocalDateTime createdAt;
}
