package it.unical.ea.dtos.location;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class LocationDto {
    @Schema(format = "uuid", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @NotBlank(message = "Il nome della località è obbligatorio")
    @Schema(example = "Roma")
    private String name;

    @Schema(example = "Visita alla città eterna in 3 giorni.")
    private String description;

    @Schema(example = "https://images.unsplash.com/photo-1552832230-c0197dd311b5?ixlib=rb-1.2.1&auto=format&fit=crop&w=800&q=80")
    private String imageUrl;
}
