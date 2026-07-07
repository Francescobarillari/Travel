package it.unical.ea.Travel.Entities.activity;

import it.unical.ea.enums.TravelTag;
import it.unical.ea.Travel.Entities.audit.AuditBaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "activity_templates", indexes = {
    @Index(name = "idx_activity_template_location", columnList = "location"),
    @Index(name = "idx_activity_template_name", columnList = "name")
})
@SQLDelete(sql = "UPDATE activity_templates SET deleted_at = CURRENT_TIMESTAMP WHERE id = ? AND version = ?")
@SQLRestriction("deleted_at IS NULL")
public class ActivityTemplate extends AuditBaseEntity {

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizer_id", nullable = false)
    private it.unical.ea.Travel.Entities.user.User organizer;

    @ElementCollection
    @CollectionTable(name = "activity_template_images", joinColumns = @JoinColumn(name = "activity_template_id"))
    @Column(name = "image_url")
    private List<String> images;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private it.unical.ea.Travel.Entities.location.Location locationEntity;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Version
    private Long version;

    @Column(name = "approved", nullable = false)
    private Boolean approved = true;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "activity_template_tags",
        joinColumns = @JoinColumn(name = "activity_template_id")
    )
    @Column(name = "tag")
    @Enumerated(EnumType.STRING)
    private Set<TravelTag> tags = new HashSet<>();

    public boolean isDeleted() {
        return deletedAt != null;
    }
}
