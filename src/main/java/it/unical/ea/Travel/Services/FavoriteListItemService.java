package it.unical.ea.Travel.Services;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.unical.ea.Travel.DTOs.favorite.FavoriteListItemRequestDTO;
import it.unical.ea.Travel.DTOs.favorite.FavoriteListItemResponseDTO;
import it.unical.ea.Travel.Entities.Experience;
import it.unical.ea.Travel.Entities.FavoriteList;
import it.unical.ea.Travel.Entities.FavoriteListItem;
import it.unical.ea.Travel.Mappers.FavoriteListItemMapper;
import it.unical.ea.Travel.Repositories.ExperienceRepository;
import it.unical.ea.Travel.Repositories.FavoriteListRepository;
import it.unical.ea.Travel.Repositories.FavoriteListItemRepository;

@Service
public class FavoriteListItemService {

    private final FavoriteListItemRepository favoriteListItemRepository;
    private final FavoriteListRepository favoriteListRepository;
    private final ExperienceRepository experienceRepository;

    public FavoriteListItemService(
            FavoriteListItemRepository favoriteListItemRepository,
            FavoriteListRepository favoriteListRepository,
            ExperienceRepository experienceRepository) {
        this.favoriteListItemRepository = favoriteListItemRepository;
        this.favoriteListRepository = favoriteListRepository;
        this.experienceRepository = experienceRepository;
    }

    @Transactional
    public FavoriteListItemResponseDTO saveFavoriteListItem(FavoriteListItemRequestDTO request) {
        FavoriteList favoriteList = getFavoriteList(request.favoriteListId());
        Experience experience = getExperience(request.experienceId());
        FavoriteListItem favoriteListItem = FavoriteListItemMapper.toEntity(request, favoriteList, experience);
        FavoriteListItem savedFavoriteListItem = favoriteListItemRepository.save(favoriteListItem);
        return FavoriteListItemMapper.toResponseDTO(getFavoriteListItemEntity(savedFavoriteListItem.getId()));
    }

    @Transactional(readOnly = true)
    public FavoriteListItemResponseDTO getFavoriteListItem(String stringId) {
        UUID uuid = UUID.fromString(stringId);
        return FavoriteListItemMapper.toResponseDTO(getFavoriteListItemEntity(uuid));
    }

    @Transactional(readOnly = true)
    public List<FavoriteListItemResponseDTO> getFavoriteListItems() {
        return favoriteListItemRepository.findAll()
                .stream()
                .map(FavoriteListItemMapper::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<FavoriteListItemResponseDTO> getFavoriteListItemsByFavoriteList(String favoriteListId) {
        UUID uuid = UUID.fromString(favoriteListId);
        return favoriteListItemRepository.findByFavoriteListId(uuid)
                .stream()
                .map(FavoriteListItemMapper::toResponseDTO)
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

    private FavoriteList getFavoriteList(UUID favoriteListId) {
        if (favoriteListId == null) {
            throw new RuntimeException("Il favoriteListId e' obbligatorio");
        }

        return favoriteListRepository.findByIdAndDeletedAtIsNull(favoriteListId)
                .orElseThrow(() -> new RuntimeException("Lista preferiti non trovata"));
    }

    private Experience getExperience(UUID experienceId) {
        if (experienceId == null) {
            throw new RuntimeException("L'experienceId e' obbligatorio");
        }

        return experienceRepository.findByIdAndDeletedAtIsNull(experienceId)
                .orElseThrow(() -> new RuntimeException("Experience non trovata"));
    }
}
