package it.unical.ea.dtos.trip;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class TripDto {
    @Schema(format = "uuid", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @NotBlank(message = "Il titolo è obbligatorio")
    @Schema(example = "Weekend a Roma")
    private String title;

    @NotBlank(message = "La località è obbligatoria")
    @Schema(example = "Roma")
    private String location;

    @Schema(example = "Visita alla città eterna in 3 giorni.")
    private String description;

    @Schema(example = "Marco Rossi")
    private String organizer;
    
    private Double price;
    
    private Integer duration;
    
    private String imageUrl;
}
