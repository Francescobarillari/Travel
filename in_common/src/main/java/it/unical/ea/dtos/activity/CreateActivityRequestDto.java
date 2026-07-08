package it.unical.ea.dtos.activity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import it.unical.ea.enums.TravelTag;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

@Getter
@Setter
@NoArgsConstructor
public class CreateActivityRequestDto {

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

    @NotNull(message = "La data di inizio periodo è obbligatoria")
    @Schema(type = "string", format = "date", example = "2025-07-01")
    private LocalDate startDate;

    @NotNull(message = "La data di fine periodo è obbligatoria")
    @Schema(type = "string", format = "date", example = "2025-07-31")
    private LocalDate endDate;

    @NotEmpty(message = "Seleziona almeno un giorno della settimana")
    @Schema(description = "Giorni della settimana selezionati (es. MONDAY, TUESDAY)", example = "[\"MONDAY\", \"WEDNESDAY\"]")
    private Set<String> daysOfWeek = new HashSet<>();

    @NotEmpty(message = "Seleziona almeno una fascia oraria")
    private List<TimeSlotDto> timeSlots;

    @NotNull(message = "Il numero massimo di partecipanti per sessione è obbligatorio")
    @Min(value = 1, message = "Il numero di partecipanti deve essere almeno 1")
    @Schema(example = "20")
    private Integer participants;

    @DecimalMin(value = "0.0", message = "Il prezzo non può essere negativo")
    @Schema(example = "15.50")
    private BigDecimal price;

    @Schema(example = "Guida Turistica S.r.l.")
    private String organizer;

    @Schema(description = "Tag/Categorie associate all'attività")
    private Set<TravelTag> tags = new HashSet<>();
}
