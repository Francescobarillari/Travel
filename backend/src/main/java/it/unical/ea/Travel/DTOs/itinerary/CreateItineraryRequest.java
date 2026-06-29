package it.unical.ea.Travel.DTOs.itinerary;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class CreateItineraryRequest {
    @NotBlank(message = "Il titolo dell'itinerario è obbligatorio")
    @Size(max = 150, message = "Il titolo non può superare i 150 caratteri")
    @Schema(example = "Weekend a Roma")
    private String title;

    @Size(max = 1000, message = "La descrizione non può superare i 1000 caratteri")
    @Schema(example = "Itinerario di 3 giorni alla scoperta della Città Eterna")
    private String description;

    @Schema(type = "string", format = "date-time", example = "2025-07-01T09:00:00")
    private LocalDateTime startDateTime;

    @Schema(type = "string", format = "date-time", example = "2025-07-03T18:00:00")
    private LocalDateTime endDateTime;

    @NotBlank(message = "L'ID del creatore è obbligatorio")
    @Schema(format = "uuid", example = "550e8400-e29b-41d4-a716-446655440000")
    private String creatorId;

    @Schema(format = "uuid", example = "[\"550e8400-e29b-41d4-a716-446655440000\"]")
    private List<String> activityIds;
}
