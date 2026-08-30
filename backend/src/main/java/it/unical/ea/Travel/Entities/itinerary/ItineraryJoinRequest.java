package it.unical.ea.Travel.Entities.itinerary;

import it.unical.ea.Travel.Entities.audit.AuditBaseEntity;
import it.unical.ea.Travel.Entities.user.User;
import it.unical.ea.enums.JoinRequestStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "itinerary_join_requests", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"itinerary_id", "user_id"})
})
public class ItineraryJoinRequest extends AuditBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "itinerary_id", nullable = false)
    private Itinerary itinerary;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private JoinRequestStatus status = JoinRequestStatus.PENDING;
}
