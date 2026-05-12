package it.unical.ea.Travel.Controllers.favorite;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import it.unical.ea.Travel.DTOs.favorite.FavoriteListItemRequestDTO;
import it.unical.ea.Travel.DTOs.favorite.FavoriteListItemResponseDTO;
import it.unical.ea.Travel.Services.favorite.FavoriteListItemService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/FavoriteListItem")
public class FavoriteListItemController {

    private final FavoriteListItemService favoriteListItemService;

    public FavoriteListItemController(FavoriteListItemService favoriteListItemService) {
        this.favoriteListItemService = favoriteListItemService;
    }

    @PostMapping
    public FavoriteListItemResponseDTO saveFavoriteListItem(@Valid@RequestBody FavoriteListItemRequestDTO request) {
        return favoriteListItemService.saveFavoriteListItem(request);
    }

    @GetMapping("/{stringId}")
    public FavoriteListItemResponseDTO getFavoriteListItem(@PathVariable String stringId) {
        return favoriteListItemService.getFavoriteListItem(stringId);
    }

    @GetMapping
    public List<FavoriteListItemResponseDTO> getFavoriteListItems(@RequestParam(required = false) String favoriteListId) {
        if (favoriteListId != null && !favoriteListId.isBlank()) {
            return favoriteListItemService.getFavoriteListItemsByFavoriteList(favoriteListId);
        }
        return favoriteListItemService.getFavoriteListItems();
    }

    @DeleteMapping("/{stringId}")
    public void deleteFavoriteListItem(@PathVariable String stringId) {
        favoriteListItemService.deleteFavoriteListItem(stringId);
    }
}
