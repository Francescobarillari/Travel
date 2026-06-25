package it.unical.ea.Travel.DTOs.itinerary;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class CreateItineraryRequest {
    @Schema(example = "Weekend a Roma")
    private String title;
    @Schema(example = "Itinerario di 3 giorni alla scoperta della Città Eterna")
    private String description;
    @Schema(type = "string", format = "date-time", example = "2025-07-01T09:00:00")
    private LocalDateTime startDateTime;
    @Schema(type = "string", format = "date-time", example = "2025-07-03T18:00:00")
    private LocalDateTime endDateTime;
    @Schema(format = "uuid", example = "550e8400-e29b-41d4-a716-446655440000")
    private String creatorId;
    @Schema(format = "uuid", example = "[\"550e8400-e29b-41d4-a716-446655440000\"]")
    private List<String> activityIds;
}
