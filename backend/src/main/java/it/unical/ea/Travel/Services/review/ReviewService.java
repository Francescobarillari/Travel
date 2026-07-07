package it.unical.ea.Travel.Services.review;

import it.unical.ea.Travel.Entities.activity.Activity;
import it.unical.ea.Travel.Entities.itinerary.Itinerary;
import it.unical.ea.Travel.Entities.review.Review;
import it.unical.ea.Travel.Entities.user.User;
import it.unical.ea.Travel.Repositories.activity.ActivityRepository;
import it.unical.ea.Travel.Repositories.itinerary.ItineraryRepository;
import it.unical.ea.Travel.Repositories.review.ReviewRepository;
import it.unical.ea.Travel.Repositories.user.UserRepository;
import it.unical.ea.Travel.Config.SecurityUtils;
import it.unical.ea.dtos.review.CreateReviewDto;
import it.unical.ea.dtos.review.ReviewDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ActivityRepository activityRepository;
    private final ItineraryRepository itineraryRepository;
    private final UserRepository userRepository;

    @Transactional
    public ReviewDto createReview(CreateReviewDto dto) {
        String email = SecurityUtils.getCurrentUserEmail();
        User currentUser = userRepository.getUserByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        Review review = new Review();
        review.setAuthor(currentUser);
        review.setRating(dto.getRating());
        review.setComment(dto.getComment());

        if (dto.getActivityId() != null) {
            Activity activity = activityRepository.findById(dto.getActivityId())
                    .orElseThrow(() -> new RuntimeException("Activity not found"));
            review.setActivity(activity);
        } else if (dto.getItineraryId() != null) {
            Itinerary itinerary = itineraryRepository.findById(dto.getItineraryId())
                    .orElseThrow(() -> new RuntimeException("Itinerary not found"));
            review.setItinerary(itinerary);
        } else {
            throw new IllegalArgumentException("Either ActivityId or ItineraryId must be provided");
        }

        Review savedReview = reviewRepository.save(review);
        return toDto(savedReview);
    }

    @Transactional(readOnly = true)
    public List<ReviewDto> getReviewsForActivity(UUID activityId) {
        return reviewRepository.findByActivityIdOrderByCreatedAtDesc(activityId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ReviewDto> getReviewsForItinerary(UUID itineraryId) {
        Itinerary itinerary = itineraryRepository.findById(itineraryId)
                .orElseThrow(() -> new RuntimeException("Itinerary not found"));
                
        List<Review> allReviews = new ArrayList<>();
        
        // 1. Get global itinerary reviews
        allReviews.addAll(reviewRepository.findByItineraryIdOrderByCreatedAtDesc(itineraryId));
        
        // 2. Get reviews for all activities in this itinerary
        List<UUID> activityIds = itinerary.getActivities().stream()
                .map(Activity::getId)
                .collect(Collectors.toList());
                
        if (!activityIds.isEmpty()) {
            allReviews.addAll(reviewRepository.findByActivityIdInOrderByCreatedAtDesc(activityIds));
        }

        // Sort by createdAt desc
        allReviews.sort((r1, r2) -> r2.getCreatedAt().compareTo(r1.getCreatedAt()));

        return allReviews.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ReviewDto updateReview(UUID id, CreateReviewDto dto) {
        String email = SecurityUtils.getCurrentUserEmail();
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Review not found"));

        if (!review.getAuthor().getEmail().equals(email)) {
            throw new RuntimeException("Non hai i permessi per modificare questa recensione");
        }

        review.setRating(dto.getRating());
        review.setComment(dto.getComment());
        
        Review savedReview = reviewRepository.save(review);
        return toDto(savedReview);
    }

    @Transactional
    public void deleteReview(UUID id) {
        String email = SecurityUtils.getCurrentUserEmail();
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Review not found"));

        if (!review.getAuthor().getEmail().equals(email)) {
            throw new RuntimeException("Non hai i permessi per eliminare questa recensione");
        }

        reviewRepository.delete(review);
    }

    @Transactional(readOnly = true)
    public List<ReviewDto> getReviewsByUser(UUID userId) {
        return reviewRepository.findByAuthorIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private ReviewDto toDto(Review review) {
        ReviewDto dto = new ReviewDto();
        dto.setId(review.getId());
        dto.setAuthorName(getAuthorName(review.getAuthor()));

        String email = SecurityUtils.getCurrentUserEmail();
        dto.setIsEditable(review.getAuthor().getEmail().equals(email));

        dto.setRating(review.getRating());
        dto.setComment(review.getComment());
        dto.setCreatedAt(review.getCreatedAt());
        
        if (review.getActivity() != null) {
            try {
                dto.setActivityId(review.getActivity().getId());
                dto.setActivityName(review.getActivity().getName());
            } catch (Exception e) {
                dto.setActivityName("Attività Eliminata");
            }
        }
        
        if (review.getItinerary() != null) {
            try {
                dto.setItineraryId(review.getItinerary().getId());
                dto.setItineraryName(review.getItinerary().getTitle());
            } catch (Exception e) {
                dto.setItineraryName("Itinerario Eliminato");
            }
        }
        
        return dto;
    }

    private String getAuthorName(User user) {
        if (user == null) return "Unknown";
        if (user.getFirstName() != null && user.getLastName() != null) {
            return user.getFirstName() + " " + user.getLastName();
        } else if (user.getCompanyName() != null) {
            return user.getCompanyName();
        } else if (user.getFirstName() != null) {
            return user.getFirstName();
        }
        return "Unknown";
    }
}
