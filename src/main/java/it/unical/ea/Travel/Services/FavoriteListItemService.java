package it.unical.ea.Travel.Services;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import it.unical.ea.Travel.Entities.FavoriteListItem;
import it.unical.ea.Travel.Repositories.FavoriteListItemRepository;

@Service
public class FavoriteListItemService {

    private final FavoriteListItemRepository favoriteListItemRepository;

    public FavoriteListItemService(FavoriteListItemRepository favoriteListItemRepository) {
        this.favoriteListItemRepository = favoriteListItemRepository;
    }

    public FavoriteListItem saveFavoriteListItem(FavoriteListItem favoriteListItem) {
        return favoriteListItemRepository.save(favoriteListItem);
    }

    public FavoriteListItem getFavoriteListItem(String stringId) {
        UUID uuid = UUID.fromString(stringId);
        return favoriteListItemRepository.findById(uuid)
                .orElseThrow(() -> new RuntimeException("Elemento lista preferiti non trovato"));
    }

    public List<FavoriteListItem> getFavoriteListItems() {
        return favoriteListItemRepository.findAll();
    }

    public List<FavoriteListItem> getFavoriteListItemsByFavoriteList(String favoriteListId) {
        UUID uuid = UUID.fromString(favoriteListId);
        return favoriteListItemRepository.findByFavoriteListId(uuid);
    }

    public void updateFavoriteListItem() {
        return; // da implementare
    }

    public void deleteFavoriteListItem(String stringId) {
        UUID uuid = UUID.fromString(stringId);
        favoriteListItemRepository.deleteById(uuid);
    }
}
