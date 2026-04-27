package it.unical.ea.Travel.Services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.unical.ea.Travel.DTOs.favorite.FavoriteListRequestDTO;
import it.unical.ea.Travel.DTOs.favorite.FavoriteListResponseDTO;
import it.unical.ea.Travel.Entities.FavoriteList;
import it.unical.ea.Travel.Entities.User;
import it.unical.ea.Travel.Mappers.FavoriteListMapper;
import it.unical.ea.Travel.Repositories.FavoriteListRepository;
import it.unical.ea.Travel.Repositories.UserRepository;

@Service
public class FavoriteListService {

    private final FavoriteListRepository favoriteListRepository;
    private final UserRepository userRepository;

    public FavoriteListService(FavoriteListRepository favoriteListRepository, UserRepository userRepository) {
        this.favoriteListRepository = favoriteListRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public FavoriteListResponseDTO saveFavoriteList(FavoriteListRequestDTO request) {
        User owner = getOwner(request.ownerId());
        FavoriteList favoriteList = FavoriteListMapper.toEntity(request, owner);
        FavoriteList savedFavoriteList = favoriteListRepository.save(favoriteList);
        return FavoriteListMapper.toResponseDTO(getFavoriteListEntity(savedFavoriteList.getId()));
    }

    @Transactional(readOnly = true)
    public FavoriteListResponseDTO getFavoriteList(String stringId) {
        UUID uuid = UUID.fromString(stringId);
        return FavoriteListMapper.toResponseDTO(getFavoriteListEntity(uuid));
    }

    @Transactional(readOnly = true)
    public List<FavoriteListResponseDTO> getFavoriteLists() {
        return favoriteListRepository.findByDeletedAtIsNull()
                .stream()
                .map(FavoriteListMapper::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<FavoriteListResponseDTO> getFavoriteListsByOwner(String ownerId) {
        UUID uuid = UUID.fromString(ownerId);
        return favoriteListRepository.findByOwnerIdAndDeletedAtIsNull(uuid)
                .stream()
                .map(FavoriteListMapper::toResponseDTO)
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
        return favoriteListRepository.findByIdAndDeletedAtIsNull(uuid)
                .orElseThrow(() -> new RuntimeException("Lista preferiti non trovata"));
    }

    private User getOwner(UUID ownerId) {
        if (ownerId == null) {
            throw new RuntimeException("L'ownerId e' obbligatorio");
        }

        return userRepository.findById(ownerId)
                .filter(user -> !user.isDeleted())
                .orElseThrow(() -> new RuntimeException("Owner non trovato"));
    }
}
