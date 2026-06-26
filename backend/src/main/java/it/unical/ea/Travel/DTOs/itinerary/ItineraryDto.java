package it.unical.ea.Travel.DTOs.itinerary;

import io.swagger.v3.oas.annotations.media.Schema;
import it.unical.ea.dtos.activity.ActivityDto;
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
    @Schema(format = "uuid", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;
    @Schema(example = "Weekend a Roma")
    private String title;
    @Schema(example = "Itinerario di 3 giorni alla scoperta della Città Eterna")
    private String description;
    @Schema(type = "string", format = "date-time", example = "2025-07-01T09:00:00")
    private LocalDateTime startDateTime;
    @Schema(type = "string", format = "date-time", example = "2025-07-03T18:00:00")
    private LocalDateTime endDateTime;
    @Schema(format = "uuid", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID creatorId;
    private List<ActivityDto> activities;
    @Schema(type = "string", format = "date-time", example = "2025-06-25T10:30:00")
    private LocalDateTime createdAt;
    @Schema(format = "uri", example = "http://localhost:8080/itinerary/550e8400-e29b-41d4-a716-446655440000/image")
    private String imageUrl;
}
