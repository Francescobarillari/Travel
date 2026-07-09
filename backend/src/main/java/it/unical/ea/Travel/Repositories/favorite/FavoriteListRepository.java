package it.unical.ea.Travel.Repositories.favorite;

import it.unical.ea.Travel.Entities.favorite.FavoriteList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FavoriteListRepository extends JpaRepository<FavoriteList, UUID> {

    List<FavoriteList> findByOwnerIdOrderByCreatedAtDesc(UUID ownerId);

    Optional<FavoriteList> findByShareToken(String shareToken);

    /** Liste condivise esplicitamente con l'email indicata. */
    @Query("SELECT fl FROM FavoriteList fl JOIN fl.sharedWithEmails e "
            + "WHERE LOWER(e) = LOWER(:email) ORDER BY fl.createdAt DESC")
    List<FavoriteList> findSharedWithEmail(@Param("email") String email);
}
