package it.unical.ea.dtos.favorite;

import io.swagger.v3.oas.annotations.media.Schema;
import it.unical.ea.dtos.itinerary.ItineraryDto;
import it.unical.ea.enums.FavoriteListVisibility;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Rappresentazione di una lista di itinerari preferiti restituita al client.
 */
@Getter
@Setter
@NoArgsConstructor
public class FavoriteListDto {

    @Schema(format = "uuid", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Schema(example = "Sogni di viaggio")
    private String name;

    @Schema(example = "PRIVATE", description = "PRIVATE, SHARED o PUBLIC")
    private FavoriteListVisibility visibility;

    @Schema(format = "uuid", description = "Proprietario della lista")
    private UUID ownerId;

    @Schema(example = "Mario Rossi")
    private String ownerName;

    @Schema(description = "Email degli utenti con cui la lista è condivisa (solo per visibilità SHARED)")
    private List<String> sharedWithEmails = new ArrayList<>();

    @Schema(description = "Token opaco per l'accesso pubblico via link. Valorizzato solo per liste PUBLIC.")
    private String shareToken;

    @Schema(description = "Itinerari contenuti nella lista")
    private List<ItineraryDto> itineraries = new ArrayList<>();

    @Schema(description = "true se l'utente autenticato è il proprietario della lista", accessMode = Schema.AccessMode.READ_ONLY)
    private boolean editable;

    @Schema(type = "string", format = "date-time")
    private LocalDateTime createdAt;
}
