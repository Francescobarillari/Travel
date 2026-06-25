package it.unical.ea.Travel.DTOs.activity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class ActivityDto {
    @Schema(format = "uuid", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Schema(example = "Visita al Colosseo")
    private String name;

    @Schema(example = "Una splendida visita guidata all'anfiteatro Flavio")
    private String description;

    @Schema(example = "Roma, Colosseo")
    private String location;

    @Schema(type = "string", format = "date-time", example = "2025-07-01T10:00:00")
    private LocalDateTime startTime;

    @Schema(type = "string", format = "date-time", example = "2025-07-01T12:00:00")
    private LocalDateTime endTime;

    @Schema(example = "20")
    private Integer participants;

    @Schema(example = "15.50")
    private BigDecimal price;

    @Schema(example = "Guida Turistica S.r.l.")
    private String organizer;

    @Schema(description = "Lista di URL di immagini dell'attività")
    private List<String> images;

    @Schema(type = "string", format = "date-time", example = "2025-06-25T10:30:00")
    private LocalDateTime createdAt;
}