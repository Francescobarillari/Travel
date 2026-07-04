package it.unical.ea.Travel.Controllers.feed;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.unical.ea.Travel.Services.feed.FeedService;
import it.unical.ea.dtos.trip.TripDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import it.unical.ea.Travel.Exception.ApiException;

import java.util.List;

@RestController
@RequestMapping("/api/feed")
@RequiredArgsConstructor
@Tag(name = "Feed", description = "Personalized Traveler Recommendation Feed")
public class FeedController {

    private final FeedService feedService;

    @Operation(summary = "Ottieni il feed personalizzato per il viaggiatore autenticato")
    @GetMapping("/personalized")
    public List<TripDto> getPersonalizedFeed(@AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "auth.login.invalidCredentials");
        }
        String email = jwt.getClaimAsString("email");
        if (email == null) {
            email = jwt.getClaimAsString("preferred_username");
        }
        if (email == null) {
            email = jwt.getSubject();
        }
        return feedService.getPersonalizedFeed(email);
    }
}
