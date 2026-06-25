package it.unical.ea.Travel.DTOs.activity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class ActivityDto {
    @Schema(format = "uuid", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;
    @Schema(example = "Visita al Colosseo")
    private String title;
    @Schema(example = "Tour guidato del Colosseo e del Foro Romano")
    private String description;
    @Schema(type = "string", format = "date-time", example = "2025-06-25T10:30:00")
    private LocalDateTime createdAt;
}