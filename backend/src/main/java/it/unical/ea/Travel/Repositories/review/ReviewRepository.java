package it.unical.ea.Travel.Repositories.review;

import it.unical.ea.Travel.Entities.review.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {

    List<Review> findByActivityTemplateIdOrderByCreatedAtDesc(UUID activityTemplateId);

    List<Review> findByItineraryIdOrderByCreatedAtDesc(UUID itineraryId);

    @Query("SELECT r FROM Review r WHERE r.activityTemplate.id IN :activityTemplateIds ORDER BY r.createdAt DESC")
    List<Review> findByActivityTemplateIdInOrderByCreatedAtDesc(@Param("activityTemplateIds") List<UUID> activityTemplateIds);

    List<Review> findByAuthorIdOrderByCreatedAtDesc(UUID authorId);
}
