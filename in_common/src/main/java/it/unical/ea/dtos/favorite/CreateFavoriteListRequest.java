package it.unical.ea.dtos.favorite;

import io.swagger.v3.oas.annotations.media.Schema;
import it.unical.ea.enums.FavoriteListVisibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Payload per creare o aggiornare una lista di itinerari preferiti.
 */
@Getter
@Setter
@NoArgsConstructor
public class CreateFavoriteListRequest {

    @NotBlank(message = "{validation.favoriteList.name.notBlank}")
    @Size(max = 100, message = "{validation.favoriteList.name.maxSize}")
    @Schema(example = "Sogni di viaggio")
    private String name;

    @Schema(example = "PRIVATE", description = "PRIVATE (default), SHARED o PUBLIC")
    private FavoriteListVisibility visibility = FavoriteListVisibility.PRIVATE;

    @Schema(description = "Email degli utenti con cui condividere la lista (usato solo se visibility=SHARED)")
    private List<String> sharedWithEmails = new ArrayList<>();
}
