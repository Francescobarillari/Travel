package it.unical.ea.Travel.Services.favorite;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.unical.ea.Travel.DTOs.favorite.FavoriteListRequestDTO;
import it.unical.ea.Travel.DTOs.favorite.FavoriteListResponseDTO;
import it.unical.ea.Travel.Entities.favorite.FavoriteList;
import it.unical.ea.Travel.Entities.user.User;
import it.unical.ea.Travel.Mappers.favorite.FavoriteListMapper;
import it.unical.ea.Travel.Repositories.favorite.FavoriteListRepository;
import it.unical.ea.Travel.Repositories.user.UserRepository;

@Service
public class FavoriteListService {

    private static final Logger logger = LoggerFactory.getLogger(FavoriteListService.class);

    private final FavoriteListRepository favoriteListRepository;
    private final UserRepository userRepository;
    private final FavoriteListMapper favoriteListMapper;

    public FavoriteListService(
            FavoriteListRepository favoriteListRepository,
            UserRepository userRepository,
            FavoriteListMapper favoriteListMapper) {
        this.favoriteListRepository = favoriteListRepository;
        this.userRepository = userRepository;
        this.favoriteListMapper = favoriteListMapper;
    }

    @Transactional
    public FavoriteListResponseDTO saveFavoriteList(FavoriteListRequestDTO request) {
        logger.info("Creating favorite list for ownerId={} name='{}' visibility={}",
                request.ownerId(), request.name(), request.visibility());
        User owner = getOwner(request.ownerId());
        FavoriteList favoriteList = favoriteListMapper.toEntity(request, owner);
        FavoriteList savedFavoriteList = favoriteListRepository.save(favoriteList);
        logger.info("Created favorite list id={} for ownerId={}",
                savedFavoriteList.getId(), owner.getId());
        return favoriteListMapper.toResponseDTO(getFavoriteListEntity(savedFavoriteList.getId()));
    }

    @Transactional(readOnly = true)
    public FavoriteListResponseDTO getFavoriteList(String stringId) {
        logger.debug("Fetching favorite list id={}", stringId);
        UUID uuid = UUID.fromString(stringId);
        return favoriteListMapper.toResponseDTO(getFavoriteListEntity(uuid));
    }

    @Transactional(readOnly = true)
    public List<FavoriteListResponseDTO> getFavoriteLists() {
        logger.debug("Fetching all favorite lists");
        List<FavoriteListResponseDTO> favoriteLists = favoriteListRepository.findByDeletedAtIsNull()
                .stream()
                .map(favoriteListMapper::toResponseDTO)
                .toList();
        logger.debug("Fetched {} favorite lists", favoriteLists.size());
        return favoriteLists;
    }

    @Transactional(readOnly = true)
    public List<FavoriteListResponseDTO> getFavoriteListsByOwner(String ownerId) {
        logger.debug("Fetching favorite lists for ownerId={}", ownerId);
        UUID uuid = UUID.fromString(ownerId);
        List<FavoriteListResponseDTO> favoriteLists = favoriteListRepository.findByOwnerIdAndDeletedAtIsNull(uuid)
                .stream()
                .map(favoriteListMapper::toResponseDTO)
                .toList();
        logger.debug("Fetched {} favorite lists for ownerId={}", favoriteLists.size(), ownerId);
        return favoriteLists;
    }

    public void updateFavoriteList() {
        return; // da implementare
    }

    @Transactional
    public void deleteFavoriteList(String stringId) {
        logger.info("Soft deleting favorite list id={}", stringId);
        FavoriteList favoriteList = getFavoriteListEntity(stringId);
        favoriteList.setDeletedAt(LocalDateTime.now());
        favoriteListRepository.save(favoriteList);
        logger.info("Soft deleted favorite list id={}", favoriteList.getId());
    }

    private FavoriteList getFavoriteListEntity(String stringId) {
        UUID uuid = UUID.fromString(stringId);
        return getFavoriteListEntity(uuid);
    }

    private FavoriteList getFavoriteListEntity(UUID uuid) {
        return favoriteListRepository.findByIdAndDeletedAtIsNull(uuid)
                .orElseThrow(() -> {
                    logger.warn("Favorite list not found for id={}", uuid);
                    return new RuntimeException("Lista preferiti non trovata");
                });
    }

    private User getOwner(UUID ownerId) {
        if (ownerId == null) {
            logger.warn("Missing ownerId while handling favorite list operation");
            throw new RuntimeException("L'ownerId e' obbligatorio");
        }

        return userRepository.findById(ownerId)
                .filter(user -> !user.isDeleted())
                .orElseThrow(() -> {
                    logger.warn("Owner not found or deleted for ownerId={}", ownerId);
                    return new RuntimeException("Owner non trovato");
                });
    }
}
