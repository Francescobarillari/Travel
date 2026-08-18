package it.unical.ea.Travel.Services.review;

import it.unical.ea.Travel.Config.SecurityUtils;
import it.unical.ea.Travel.Entities.activity.ActivityTemplate;
import it.unical.ea.Travel.Entities.itinerary.Itinerary;
import it.unical.ea.Travel.Entities.payment.BookingStatus;
import it.unical.ea.Travel.Entities.review.Review;
import it.unical.ea.Travel.Entities.user.User;
import it.unical.ea.Travel.Exception.ApiException;
import it.unical.ea.Travel.Repositories.activity.ActivityBookingRepository;
import it.unical.ea.Travel.Repositories.activity.ActivityTemplateRepository;
import it.unical.ea.Travel.Repositories.itinerary.ItineraryBookingRepository;
import it.unical.ea.Travel.Repositories.itinerary.ItineraryRepository;
import it.unical.ea.Travel.Repositories.review.ReviewRepository;
import it.unical.ea.Travel.Repositories.user.UserRepository;
import it.unical.ea.dtos.review.CreateReviewDto;
import it.unical.ea.dtos.review.ReviewDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
    private final ActivityTemplateRepository activityTemplateRepository;
    private final ItineraryRepository itineraryRepository;
    private final UserRepository userRepository;
    private final ActivityBookingRepository activityBookingRepository;
    private final ItineraryBookingRepository itineraryBookingRepository;

    @Transactional
    public ReviewDto createReview(CreateReviewDto dto) {
        String email = SecurityUtils.getCurrentUserEmail();
        User currentUser = userRepository.getUserByEmail(email)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "user.notFound"));

        if (dto.getRating() == null || dto.getRating() < 1.0 || dto.getRating() > 5.0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "review.invalidRating");
        }

        Review review = new Review();
        review.setAuthor(currentUser);
        review.setRating(dto.getRating());
        review.setComment(dto.getComment());

        java.time.LocalDateTime now = java.time.LocalDateTime.now();

        if (dto.getActivityId() != null) {
            ActivityTemplate activityTemplate = activityTemplateRepository.findById(dto.getActivityId())
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "activity.notFound"));

            boolean hasBooked = activityBookingRepository.existsByUserIdAndActivityTemplateIdAndStatus(
                    currentUser.getId(), activityTemplate.getId(), BookingStatus.CONFIRMED);
            if (!hasBooked) {
                throw new ApiException(HttpStatus.FORBIDDEN, "review.unverifiedPurchase");
            }

            boolean hasCompleted = activityBookingRepository.existsCompletedBookingByTemplate(
                    currentUser.getId(), activityTemplate.getId(), BookingStatus.CONFIRMED, now);
            if (!hasCompleted) {
                throw new ApiException(HttpStatus.FORBIDDEN, "review.activityNotCompleted");
            }

            boolean alreadyReviewed = reviewRepository.existsByAuthorIdAndActivityTemplateId(
                    currentUser.getId(), activityTemplate.getId());
            if (alreadyReviewed) {
                throw new ApiException(HttpStatus.CONFLICT, "review.alreadyExists");
            }

            review.setActivityTemplate(activityTemplate);
        } else if (dto.getItineraryId() != null) {
            Itinerary itinerary = itineraryRepository.findById(dto.getItineraryId())
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "itinerary.notFound"));

            boolean hasBooked = itineraryBookingRepository.existsByUserIdAndItineraryIdAndStatus(
                    currentUser.getId(), itinerary.getId(), BookingStatus.CONFIRMED);
            if (!hasBooked) {
                throw new ApiException(HttpStatus.FORBIDDEN, "review.unverifiedPurchase");
            }

            boolean hasCompleted = itineraryBookingRepository.existsCompletedBookingByItinerary(
                    currentUser.getId(), itinerary.getId(), BookingStatus.CONFIRMED, now);
            if (!hasCompleted) {
                throw new ApiException(HttpStatus.FORBIDDEN, "review.activityNotCompleted");
            }

            boolean alreadyReviewed = reviewRepository.existsByAuthorIdAndItineraryId(
                    currentUser.getId(), itinerary.getId());
            if (alreadyReviewed) {
                throw new ApiException(HttpStatus.CONFLICT, "review.alreadyExists");
            }

            review.setItinerary(itinerary);
        } else {
            throw new ApiException(HttpStatus.BAD_REQUEST, "review.targetRequired");
        }

        Review savedReview = reviewRepository.save(review);
        return toDto(savedReview);
    }

    @Transactional(readOnly = true)
    public List<ReviewDto> getReviewsForActivity(UUID activityTemplateId) {
        return reviewRepository.findByActivityTemplateIdOrderByCreatedAtDesc(activityTemplateId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ReviewDto> getReviewsForItinerary(UUID itineraryId) {
        Itinerary itinerary = itineraryRepository.findById(itineraryId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "itinerary.notFound"));

        List<Review> allReviews = new ArrayList<>(reviewRepository.findByItineraryIdOrderByCreatedAtDesc(itineraryId));

        List<UUID> activityTemplateIds = itinerary.getActivities().stream()
                .map(a -> a.getTemplate().getId())
                .distinct()
                .collect(Collectors.toList());

        if (!activityTemplateIds.isEmpty()) {
            allReviews.addAll(reviewRepository.findByActivityTemplateIdInOrderByCreatedAtDesc(activityTemplateIds));
        }

        allReviews.sort((r1, r2) -> r2.getCreatedAt().compareTo(r1.getCreatedAt()));

        return allReviews.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ReviewDto updateReview(UUID id, CreateReviewDto dto) {
        String email = SecurityUtils.getCurrentUserEmail();
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "review.notFound"));

        if (!review.getAuthor().getEmail().equalsIgnoreCase(email)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "review.unauthorized");
        }

        if (dto.getRating() == null || dto.getRating() < 1.0 || dto.getRating() > 5.0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "review.invalidRating");
        }

        review.setRating(dto.getRating());
        review.setComment(dto.getComment());

        Review savedReview = reviewRepository.save(review);
        return toDto(savedReview);
    }

    public void deleteReview(UUID id) {
        deleteReview(id, false);
    }

    @Transactional
    public void deleteReview(UUID id, boolean isAdmin) {
        String email = SecurityUtils.getCurrentUserEmail();
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "review.notFound"));

        boolean isAuthor = review.getAuthor().getEmail().equalsIgnoreCase(email);
        if (!isAuthor && !isAdmin) {
            throw new ApiException(HttpStatus.FORBIDDEN, "review.unauthorized");
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
        dto.setIsEditable(review.getAuthor().getEmail().equalsIgnoreCase(email));

        dto.setRating(review.getRating());
        dto.setComment(review.getComment());
        dto.setCreatedAt(review.getCreatedAt());

        if (review.getActivityTemplate() != null) {
            try {
                dto.setActivityId(review.getActivityTemplate().getId());
                dto.setActivityName(review.getActivityTemplate().getName());
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

