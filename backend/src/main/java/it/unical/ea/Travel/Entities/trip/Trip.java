package it.unical.ea.Travel.Entities.trip;

import it.unical.ea.Travel.Entities.activity.Activity;
import it.unical.ea.Travel.Entities.itinerary.Itinerary;
import it.unical.ea.Travel.Entities.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "trips", indexes = {
    @Index(name = "idx_trip_location", columnList = "location"),
    @Index(name = "idx_trip_title", columnList = "title")
})
@SQLDelete(sql = "UPDATE trips SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Trip {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "title", nullable = false, length = 150)
    private String title;

    @Column(name = "location", nullable = false)
    private String location;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "price")
    private Double price;

    @Column(name = "duration")
    private Integer duration; // espresso in giorni

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizer_id", nullable = false)
    private User organizer;

    // Itinerario standard opzionale associato a questo Viaggio
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "standard_itinerary_id")
    private Itinerary standardItinerary;

    @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Activity> activities = new ArrayList<>();

    @Column(name = "image_url")
    private String imageUrl;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Version
    private Long version;

    public boolean isDeleted() {
        return deletedAt != null;
    }
}
