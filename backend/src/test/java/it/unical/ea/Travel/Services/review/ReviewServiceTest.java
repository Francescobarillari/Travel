package it.unical.ea.Travel.Services.review;

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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ActivityTemplateRepository activityTemplateRepository;

    @Mock
    private ItineraryRepository itineraryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ActivityBookingRepository activityBookingRepository;

    @Mock
    private ItineraryBookingRepository itineraryBookingRepository;

    @InjectMocks
    private ReviewService reviewService;

    private User testUser;
    private ActivityTemplate testTemplate;
    private Itinerary testItinerary;
    private final String userEmail = "traveler@example.com";

    @BeforeEach
    void setUp() {
        Jwt jwt = Jwt.withTokenValue("mock-token")
                .header("alg", "none")
                .claim("email", userEmail)
                .build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(jwt, "token", Collections.emptyList())
        );

        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setEmail(userEmail);
        testUser.setFirstName("Mario");
        testUser.setLastName("Rossi");

        testTemplate = new ActivityTemplate();
        testTemplate.setId(UUID.randomUUID());
        testTemplate.setName("Tour del Colosseo");

        testItinerary = new Itinerary();
        testItinerary.setId(UUID.randomUUID());
        testItinerary.setTitle("Weekend a Roma");
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createReview_ValidPurchasedActivity_Success() {
        when(userRepository.getUserByEmail(userEmail)).thenReturn(Optional.of(testUser));
        when(activityTemplateRepository.findById(testTemplate.getId())).thenReturn(Optional.of(testTemplate));
        when(activityBookingRepository.existsByUserIdAndActivityTemplateIdAndStatus(testUser.getId(), testTemplate.getId(), BookingStatus.CONFIRMED)).thenReturn(true);
        when(reviewRepository.existsByAuthorIdAndActivityTemplateId(testUser.getId(), testTemplate.getId())).thenReturn(false);

        Review savedReview = new Review();
        savedReview.setId(UUID.randomUUID());
        savedReview.setAuthor(testUser);
        savedReview.setActivityTemplate(testTemplate);
        savedReview.setRating(5.0);
        savedReview.setComment("Fantastico!");
        savedReview.setCreatedAt(LocalDateTime.now());

        when(reviewRepository.save(any(Review.class))).thenReturn(savedReview);

        CreateReviewDto dto = new CreateReviewDto(testTemplate.getId(), null, 5.0, "Fantastico!");
        ReviewDto result = reviewService.createReview(dto);

        assertNotNull(result);
        assertEquals(5.0, result.getRating());
        assertEquals("Fantastico!", result.getComment());
        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    void createReview_UnverifiedPurchaseActivity_ThrowsForbidden() {
        when(userRepository.getUserByEmail(userEmail)).thenReturn(Optional.of(testUser));
        when(activityTemplateRepository.findById(testTemplate.getId())).thenReturn(Optional.of(testTemplate));
        when(activityBookingRepository.existsByUserIdAndActivityTemplateIdAndStatus(testUser.getId(), testTemplate.getId(), BookingStatus.CONFIRMED)).thenReturn(false);

        CreateReviewDto dto = new CreateReviewDto(testTemplate.getId(), null, 4.0, "Bello");
        ApiException ex = assertThrows(ApiException.class, () -> reviewService.createReview(dto));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
        assertEquals("review.unverifiedPurchase", ex.getMessage());
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void createReview_AlreadyReviewedActivity_ThrowsConflict() {
        when(userRepository.getUserByEmail(userEmail)).thenReturn(Optional.of(testUser));
        when(activityTemplateRepository.findById(testTemplate.getId())).thenReturn(Optional.of(testTemplate));
        when(activityBookingRepository.existsByUserIdAndActivityTemplateIdAndStatus(testUser.getId(), testTemplate.getId(), BookingStatus.CONFIRMED)).thenReturn(true);
        when(reviewRepository.existsByAuthorIdAndActivityTemplateId(testUser.getId(), testTemplate.getId())).thenReturn(true);

        CreateReviewDto dto = new CreateReviewDto(testTemplate.getId(), null, 4.0, "Bello");
        ApiException ex = assertThrows(ApiException.class, () -> reviewService.createReview(dto));

        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
        assertEquals("review.alreadyExists", ex.getMessage());
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void createReview_InvalidRating_ThrowsBadRequest() {
        when(userRepository.getUserByEmail(userEmail)).thenReturn(Optional.of(testUser));

        CreateReviewDto dto = new CreateReviewDto(testTemplate.getId(), null, 999.0, "Voto falso");
        ApiException ex = assertThrows(ApiException.class, () -> reviewService.createReview(dto));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertEquals("review.invalidRating", ex.getMessage());
    }

    @Test
    void createReview_ValidPurchasedItinerary_Success() {
        when(userRepository.getUserByEmail(userEmail)).thenReturn(Optional.of(testUser));
        when(itineraryRepository.findById(testItinerary.getId())).thenReturn(Optional.of(testItinerary));
        when(itineraryBookingRepository.existsByUserIdAndItineraryIdAndStatus(testUser.getId(), testItinerary.getId(), BookingStatus.CONFIRMED)).thenReturn(true);
        when(reviewRepository.existsByAuthorIdAndItineraryId(testUser.getId(), testItinerary.getId())).thenReturn(false);

        Review savedReview = new Review();
        savedReview.setId(UUID.randomUUID());
        savedReview.setAuthor(testUser);
        savedReview.setItinerary(testItinerary);
        savedReview.setRating(4.5);
        savedReview.setComment("Itinerario bellissimo");
        savedReview.setCreatedAt(LocalDateTime.now());

        when(reviewRepository.save(any(Review.class))).thenReturn(savedReview);

        CreateReviewDto dto = new CreateReviewDto(null, testItinerary.getId(), 4.5, "Itinerario bellissimo");
        ReviewDto result = reviewService.createReview(dto);

        assertNotNull(result);
        assertEquals(4.5, result.getRating());
        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    void updateReview_ByAuthor_Success() {
        Review review = new Review();
        review.setId(UUID.randomUUID());
        review.setAuthor(testUser);
        review.setRating(3.0);
        review.setComment("Vecchio commento");
        review.setCreatedAt(LocalDateTime.now());

        when(reviewRepository.findById(review.getId())).thenReturn(Optional.of(review));
        when(reviewRepository.save(any(Review.class))).thenReturn(review);

        CreateReviewDto dto = new CreateReviewDto(null, null, 4.0, "Nuovo commento");
        ReviewDto updated = reviewService.updateReview(review.getId(), dto);

        assertNotNull(updated);
        assertEquals(4.0, updated.getRating());
        assertEquals("Nuovo commento", updated.getComment());
    }

    @Test
    void updateReview_ByOtherUser_ThrowsForbidden() {
        User otherAuthor = new User();
        otherAuthor.setId(UUID.randomUUID());
        otherAuthor.setEmail("other@example.com");

        Review review = new Review();
        review.setId(UUID.randomUUID());
        review.setAuthor(otherAuthor);

        when(reviewRepository.findById(review.getId())).thenReturn(Optional.of(review));

        CreateReviewDto dto = new CreateReviewDto(null, null, 5.0, "Tentativo hacker");
        ApiException ex = assertThrows(ApiException.class, () -> reviewService.updateReview(review.getId(), dto));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
        assertEquals("review.unauthorized", ex.getMessage());
    }

    @Test
    void deleteReview_ByAuthor_Success() {
        Review review = new Review();
        review.setId(UUID.randomUUID());
        review.setAuthor(testUser);

        when(reviewRepository.findById(review.getId())).thenReturn(Optional.of(review));

        reviewService.deleteReview(review.getId(), false);

        verify(reviewRepository).delete(review);
    }

    @Test
    void deleteReview_ByAdmin_Success() {
        User otherAuthor = new User();
        otherAuthor.setId(UUID.randomUUID());
        otherAuthor.setEmail("other@example.com");

        Review review = new Review();
        review.setId(UUID.randomUUID());
        review.setAuthor(otherAuthor);

        when(reviewRepository.findById(review.getId())).thenReturn(Optional.of(review));

        reviewService.deleteReview(review.getId(), true);

        verify(reviewRepository).delete(review);
    }

    @Test
    void deleteReview_ByOtherUserNonAdmin_ThrowsForbidden() {
        User otherAuthor = new User();
        otherAuthor.setId(UUID.randomUUID());
        otherAuthor.setEmail("other@example.com");

        Review review = new Review();
        review.setId(UUID.randomUUID());
        review.setAuthor(otherAuthor);

        when(reviewRepository.findById(review.getId())).thenReturn(Optional.of(review));

        ApiException ex = assertThrows(ApiException.class, () -> reviewService.deleteReview(review.getId(), false));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
        verify(reviewRepository, never()).delete(any());
    }
}
