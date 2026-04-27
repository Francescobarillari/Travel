package it.unical.ea.Travel.Services;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.unical.ea.Travel.DTOs.FavoriteListItemDTO;
import it.unical.ea.Travel.Entities.FavoriteListItem;
import it.unical.ea.Travel.Repositories.FavoriteListItemRepository;

@Service
public class FavoriteListItemService {

    private final FavoriteListItemRepository favoriteListItemRepository;

    public FavoriteListItemService(FavoriteListItemRepository favoriteListItemRepository) {
        this.favoriteListItemRepository = favoriteListItemRepository;
    }

    @Transactional
    public FavoriteListItemDTO saveFavoriteListItem(FavoriteListItem favoriteListItem) {
        FavoriteListItem savedFavoriteListItem = favoriteListItemRepository.save(favoriteListItem);
        return mapToDTO(getFavoriteListItemEntity(savedFavoriteListItem.getId()));
    }

    @Transactional(readOnly = true)
    public FavoriteListItemDTO getFavoriteListItem(String stringId) {
        UUID uuid = UUID.fromString(stringId);
        return mapToDTO(getFavoriteListItemEntity(uuid));
    }

    @Transactional(readOnly = true)
    public List<FavoriteListItemDTO> getFavoriteListItems() {
        return favoriteListItemRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<FavoriteListItemDTO> getFavoriteListItemsByFavoriteList(String favoriteListId) {
        UUID uuid = UUID.fromString(favoriteListId);
        return favoriteListItemRepository.findByFavoriteListId(uuid)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    public void updateFavoriteListItem() {
        return; // da implementare
    }

    @Transactional
    public void deleteFavoriteListItem(String stringId) {
        UUID uuid = UUID.fromString(stringId);
        favoriteListItemRepository.deleteById(uuid);
    }

    private FavoriteListItem getFavoriteListItemEntity(UUID uuid) {
        return favoriteListItemRepository.findById(uuid)
                .orElseThrow(() -> new RuntimeException("Elemento lista preferiti non trovato"));
    }

    private FavoriteListItemDTO mapToDTO(FavoriteListItem favoriteListItem) {
        return new FavoriteListItemDTO(
                favoriteListItem.getId(),
                favoriteListItem.getAddedAt(),
                favoriteListItem.getNotes(),
                favoriteListItem.getFavoriteList() != null ? favoriteListItem.getFavoriteList().getId() : null,
                favoriteListItem.getExperience() != null ? favoriteListItem.getExperience().getId() : null
        );
    }
}
