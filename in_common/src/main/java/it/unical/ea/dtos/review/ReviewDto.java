package it.unical.ea.dtos.review;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDto {
    private UUID id;
    private String authorName;
    private Boolean isEditable;
    private Double rating;
    private String comment;
    private LocalDateTime createdAt;
    
    private UUID activityId;
    private String activityName; // Populated when returning aggregated reviews for an itinerary

    private UUID itineraryId;
    private String itineraryName;
}
