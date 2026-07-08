package it.unical.ea.dtos.activity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TimeSlotDto {
    @NotNull(message = "L'orario di inizio è obbligatorio")
    @Schema(type = "string", format = "time", example = "14:00:00")
    private LocalTime startTime;

    @NotNull(message = "L'orario di fine è obbligatorio")
    @Schema(type = "string", format = "time", example = "16:00:00")
    private LocalTime endTime;
}
