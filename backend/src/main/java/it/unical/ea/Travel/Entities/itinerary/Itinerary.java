package it.unical.ea.Travel.Entities.itinerary;

import it.unical.ea.Travel.Entities.activity.Activity;
import it.unical.ea.Travel.Entities.audit.AuditBaseEntity;
import it.unical.ea.Travel.Entities.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "itineraries")
@SQLDelete(sql = "UPDATE itineraries SET deleted_at = CURRENT_TIMESTAMP WHERE id = ? AND version = ?")
@SQLRestriction("deleted_at IS NULL")
public class Itinerary extends AuditBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "title", nullable = false, length = 150)
    private String title;

    @Column(name = "description", length = 1000)
    private String description;

    // Per ora manuali, in futuro calcolati dalla prima/ultima Activity
    @Column(name = "start_date_time")
    private LocalDateTime startDateTime;

    @Column(name = "end_date_time")
    private LocalDateTime endDateTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    @ManyToMany
    @JoinTable(
            name = "itinerary_activities",
            joinColumns = @JoinColumn(name = "itinerary_id"),
            inverseJoinColumns = @JoinColumn(name = "activity_id")
    )
    private List<Activity> activities = new ArrayList<>();

    @Column(name = "image_path", length = 500)
    private String imagePath;

    @Column(name = "visibility", length = 50, nullable = false)
    private String visibility = "PRIVATE";

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Version
    private Long version;

    public boolean isDeleted() {
        return deletedAt != null;
    }
}

/*
 * itineraries
 * ├── id (PK, UUID)
 * ├── title
 * ├── description (nullable)
 * ├── start_date_time (nullable, manuale)
 * ├── end_date_time (nullable, manuale)
 * ├── creator_id (FK → users.id)
 * ├── created_at, updated_at
 * └── deleted_at (nullable, soft delete)
 *
 * itinerary_activities (tabella di join)
 * ├── itinerary_id (FK → itineraries.id)
 * └── activity_id (FK → activities.id)
 */
