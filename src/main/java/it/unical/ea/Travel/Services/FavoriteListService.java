package it.unical.ea.Travel.Services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.unical.ea.Travel.DTOs.FavoriteListDTO;
import it.unical.ea.Travel.Entities.FavoriteList;
import it.unical.ea.Travel.Repositories.FavoriteListRepository;

@Service
public class FavoriteListService {

    private final FavoriteListRepository favoriteListRepository;

    public FavoriteListService(FavoriteListRepository favoriteListRepository) {
        this.favoriteListRepository = favoriteListRepository;
    }

    @Transactional
    public FavoriteListDTO saveFavoriteList(FavoriteList favoriteList) {
        FavoriteList savedFavoriteList = favoriteListRepository.save(favoriteList);
        return mapToDTO(getFavoriteListEntity(savedFavoriteList.getId()));
    }

    @Transactional(readOnly = true)
    public FavoriteListDTO getFavoriteList(String stringId) {
        UUID uuid = UUID.fromString(stringId);
        return mapToDTO(getFavoriteListEntity(uuid));
    }

    @Transactional(readOnly = true)
    public List<FavoriteListDTO> getFavoriteLists() {
        return favoriteListRepository.findByDeletedAtIsNull()
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<FavoriteListDTO> getFavoriteListsByOwner(String ownerId) {
        UUID uuid = UUID.fromString(ownerId);
        return favoriteListRepository.findByOwnerIdAndDeletedAtIsNull(uuid)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    public void updateFavoriteList() {
        return; // da implementare
    }

    @Transactional
    public void deleteFavoriteList(String stringId) {
        FavoriteList favoriteList = getFavoriteListEntity(stringId);
        favoriteList.setDeletedAt(LocalDateTime.now());
        favoriteListRepository.save(favoriteList);
    }

    private FavoriteList getFavoriteListEntity(String stringId) {
        UUID uuid = UUID.fromString(stringId);
        return getFavoriteListEntity(uuid);
    }

    private FavoriteList getFavoriteListEntity(UUID uuid) {
        return favoriteListRepository.findById(uuid)
                .filter(favoriteList -> !favoriteList.isDeleted())
                .orElseThrow(() -> new RuntimeException("Lista preferiti non trovata"));
    }

    private FavoriteListDTO mapToDTO(FavoriteList favoriteList) {
        return new FavoriteListDTO(
                favoriteList.getId(),
                favoriteList.getName(),
                favoriteList.getDescription(),
                favoriteList.getVisibility() != null ? favoriteList.getVisibility().name() : null,
                favoriteList.getCreatedAt(),
                favoriteList.getUpdatedAt(),
                favoriteList.getOwner() != null ? favoriteList.getOwner().getId() : null
        );
    }
}
