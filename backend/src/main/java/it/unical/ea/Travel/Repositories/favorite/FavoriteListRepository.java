package it.unical.ea.Travel.Repositories.favorite;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import it.unical.ea.Travel.Entities.favorite.FavoriteList;

@Repository
public interface FavoriteListRepository extends JpaRepository<FavoriteList, UUID> {

    @EntityGraph(attributePaths = "owner")
    Optional<FavoriteList> findByIdAndDeletedAtIsNull(UUID id);

    @EntityGraph(attributePaths = "owner")
    List<FavoriteList> findByDeletedAtIsNull();

    @EntityGraph(attributePaths = "owner")
    List<FavoriteList> findByOwnerIdAndDeletedAtIsNull(UUID ownerId);
}
