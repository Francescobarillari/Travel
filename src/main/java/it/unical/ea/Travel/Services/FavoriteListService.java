package it.unical.ea.Travel.Services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import it.unical.ea.Travel.Entities.FavoriteList;
import it.unical.ea.Travel.Repositories.FavoriteListRepository;

@Service
public class FavoriteListService {

    private final FavoriteListRepository favoriteListRepository;

    public FavoriteListService(FavoriteListRepository favoriteListRepository) {
        this.favoriteListRepository = favoriteListRepository;
    }

    public FavoriteList saveFavoriteList(FavoriteList favoriteList) {
        return favoriteListRepository.save(favoriteList);
    }

    public FavoriteList getFavoriteList(String stringId) {
        UUID uuid = UUID.fromString(stringId);
        return favoriteListRepository.findById(uuid)
                .filter(favoriteList -> !favoriteList.isDeleted())
                .orElseThrow(() -> new RuntimeException("Lista preferiti non trovata"));
    }

    public List<FavoriteList> getFavoriteLists() {
        return favoriteListRepository.findByDeletedAtIsNull();
    }

    public List<FavoriteList> getFavoriteListsByOwner(String ownerId) {
        UUID uuid = UUID.fromString(ownerId);
        return favoriteListRepository.findByOwnerIdAndDeletedAtIsNull(uuid);
    }

    public void updateFavoriteList() {
        return; // da implementare
    }

    public void deleteFavoriteList(String stringId) {
        FavoriteList favoriteList = getFavoriteList(stringId);
        favoriteList.setDeletedAt(LocalDateTime.now());
        favoriteListRepository.save(favoriteList);
    }
}
