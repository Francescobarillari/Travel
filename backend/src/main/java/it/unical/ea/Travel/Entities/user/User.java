package it.unical.ea.Travel.Entities.user;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import it.unical.ea.enums.UserType;

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
@Table(name = "users", indexes = {
        @Index(name = "idx_user_email", columnList = "email", unique = true)
})

@SQLDelete(sql = "UPDATE users SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "email", unique = true, nullable = false, length = 255)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "keycloak_id", length = 36)
    private String keycloakId;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_type", nullable = false, length = 50)
    private UserType userType = UserType.VIAGGIATORE;

    @Column(name = "first_name", length = 100)
    private String firstName;

    @Column(name = "last_name", length = 100)
    private String lastName;

    @Column(name = "company_name", length = 150)
    private String companyName;

    @Column(name = "vat_number", length = 50)
    private String vatNumber;

    // Foto dei documenti di identità della società (fronte/retro ecc.)
    @ElementCollection
    @CollectionTable(
        name = "company_document_photos",
        joinColumns = @JoinColumn(name = "user_id")
    )
    @Column(name = "photo_url", length = 500, nullable = false)
    private List<String> documentPhotos = new ArrayList<>();

    @Column(name = "phone", length = 30)
    private String phone;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Column(name = "email_verified", nullable = false)
    private Boolean emailVerified = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "roles", nullable = false)
    private String roles = "ROLE_USER";

    @Column(name = "approved", nullable = false)
    private Boolean approved = true;

    @Column(name = "blocked", nullable = false)
    private Boolean blocked = false;

    public boolean isDeleted() {
        return deletedAt != null;
    }
}

/*
 * users
 * ├── id (PK, UUID)
 * ├── email (UNIQUE)
 * ├── password_hash
 * ├── user_type (VIAGGIATORE | SOCIETA)
 * ├── first_name, last_name       (nullable, solo VIAGGIATORE)
 * ├── company_name, vat_number    (nullable, solo SOCIETA)
 * ├── phone (nullable)
 * ├── avatar_url (nullable)
 * ├── email_verified (boolean)
 * ├── created_at, updated_at
 * └── deleted_at (nullable, soft delete)
 *
 * company_document_photos
 * ├── user_id (FK → users.id)
 * └── photo_url
 */