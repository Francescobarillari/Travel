package it.unical.ea.Travel.Controllers.review;

import it.unical.ea.Travel.Services.review.ReviewService;
import it.unical.ea.dtos.review.CreateReviewDto;
import it.unical.ea.dtos.review.ReviewDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<ReviewDto> createReview(@Valid @RequestBody CreateReviewDto createReviewDto) {
        return ResponseEntity.ok(reviewService.createReview(createReviewDto));
    }

    @GetMapping("/activity/{activityId}")
    public ResponseEntity<List<ReviewDto>> getReviewsForActivity(@PathVariable UUID activityId) {
        return ResponseEntity.ok(reviewService.getReviewsForActivity(activityId));
    }

    @GetMapping("/itinerary/{itineraryId}")
    public ResponseEntity<List<ReviewDto>> getReviewsForItinerary(@PathVariable UUID itineraryId) {
        return ResponseEntity.ok(reviewService.getReviewsForItinerary(itineraryId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReviewDto> updateReview(@PathVariable UUID id, @Valid @RequestBody CreateReviewDto updateReviewDto) {
        return ResponseEntity.ok(reviewService.updateReview(id, updateReviewDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        reviewService.deleteReview(id, isAdmin(jwt));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ReviewDto>> getReviewsByUser(@PathVariable UUID userId) {
        return ResponseEntity.ok(reviewService.getReviewsByUser(userId));
    }

    private boolean isAdmin(Jwt jwt) {
        if (jwt == null) return false;
        List<String> roles = jwt.getClaimAsStringList("roles");
        if (roles != null && roles.stream().anyMatch(r -> r.equalsIgnoreCase("ADMIN") || r.equalsIgnoreCase("ROLE_ADMIN"))) {
            return true;
        }
        Map<String, Object> resourceAccess = jwt.getClaim("resource_access");
        if (resourceAccess != null) {
            Object clientAccess = resourceAccess.get("ae-client");
            if (clientAccess instanceof Map<?, ?> clientAccessMap) {
                Object clientRoles = clientAccessMap.get("roles");
                if (clientRoles instanceof Collection<?> roleCollection) {
                    if (roleCollection.stream().anyMatch(r -> "ADMIN".equalsIgnoreCase(String.valueOf(r)) || "ROLE_ADMIN".equalsIgnoreCase(String.valueOf(r)))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}

