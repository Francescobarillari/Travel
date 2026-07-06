package it.unical.ea.Travel.Entities.activity;

import it.unical.ea.Travel.Entities.itinerary.Itinerary;
import it.unical.ea.Travel.Entities.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "activity_bookings", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "activity_id"})
})
@Getter
@Setter
@NoArgsConstructor
public class ActivityBooking {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activity_id", nullable = false)
    private Activity activity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "itinerary_id")
    private Itinerary itinerary;

    @Column(name = "booked_at", nullable = false)
    private LocalDateTime bookedAt = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private it.unical.ea.Travel.Entities.payment.BookingStatus status = it.unical.ea.Travel.Entities.payment.BookingStatus.PENDING;

    @Column(name = "payment_intent_id")
    private String paymentIntentId;
}
