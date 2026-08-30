package it.unical.ea.dtos.itinerary;

import io.swagger.v3.oas.annotations.media.Schema;
import it.unical.ea.enums.JoinRequestStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class ItineraryJoinRequestDto {
    @Schema(format = "uuid", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Schema(format = "uuid", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID itineraryId;

    @Schema(example = "Tour di Roma")
    private String itineraryTitle;

    @Schema(format = "uuid", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID userId;

    @Schema(example = "Mario Rossi")
    private String userName;

    @Schema(example = "mario.rossi@example.com")
    private String userEmail;

    @Schema(example = "http://localhost:8080/user/avatar/...")
    private String userAvatarUrl;

    @Schema(example = "PENDING")
    private JoinRequestStatus status = JoinRequestStatus.PENDING;

    @Schema(type = "string", format = "date-time", example = "2025-07-01T10:00:00")
    private LocalDateTime createdAt;
}
