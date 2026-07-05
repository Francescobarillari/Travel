package it.unical.ea.Travel.Entities.activity;

import it.unical.ea.Travel.Entities.audit.AuditBaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import it.unical.ea.enums.TravelTag;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "activities", indexes = {
    @Index(name = "idx_activity_location", columnList = "location"),
    @Index(name = "idx_activity_name", columnList = "name")
})
@SQLDelete(sql = "UPDATE activities SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Activity extends AuditBaseEntity {

    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "location", nullable = false)
    private String location;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(name = "participants", nullable = false)
    private Integer participants;

    @Column(name = "price", precision = 10, scale = 2)
    private BigDecimal price;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizer_id", nullable = false)
    private it.unical.ea.Travel.Entities.user.User organizer;

    @ElementCollection
    @CollectionTable(name = "activity_images", joinColumns = @JoinColumn(name = "activity_id"))
    @Column(name = "image_url")
    private List<String> images;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "localita_id")
    private it.unical.ea.Travel.Entities.localita.Localita localita;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Version
    private Long version;

    @Column(name = "approved", nullable = false)
    private Boolean approved = true;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "activity_tags",
        joinColumns = @JoinColumn(name = "activity_id")
    )
    @Column(name = "tag")
    @Enumerated(EnumType.STRING)
    private Set<TravelTag> tags = new HashSet<>();

    public boolean isDeleted() {
        return deletedAt != null;
    }
}