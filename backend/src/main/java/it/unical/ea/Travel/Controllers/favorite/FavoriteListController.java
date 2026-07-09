package it.unical.ea.Travel.Controllers.favorite;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.unical.ea.Travel.Exception.ApiException;
import it.unical.ea.Travel.Services.favorite.FavoriteListService;
import it.unical.ea.dtos.favorite.CreateFavoriteListRequest;
import it.unical.ea.dtos.favorite.FavoriteListDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/favorite-lists")
@RequiredArgsConstructor
@Tag(name = "Favorite Lists", description = "Liste di itinerari preferiti (private, condivise o pubbliche via link)")
public class FavoriteListController {

    private final FavoriteListService favoriteListService;

    @Operation(summary = "Crea una nuova lista di preferiti")
    @PostMapping
    public FavoriteListDto createList(@Valid @RequestBody CreateFavoriteListRequest request,
                                      @AuthenticationPrincipal Jwt jwt) {
        return favoriteListService.createList(request, requireEmail(jwt));
    }

    @Operation(summary = "Aggiorna nome, visibilità o condivisioni di una lista")
    @PutMapping("/{listId}")
    public FavoriteListDto updateList(@PathVariable String listId,
                                      @Valid @RequestBody CreateFavoriteListRequest request,
                                      @AuthenticationPrincipal Jwt jwt) {
        return favoriteListService.updateList(listId, request, requireEmail(jwt));
    }

    @Operation(summary = "Elimina una lista di preferiti")
    @DeleteMapping("/{listId}")
    public ResponseEntity<Void> deleteList(@PathVariable String listId,
                                           @AuthenticationPrincipal Jwt jwt) {
        favoriteListService.deleteList(listId, requireEmail(jwt));
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Elenca le liste di preferiti dell'utente autenticato")
    @GetMapping
    public List<FavoriteListDto> getMyLists(@AuthenticationPrincipal Jwt jwt) {
        return favoriteListService.getMyLists(requireEmail(jwt));
    }

    @Operation(summary = "Elenca le liste condivise con l'utente autenticato")
    @GetMapping("/shared-with-me")
    public List<FavoriteListDto> getSharedWithMe(@AuthenticationPrincipal Jwt jwt) {
        return favoriteListService.getSharedWithMe(requireEmail(jwt));
    }

    @Operation(summary = "Ottieni una lista per ID", description = "Accessibile al proprietario, ai destinatari se SHARED, o a chiunque se PUBLIC")
    @GetMapping("/{listId}")
    public FavoriteListDto getList(@PathVariable String listId,
                                   @AuthenticationPrincipal Jwt jwt) {
        String email = jwt != null ? jwt.getClaimAsString("email") : null;
        return favoriteListService.getList(listId, email);
    }

    @Operation(summary = "Aggiungi un itinerario alla lista")
    @PostMapping("/{listId}/itineraries/{itineraryId}")
    public FavoriteListDto addItinerary(@PathVariable String listId,
                                        @Parameter(schema = @Schema(format = "uuid")) @PathVariable String itineraryId,
                                        @AuthenticationPrincipal Jwt jwt) {
        return favoriteListService.addItinerary(listId, itineraryId, requireEmail(jwt));
    }

    @Operation(summary = "Rimuovi un itinerario dalla lista")
    @DeleteMapping("/{listId}/itineraries/{itineraryId}")
    public FavoriteListDto removeItinerary(@PathVariable String listId,
                                           @Parameter(schema = @Schema(format = "uuid")) @PathVariable String itineraryId,
                                           @AuthenticationPrincipal Jwt jwt) {
        return favoriteListService.removeItinerary(listId, itineraryId, requireEmail(jwt));
    }

    @Operation(summary = "Accedi a una lista pubblica tramite token di condivisione",
            description = "Endpoint pubblico: consente l'accesso in sola lettura a chiunque possieda il link, senza autenticazione")
    @GetMapping("/shared/{shareToken}")
    public FavoriteListDto getByShareToken(@PathVariable String shareToken) {
        return favoriteListService.getByShareToken(shareToken);
    }

    private String requireEmail(Jwt jwt) {
        if (jwt == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "error.unauthorized");
        }
        return jwt.getClaimAsString("email");
    }
}
