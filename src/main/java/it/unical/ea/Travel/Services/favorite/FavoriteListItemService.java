package it.unical.ea.Travel.Services.favorite;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.unical.ea.Travel.DTOs.favorite.FavoriteListItemRequestDTO;
import it.unical.ea.Travel.DTOs.favorite.FavoriteListItemResponseDTO;
import it.unical.ea.Travel.Entities.experience.Experience;
import it.unical.ea.Travel.Entities.favorite.FavoriteList;
import it.unical.ea.Travel.Entities.favorite.FavoriteListItem;
import it.unical.ea.Travel.Mappers.favorite.FavoriteListItemMapper;
import it.unical.ea.Travel.Repositories.experience.ExperienceRepository;
import it.unical.ea.Travel.Repositories.favorite.FavoriteListRepository;
import it.unical.ea.Travel.Repositories.favorite.FavoriteListItemRepository;

@Service
public class FavoriteListItemService {

    private static final Logger logger = LoggerFactory.getLogger(FavoriteListItemService.class);

    private final FavoriteListItemRepository favoriteListItemRepository;
    private final FavoriteListRepository favoriteListRepository;
    private final ExperienceRepository experienceRepository;
    private final FavoriteListItemMapper favoriteListItemMapper;

    public FavoriteListItemService(
            FavoriteListItemRepository favoriteListItemRepository,
            FavoriteListRepository favoriteListRepository,
            ExperienceRepository experienceRepository,
            FavoriteListItemMapper favoriteListItemMapper) {
        this.favoriteListItemRepository = favoriteListItemRepository;
        this.favoriteListRepository = favoriteListRepository;
        this.experienceRepository = experienceRepository;
        this.favoriteListItemMapper = favoriteListItemMapper;
    }

    @Transactional
    public FavoriteListItemResponseDTO saveFavoriteListItem(FavoriteListItemRequestDTO request) {
        logger.info("Creating favorite list item for favoriteListId={} experienceId={}",
                request.favoriteListId(), request.experienceId());
        FavoriteList favoriteList = getFavoriteList(request.favoriteListId());
        Experience experience = getExperience(request.experienceId());
        FavoriteListItem favoriteListItem = favoriteListItemMapper.toEntity(request, favoriteList, experience);
        FavoriteListItem savedFavoriteListItem = favoriteListItemRepository.save(favoriteListItem);
        logger.info("Created favorite list item id={} for favoriteListId={} experienceId={}",
                savedFavoriteListItem.getId(), favoriteList.getId(), experience.getId());
        return favoriteListItemMapper.toResponseDTO(getFavoriteListItemEntity(savedFavoriteListItem.getId()));
    }

    @Transactional(readOnly = true)
    public FavoriteListItemResponseDTO getFavoriteListItem(String stringId) {
        logger.debug("Fetching favorite list item id={}", stringId);
        UUID uuid = UUID.fromString(stringId);
        return favoriteListItemMapper.toResponseDTO(getFavoriteListItemEntity(uuid));
    }

    @Transactional(readOnly = true)
    public List<FavoriteListItemResponseDTO> getFavoriteListItems() {
        logger.debug("Fetching all favorite list items");
        List<FavoriteListItemResponseDTO> favoriteListItems = favoriteListItemRepository.findAll()
                .stream()
                .map(favoriteListItemMapper::toResponseDTO)
                .toList();
        logger.debug("Fetched {} favorite list items", favoriteListItems.size());
        return favoriteListItems;
    }

    @Transactional(readOnly = true)
    public List<FavoriteListItemResponseDTO> getFavoriteListItemsByFavoriteList(String favoriteListId) {
        logger.debug("Fetching favorite list items for favoriteListId={}", favoriteListId);
        UUID uuid = UUID.fromString(favoriteListId);
        List<FavoriteListItemResponseDTO> favoriteListItems = favoriteListItemRepository.findByFavoriteListId(uuid)
                .stream()
                .map(favoriteListItemMapper::toResponseDTO)
                .toList();
        logger.debug("Fetched {} favorite list items for favoriteListId={}",
                favoriteListItems.size(), favoriteListId);
        return favoriteListItems;
    }

    public void updateFavoriteListItem() {
        return; // da implementare
    }

    @Transactional
    public void deleteFavoriteListItem(String stringId) {
        logger.info("Deleting favorite list item id={}", stringId);
        UUID uuid = UUID.fromString(stringId);
        favoriteListItemRepository.deleteById(uuid);
        logger.info("Deletion request completed for favorite list item id={}", stringId);
    }

    private FavoriteListItem getFavoriteListItemEntity(UUID uuid) {
        return favoriteListItemRepository.findById(uuid)
                .orElseThrow(() -> {
                    logger.warn("Favorite list item not found for id={}", uuid);
                    return new RuntimeException("Elemento lista preferiti non trovato");
                });
    }

    private FavoriteList getFavoriteList(UUID favoriteListId) {
        if (favoriteListId == null) {
            logger.warn("Missing favoriteListId while handling favorite list item operation");
            throw new RuntimeException("Il favoriteListId e' obbligatorio");
        }

        return favoriteListRepository.findByIdAndDeletedAtIsNull(favoriteListId)
                .orElseThrow(() -> {
                    logger.warn("Favorite list not found for id={}", favoriteListId);
                    return new RuntimeException("Lista preferiti non trovata");
                });
    }

    private Experience getExperience(UUID experienceId) {
        if (experienceId == null) {
            logger.warn("Missing experienceId while handling favorite list item operation");
            throw new RuntimeException("L'experienceId e' obbligatorio");
        }

        return experienceRepository.findByIdAndDeletedAtIsNull(experienceId)
                .orElseThrow(() -> {
                    logger.warn("Experience not found for id={}", experienceId);
                    return new RuntimeException("Experience non trovata");
                });
    }
}
