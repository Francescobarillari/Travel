package it.unical.ea.Travel.Controllers.favorite;

import it.unical.ea.Travel.Services.favorite.FavoritesService;
import it.unical.ea.dtos.favorite.UserFavoritesDto;
import it.unical.ea.Travel.Exception.ApiException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/favorites")
@Tag(name = "Favorites", description = "Gestione dei preferiti dell'utente")
public class FavoritesController {

    private final FavoritesService favoritesService;

    @Operation(summary = "Ottieni tutti i preferiti (attività e itinerari) dell'utente autenticato")
    @GetMapping
    public UserFavoritesDto getFavorites(@AuthenticationPrincipal Jwt jwt) {
        String email = getAuthenticatedUserEmail(jwt);
        return favoritesService.getFavorites(email);
    }

    @Operation(summary = "Aggiunge un itinerario ai preferiti")
    @PostMapping("/itineraries/{id}")
    public void addFavoriteItinerary(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        String email = getAuthenticatedUserEmail(jwt);
        favoritesService.addFavoriteItinerary(id, email);
    }

    @Operation(summary = "Rimuove un itinerario dai preferiti")
    @DeleteMapping("/itineraries/{id}")
    public void removeFavoriteItinerary(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        String email = getAuthenticatedUserEmail(jwt);
        favoritesService.removeFavoriteItinerary(id, email);
    }

    @Operation(summary = "Aggiunge un'attività ai preferiti")
    @PostMapping("/activities/{id}")
    public void addFavoriteActivity(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        String email = getAuthenticatedUserEmail(jwt);
        favoritesService.addFavoriteActivity(id, email);
    }

    @Operation(summary = "Rimuove un'attività dai preferiti")
    @DeleteMapping("/activities/{id}")
    public void removeFavoriteActivity(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        String email = getAuthenticatedUserEmail(jwt);
        favoritesService.removeFavoriteActivity(id, email);
    }

    private String getAuthenticatedUserEmail(Jwt jwt) {
        if (jwt == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "auth.login.invalidCredentials");
        }
        String email = jwt.getClaimAsString("email");
        if (email == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "auth.login.invalidCredentials");
        }
        return email;
    }
}
