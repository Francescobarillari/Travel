package it.unical.ea.Travel.DTOs.itinerary;

import io.swagger.v3.oas.annotations.media.Schema;
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
    @Schema(format = "uri", example = "http://localhost:8080/itinerary/550e8400-e29b-41d4-a716-446655440000/image")
    private String imageUrl;
}
