package it.unical.ea.dtos.review;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateReviewDto {
    private UUID activityId;
    private UUID itineraryId; // One of these should be populated
    private Double rating; // 1 to 5
    private String comment;
}
