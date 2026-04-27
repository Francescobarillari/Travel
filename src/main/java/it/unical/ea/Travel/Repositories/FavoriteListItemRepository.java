package it.unical.ea.Travel.Repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import it.unical.ea.Travel.Entities.FavoriteListItem;

@Repository
public interface FavoriteListItemRepository extends JpaRepository<FavoriteListItem, UUID> {

    @Override
    @EntityGraph(attributePaths = {"favoriteList", "favoriteList.owner", "experience", "experience.organizer"})
    Optional<FavoriteListItem> findById(UUID id);

    @Override
    @EntityGraph(attributePaths = {"favoriteList", "favoriteList.owner", "experience", "experience.organizer"})
    List<FavoriteListItem> findAll();

    @EntityGraph(attributePaths = {"favoriteList", "favoriteList.owner", "experience", "experience.organizer"})
    List<FavoriteListItem> findByFavoriteListId(UUID favoriteListId);
}
