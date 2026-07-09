package it.unical.ea.Travel.Services.favorite;

import it.unical.ea.Travel.Config.SecurityUtils;
import it.unical.ea.Travel.Entities.favorite.FavoriteList;
import it.unical.ea.Travel.Entities.itinerary.Itinerary;
import it.unical.ea.Travel.Entities.user.User;
import it.unical.ea.Travel.Exception.ApiException;
import it.unical.ea.Travel.Mappers.itinerary.ItineraryMapper;
import it.unical.ea.Travel.Repositories.favorite.FavoriteListRepository;
import it.unical.ea.Travel.Repositories.itinerary.ItineraryRepository;
import it.unical.ea.Travel.Repositories.user.UserRepository;
import it.unical.ea.Travel.Services.audit.AuditLogService;
import it.unical.ea.dtos.favorite.CreateFavoriteListRequest;
import it.unical.ea.dtos.favorite.FavoriteListDto;
import it.unical.ea.dtos.itinerary.ItineraryDto;
import it.unical.ea.enums.FavoriteListVisibility;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FavoriteListService {

    private final FavoriteListRepository favoriteListRepository;
    private final ItineraryRepository itineraryRepository;
    private final UserRepository userRepository;
    private final ItineraryMapper itineraryMapper;
    private final AuditLogService auditLogService;

    private static final SecureRandom RANDOM = new SecureRandom();

    // --- Comandi ---

    @Transactional
    public FavoriteListDto createList(CreateFavoriteListRequest request, String callerEmail) {
        User owner = requireUser(callerEmail);

        FavoriteList list = new FavoriteList();
        list.setName(request.getName().trim());
        list.setOwner(owner);
        applyVisibility(list, request);

        FavoriteList saved = favoriteListRepository.save(list);
        auditLogService.log("CREATE_FAVORITE_LIST", "FavoriteList", saved.getId().toString(),
                "Created favorite list: " + saved.getName());
        return toDto(saved, callerEmail);
    }

    @Transactional
    public FavoriteListDto updateList(String listId, CreateFavoriteListRequest request, String callerEmail) {
        FavoriteList list = requireOwnedList(listId, callerEmail);
        list.setName(request.getName().trim());
        applyVisibility(list, request);
        FavoriteList saved = favoriteListRepository.save(list);
        auditLogService.log("UPDATE_FAVORITE_LIST", "FavoriteList", saved.getId().toString(),
                "Updated favorite list: " + saved.getName());
        return toDto(saved, callerEmail);
    }

    @Transactional
    public void deleteList(String listId, String callerEmail) {
        FavoriteList list = requireOwnedList(listId, callerEmail);
        favoriteListRepository.delete(list);
        auditLogService.log("DELETE_FAVORITE_LIST", "FavoriteList", listId, "Deleted favorite list: " + list.getName());
    }

    @Transactional
    public FavoriteListDto addItinerary(String listId, String itineraryId, String callerEmail) {
        FavoriteList list = requireOwnedList(listId, callerEmail);
        Itinerary itinerary = itineraryRepository.findById(UUID.fromString(itineraryId))
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "itinerary.notFound"));

        boolean alreadyPresent = list.getItineraries().stream()
                .anyMatch(i -> i.getId().equals(itinerary.getId()));
        if (!alreadyPresent) {
            list.getItineraries().add(itinerary);
            favoriteListRepository.save(list);
        }
        return toDto(list, callerEmail);
    }

    @Transactional
    public FavoriteListDto removeItinerary(String listId, String itineraryId, String callerEmail) {
        FavoriteList list = requireOwnedList(listId, callerEmail);
        UUID targetId = UUID.fromString(itineraryId);
        list.getItineraries().removeIf(i -> i.getId().equals(targetId));
        favoriteListRepository.save(list);
        return toDto(list, callerEmail);
    }

    // --- Query ---

    @Transactional(readOnly = true)
    public List<FavoriteListDto> getMyLists(String callerEmail) {
        User owner = requireUser(callerEmail);
        return favoriteListRepository.findByOwnerIdOrderByCreatedAtDesc(owner.getId()).stream()
                .map(list -> toDto(list, callerEmail))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<FavoriteListDto> getSharedWithMe(String callerEmail) {
        return favoriteListRepository.findSharedWithEmail(callerEmail).stream()
                .filter(list -> list.getVisibility() == FavoriteListVisibility.SHARED)
                .map(list -> toDto(list, callerEmail))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public FavoriteListDto getList(String listId, String callerEmail) {
        FavoriteList list = favoriteListRepository.findById(UUID.fromString(listId))
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "favoriteList.notFound"));
        if (!canRead(list, callerEmail)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "error.forbidden");
        }
        return toDto(list, callerEmail);
    }

    /** Accesso pubblico via capability: nessuna autenticazione richiesta. */
    @Transactional(readOnly = true)
    public FavoriteListDto getByShareToken(String shareToken) {
        FavoriteList list = favoriteListRepository.findByShareToken(shareToken)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "favoriteList.notFound"));
        if (list.getVisibility() != FavoriteListVisibility.PUBLIC) {
            // Il token esisteva ma la lista è tornata privata/condivisa: il link non vale più.
            throw new ApiException(HttpStatus.NOT_FOUND, "favoriteList.notFound");
        }
        return toDto(list, null);
    }

    // --- Regole di accesso ---

    private boolean canRead(FavoriteList list, String callerEmail) {
        if (callerEmail != null && list.getOwner().getEmail().equalsIgnoreCase(callerEmail)) {
            return true;
        }
        return switch (list.getVisibility()) {
            case PUBLIC -> true;
            case SHARED -> callerEmail != null && list.getSharedWithEmails().stream()
                    .anyMatch(e -> e.equalsIgnoreCase(callerEmail));
            case PRIVATE -> false;
        };
    }

    private FavoriteList requireOwnedList(String listId, String callerEmail) {
        FavoriteList list = favoriteListRepository.findById(UUID.fromString(listId))
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "favoriteList.notFound"));
        if (!list.getOwner().getEmail().equalsIgnoreCase(callerEmail)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "error.forbidden");
        }
        return list;
    }

    private User requireUser(String email) {
        return userRepository.getUserByEmail(email)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "user.notFound"));
    }

    private void applyVisibility(FavoriteList list, CreateFavoriteListRequest request) {
        FavoriteListVisibility visibility = request.getVisibility() != null
                ? request.getVisibility()
                : FavoriteListVisibility.PRIVATE;
        list.setVisibility(visibility);

        // Normalizza gli stati collaterali in base alla visibilità scelta, così che
        // un token pubblico non sopravviva a un ritorno a PRIVATE.
        if (visibility == FavoriteListVisibility.SHARED) {
            list.getSharedWithEmails().clear();
            if (request.getSharedWithEmails() != null) {
                request.getSharedWithEmails().stream()
                        .filter(e -> e != null && !e.isBlank())
                        .map(e -> e.trim().toLowerCase())
                        .forEach(list.getSharedWithEmails()::add);
            }
        } else {
            list.getSharedWithEmails().clear();
        }

        if (visibility == FavoriteListVisibility.PUBLIC) {
            if (list.getShareToken() == null) {
                list.setShareToken(generateShareToken());
            }
        } else {
            list.setShareToken(null);
        }
    }

    private String generateShareToken() {
        byte[] bytes = new byte[24];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    // --- Mapping ---

    private FavoriteListDto toDto(FavoriteList list, String callerEmail) {
        FavoriteListDto dto = new FavoriteListDto();
        dto.setId(list.getId());
        dto.setName(list.getName());
        dto.setVisibility(list.getVisibility());
        dto.setOwnerId(list.getOwner().getId());
        dto.setOwnerName(displayName(list.getOwner()));
        dto.setSharedWithEmails(list.getSharedWithEmails().stream().sorted().collect(Collectors.toList()));
        dto.setShareToken(list.getShareToken());
        dto.setCreatedAt(list.getCreatedAt());
        dto.setEditable(callerEmail != null && list.getOwner().getEmail().equalsIgnoreCase(callerEmail));

        List<ItineraryDto> itineraryDtos = list.getItineraries().stream()
                .map(this::toItineraryDto)
                .collect(Collectors.toList());
        dto.setItineraries(itineraryDtos);
        return dto;
    }

    private ItineraryDto toItineraryDto(Itinerary itinerary) {
        ItineraryDto dto = itineraryMapper.toDTO(itinerary);
        if (itinerary.getImagePath() != null) {
            String imageUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/itinerary/")
                    .path(itinerary.getId().toString())
                    .path("/image")
                    .toUriString();
            dto.setImageUrl(imageUrl);
        }
        return dto;
    }

    private String displayName(User user) {
        if (user == null) {
            return "Sconosciuto";
        }
        if (user.getCompanyName() != null && !user.getCompanyName().isBlank()) {
            return user.getCompanyName();
        }
        String first = user.getFirstName() != null ? user.getFirstName() : "";
        String last = user.getLastName() != null ? user.getLastName() : "";
        String full = (first + " " + last).trim();
        return full.isEmpty() ? user.getEmail() : full;
    }
}
