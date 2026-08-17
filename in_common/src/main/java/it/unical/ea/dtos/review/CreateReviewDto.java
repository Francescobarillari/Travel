package it.unical.ea.dtos.review;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateReviewDto {
    private UUID activityId;
    private UUID itineraryId;

    @NotNull(message = "rating.required")
    @DecimalMin(value = "1.0", message = "rating.min")
    @DecimalMax(value = "5.0", message = "rating.max")
    private Double rating;

    @Size(max = 2000, message = "comment.tooLong")
    private String comment;
}

