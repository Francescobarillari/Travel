package it.unical.ea.Travel.Services.location;

import it.unical.ea.Travel.Entities.location.Location;
import it.unical.ea.Travel.Mappers.location.LocationMapper;
import it.unical.ea.Travel.Repositories.location.LocationRepository;
import it.unical.ea.dtos.location.LocationDto;
import it.unical.ea.Travel.Exception.ApiException;
import lombok.RequiredArgsConstructor;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LocationService {

    private final LocationRepository locationRepository;
    private final LocationMapper locationMapper;

    @Transactional(readOnly = true)
    public Page<LocationDto> searchLocation(String keyword, boolean includeExternal, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        String safeKeyword = (keyword == null) ? "" : keyword.trim();
        
        // 1. Cerca nel DB locale
        Page<Location> localLocations = locationRepository.searchByKeyword(safeKeyword, pageable);
        List<LocationDto> mergedResults = new java.util.ArrayList<>();
        for (Location loc : localLocations.getContent()) {
            if (!includeExternal && (loc.getImageUrl() == null || loc.getImageUrl().trim().isEmpty())) {
                continue;
            }
            mergedResults.add(locationMapper.toDto(loc));
        }

        // 2. Se includeExternal è true e l'utente sta digitando (almeno 2 caratteri) ed è la prima pagina, chiedi a Nominatim
        if (includeExternal && !safeKeyword.isEmpty() && page == 0 && safeKeyword.length() >= 2) {
            List<LocationDto> onlineSuggestions = fetchSuggestionsFromNominatim(safeKeyword);
            for (LocationDto suggestion : onlineSuggestions) {
                // Evita duplicati con quelli locali confrontando la prima parte del nome
                boolean exists = mergedResults.stream()
                        .anyMatch(loc -> {
                            String name1 = loc.getName().toLowerCase().split(",")[0].trim();
                            String name2 = suggestion.getName().toLowerCase().split(",")[0].trim();
                            return name1.equals(name2) 
                                || name2.startsWith(name1) 
                                || name1.startsWith(name2) 
                                || loc.getName().equalsIgnoreCase(suggestion.getName());
                        });
                if (!exists) {
                    mergedResults.add(suggestion);
                }
            }
        }

        // Tronca la lista in base al size richiesto
        int end = Math.min(mergedResults.size(), size);
        List<LocationDto> subList = mergedResults.subList(0, end);

        return new org.springframework.data.domain.PageImpl<>(
            subList,
            pageable,
            Math.max(localLocations.getTotalElements(), mergedResults.size())
        );
    }

    private List<LocationDto> fetchSuggestionsFromNominatim(String query) {
        List<LocationDto> suggestions = new java.util.ArrayList<>();
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "TravelApp/1.0 (contact: admin@travelapp.com)");
            HttpEntity<String> entity = new HttpEntity<>(headers);

            String url = UriComponentsBuilder.fromUriString("https://nominatim.openstreetmap.org/search")
                    .queryParam("q", query)
                    .queryParam("format", "json")
                    .queryParam("limit", 5)
                    .build()
                    .toUriString();

            ResponseEntity<List> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    List.class);
            List<?> body = response.getBody();
            if (body != null) {
                for (Object item : body) {
                    if (item instanceof java.util.Map) {
                        java.util.Map<?, ?> map = (java.util.Map<?, ?>) item;
                        String displayName = (String) map.get("display_name");
                        if (displayName != null) {
                            LocationDto dto = new LocationDto();
                            dto.setName(displayName);
                            dto.setDescription("Trovato tramite OpenStreetMap.");
                            suggestions.add(dto);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to fetch suggestions from Nominatim: " + e.getMessage());
        }
        return suggestions;
    }

    @Transactional(readOnly = true)
    public Optional<LocationDto> getLocationById(UUID id) {
        return locationRepository.findById(id).map(locationMapper::toDto);
    }

    @Transactional
    public Location getOrCreateLocation(String locationName) {
        if (locationName == null || locationName.trim().isEmpty()) {
            return null;
        }

        String trimmedName = locationName.trim();

        // 1. Cerca nel database (case-insensitive - corrispondenza esatta)
        Optional<Location> existing = locationRepository.findByNameIgnoreCase(trimmedName);
        if (existing.isPresent()) {
            return existing.get();
        }

        // 2. Cerca una corrispondenza intelligente/parziale con le località esistenti nel DB
        List<Location> allLocations = locationRepository.findAll();
        for (Location loc : allLocations) {
            String dbName = loc.getName().toLowerCase();
            String targetName = trimmedName.toLowerCase();
            
            String dbCity = dbName.split(",")[0].trim();
            String targetCity = targetName.split(",")[0].trim();

            if (dbCity.equals(targetCity) || dbName.contains(targetCity) || targetName.contains(dbCity)) {
                return loc; // Associa direttamente alla città preesistente!
            }
        }

        // 3. Verifica tramite l'API OpenStreetMap Nominatim
        boolean isValid = verifyLocationWithNominatim(trimmedName);
        if (!isValid) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "location.invalidName");
        }

        // 4. Crea e salva la nuova Location (senza immagine)
        Location newLocation = new Location();
        newLocation.setName(trimmedName);
        newLocation.setImageUrl(null);
        newLocation.setDescription("Località verificata tramite OpenStreetMap.");

        return locationRepository.save(newLocation);
    }

    private boolean verifyLocationWithNominatim(String name) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "TravelApp/1.0 (contact: admin@travelapp.com)");
            HttpEntity<String> entity = new HttpEntity<>(headers);

            String url = UriComponentsBuilder.fromUriString("https://nominatim.openstreetmap.org/search")
                    .queryParam("q", name)
                    .queryParam("format", "json")
                    .queryParam("limit", 1)
                    .build()
                    .toUriString();

            ResponseEntity<List> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    List.class);
            List<?> body = response.getBody();
            return body != null && !body.isEmpty();
        } catch (Exception e) {
            System.err.println("Nominatim verification failed: " + e.getMessage());
            return true; // Fallback in caso di KO di rete
        }
    }

    public String fetchUnsplashImageUrl(String cityName) {
        return getCuratedImageUrl(cityName);
    }

    public String getCuratedImageUrl(String cityName) {
        String lower = cityName.toLowerCase();
        if (lower.contains("napoli"))
            return "https://images.unsplash.com/photo-1599682715474-361182378581?q=80&w=1470&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D";
        if (lower.contains("roma"))
            return "https://images.unsplash.com/photo-1552832230-c0197dd311b5?auto=format&fit=crop&w=800&q=80";
        if (lower.contains("parigi"))
            return "https://images.unsplash.com/photo-1502602898657-3e91760cbb34?auto=format&fit=crop&w=800&q=80";
        if (lower.contains("londra"))
            return "https://images.unsplash.com/photo-1486299267070-83823f5448dd?q=80&w=1471&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D";
        if (lower.contains("new york"))
            return "https://images.unsplash.com/photo-1496442226666-8d4d0e62e6e9?auto=format&fit=crop&w=800&q=80";
        if (lower.contains("tokyo"))
            return "https://images.unsplash.com/photo-1540959733332-eab4deceeaf7?auto=format&fit=crop&w=800&q=80";
        if (lower.contains("barcellona"))
            return "https://images.unsplash.com/photo-1583422409516-2895a77efedd?auto=format&fit=crop&w=800&q=80";
        if (lower.contains("venezia"))
            return "https://images.unsplash.com/photo-1527631746610-bca00a040d60?auto=format&fit=crop&w=800&q=80";
        if (lower.contains("firenze"))
            return "https://images.unsplash.com/photo-1478147427282-58a87a120781?auto=format&fit=crop&w=800&q=80";
        if (lower.contains("milano") || lower.contains("milan"))
            return "https://images.unsplash.com/photo-1520175480921-4edfa2983e0f?auto=format&fit=crop&w=800&q=80";
        if (lower.contains("sydney"))
            return "https://images.unsplash.com/photo-1506973035872-a4ec16b8e8d9?auto=format&fit=crop&w=800&q=80";
        if (lower.contains("rio de janeiro"))
            return "https://images.unsplash.com/photo-1483728642387-6c3bdd6c93e5?auto=format&fit=crop&w=800&q=80";
        if (lower.contains("cairo"))
            return "https://images.unsplash.com/photo-1572252017417-20815197f809?auto=format&fit=crop&w=800&q=80";
        if (lower.contains("atene"))
            return "https://images.unsplash.com/photo-1506477331477-33d5d8b3dc85?auto=format&fit=crop&w=800&q=80";
        if (lower.contains("amsterdam"))
            return "https://images.unsplash.com/photo-1512470876302-972faa2aa9a4?q=80&w=1470&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D";
        if (lower.contains("dubai"))
            return "https://images.unsplash.com/photo-1512453979798-5ea266f8880c?auto=format&fit=crop&w=800&q=80";
        if (lower.contains("istanbul"))
            return "https://images.unsplash.com/photo-1524231757912-21f4fe3a7200?auto=format&fit=crop&w=800&q=80";
        if (lower.contains("nairobi"))
            return "https://images.unsplash.com/photo-1516426122078-c23e76319801?auto=format&fit=crop&w=800&q=80";
        if (lower.contains("trentino"))
            return "https://images.unsplash.com/photo-1506744038136-46273834b3fb?auto=format&fit=crop&w=800&q=80";
        if (lower.contains("capo") || lower.contains("cape town"))
            return "https://images.unsplash.com/photo-1580060839134-75a5edca2e99?auto=format&fit=crop&w=800&q=80";
        if (lower.contains("praga"))
            return "https://images.unsplash.com/photo-1564511287568-54483b52a35e?q=80&w=1470&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D";
        if (lower.contains("san francisco"))
            return "https://images.unsplash.com/photo-1506012787146-f92b2d7d6d96?auto=format&fit=crop&w=800&q=80";
        if (lower.contains("tropea"))
            return "https://images.unsplash.com/photo-1590001155093-a3c66ab0c3ff?auto=format&fit=crop&w=800&q=80";
        if (lower.contains("palermo"))
            return "https://images.unsplash.com/photo-1541532713592-79a0317b6b77?auto=format&fit=crop&w=800&q=80";
        if (lower.contains("reggio"))
            return "https://images.unsplash.com/photo-1690289793717-f92cc222752c?auto=format&fit=crop&w=800&q=80";
        if (lower.contains("cosenza") || lower.contains("sila"))
            return "https://images.unsplash.com/photo-1441974231531-c6227db76b6e?auto=format&fit=crop&w=800&q=80";
        if (lower.contains("scilla"))
            return "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?auto=format&fit=crop&w=800&q=80";
        return null;
    }
}
