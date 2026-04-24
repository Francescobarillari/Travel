package it.unical.ea.Travel.Repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import it.unical.ea.Travel.Entities.FavoriteListItem;

@Repository
public interface FavoriteListItemRepository extends JpaRepository<FavoriteListItem, UUID> {

    List<FavoriteListItem> findByFavoriteListId(UUID favoriteListId);
}
