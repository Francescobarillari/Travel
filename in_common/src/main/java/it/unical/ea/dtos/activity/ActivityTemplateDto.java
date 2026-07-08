package it.unical.ea.dtos.activity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import it.unical.ea.enums.TravelTag;

import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class ActivityTemplateDto {
    @Schema(format = "uuid", example = "550e8400-e29b-41d4-a716-446655440001")
    private UUID id;

    @Schema(example = "Visita al Colosseo")
    private String name;

    @Schema(example = "Una splendida visita guidata all'anfiteatro Flavio")
    private String description;

    @Schema(example = "Roma, Colosseo")
    private String location;

    @Schema(example = "Guida Turistica S.r.l.")
    private String organizer;

    @Schema(description = "Lista di URL di immagini del template")
    private List<String> images;

    @Schema(description = "Media delle recensioni", example = "4.5")
    private Double averageRating = 0.0;

    @Schema(description = "Tag/Categorie associate")
    private Set<TravelTag> tags = new HashSet<>();

    @Schema(description = "Sessioni disponibili per questo template")
    private List<ActivityDto> sessions;
}
