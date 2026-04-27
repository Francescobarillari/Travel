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

import it.unical.ea.Travel.DTOs.favorite.FavoriteListRequestDTO;
import it.unical.ea.Travel.DTOs.favorite.FavoriteListResponseDTO;
import it.unical.ea.Travel.Services.favorite.FavoriteListService;

@RestController
@RequestMapping("/FavoriteList")
public class FavoriteListController{
    private final FavoriteListService favoriteListService;

    public FavoriteListController( FavoriteListService favoriteListService){
        this.favoriteListService= favoriteListService;
    }

    @PostMapping
    public FavoriteListResponseDTO saveFavoriteList(@RequestBody FavoriteListRequestDTO request){
        return favoriteListService.saveFavoriteList(request);
    }

    @GetMapping("/{stringId}")
    public FavoriteListResponseDTO getFavoriteList(@PathVariable String stringId){
        return favoriteListService.getFavoriteList(stringId);
    }

    @GetMapping
    public List<FavoriteListResponseDTO> getFavoriteLists(@RequestParam(required = false) String ownerId){
        if (ownerId != null && !ownerId.isBlank()) {
            return favoriteListService.getFavoriteListsByOwner(ownerId);
        }
        return favoriteListService.getFavoriteLists();
    }

    @DeleteMapping
    public void deleteFavoriteList(String stringIdString)
    {
        favoriteListService.deleteFavoriteList(stringIdString);
    }
}
