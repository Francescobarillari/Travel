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
    private String title;
    private String description;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    @Schema(format = "uuid")
    private String creatorId;
    @Schema(format = "uuid")
    private List<String> activityIds;
}
