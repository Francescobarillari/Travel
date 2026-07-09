package it.unical.ea.Travel.Entities.favorite;

import it.unical.ea.Travel.Entities.audit.AuditBaseEntity;
import it.unical.ea.Travel.Entities.itinerary.Itinerary;
import it.unical.ea.Travel.Entities.user.User;
import it.unical.ea.enums.FavoriteListVisibility;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Lista di itinerari preferiti creata da un viaggiatore.
 *
 * La visibilità determina chi può leggere la lista:
 * - PRIVATE: solo il proprietario;
 * - SHARED: il proprietario e gli utenti in {@link #sharedWithEmails};
 * - PUBLIC: chiunque possieda {@link #shareToken}, anche senza autenticazione.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "favorite_lists")
@SQLDelete(sql = "UPDATE favorite_lists SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class FavoriteList extends AuditBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "visibility", nullable = false, length = 20)
    private FavoriteListVisibility visibility = FavoriteListVisibility.PRIVATE;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "favorite_list_itineraries",
            joinColumns = @JoinColumn(name = "favorite_list_id"),
            inverseJoinColumns = @JoinColumn(name = "itinerary_id")
    )
    private List<Itinerary> itineraries = new ArrayList<>();

    /** Email degli utenti autorizzati a leggere la lista quando la visibilità è SHARED. */
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "favorite_list_shared_with",
            joinColumns = @JoinColumn(name = "favorite_list_id")
    )
    @Column(name = "email", nullable = false, length = 255)
    private Set<String> sharedWithEmails = new HashSet<>();

    /**
     * Token opaco che concede accesso pubblico in sola lettura via link.
     * Popolato solo quando la visibilità è PUBLIC; azzerato altrimenti.
     */
    @Column(name = "share_token", unique = true, length = 64)
    private String shareToken;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public boolean isDeleted() {
        return deletedAt != null;
    }
}
