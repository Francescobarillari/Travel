package it.unical.ea.dtos.activity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
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

    @NotBlank(message = "Il nome dell'attività è obbligatorio")
    @Size(max = 150, message = "Il nome non può superare i 150 caratteri")
    @Schema(example = "Visita al Colosseo")
    private String name;

    @Size(max = 1000, message = "La descrizione non può superare i 1000 caratteri")
    @Schema(example = "Una splendida visita guidata all'anfiteatro Flavio")
    private String description;

    @NotBlank(message = "La posizione dell'attività è obbligatoria")
    @Schema(example = "Roma, Colosseo")
    private String location;

    @NotNull(message = "La data e ora di inizio sono obbligatorie")
    @Schema(type = "string", format = "date-time", example = "2025-07-01T10:00:00")
    private LocalDateTime startTime;

    @NotNull(message = "La data e ora di fine sono obbligatorie")
    @Schema(type = "string", format = "date-time", example = "2025-07-01T12:00:00")
    private LocalDateTime endTime;

    @NotNull(message = "Il numero massimo di partecipanti è obbligatorio")
    @Min(value = 1, message = "Il numero di partecipanti deve essere almeno 1")
    @Schema(example = "20")
    private Integer participants;

    @DecimalMin(value = "0.0", message = "Il prezzo non può essere negativo")
    @Schema(example = "15.50")
    private BigDecimal price;

    @Schema(example = "Guida Turistica S.r.l.")
    private String organizer;

    @Schema(description = "Lista di URL di immagini dell'attività")
    private List<String> images;

    @Schema(description = "Numero attuale di partecipanti prenotati", example = "5", accessMode = Schema.AccessMode.READ_ONLY)
    private Integer currentParticipants = 0;

    @Schema(type = "string", format = "date-time", example = "2025-06-25T10:30:00")
    private LocalDateTime createdAt;
}