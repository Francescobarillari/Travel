package it.unical.ea.Travel.Controllers.review;

import it.unical.ea.Travel.Services.review.ReviewService;
import it.unical.ea.dtos.review.CreateReviewDto;
import it.unical.ea.dtos.review.ReviewDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<ReviewDto> createReview(@RequestBody CreateReviewDto createReviewDto) {
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
    public ResponseEntity<ReviewDto> updateReview(@PathVariable UUID id, @RequestBody CreateReviewDto updateReviewDto) {
        return ResponseEntity.ok(reviewService.updateReview(id, updateReviewDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable UUID id) {
        reviewService.deleteReview(id);
        return ResponseEntity.noContent().build();
    }
}
