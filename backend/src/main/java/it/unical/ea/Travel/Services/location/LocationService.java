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
    public Page<LocationDto> searchLocation(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        String safeKeyword = (keyword == null) ? "" : keyword.trim();
        Page<Location> locationPage = locationRepository.searchByKeyword(safeKeyword, pageable);
        return locationPage.map(locationMapper::toDto);
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

        // 1. Cerca nel database (case-insensitive)
        Optional<Location> existing = locationRepository.findByNameIgnoreCase(trimmedName);
        if (existing.isPresent()) {
            Location loc = existing.get();
            if (loc.getImageUrl() == null || loc.getImageUrl().contains("wikimedia.org")
                    || loc.getImageUrl().contains("photo-1488646953014-85cb44e25828")
                    || loc.getImageUrl().contains("loremflickr.com")) {
                loc.setImageUrl(fetchUnsplashImageUrl(trimmedName));
                loc = locationRepository.save(loc);
            }
            return loc;
        }

        // 2. Verifica tramite l'API OpenStreetMap Nominatim
        boolean isValid = verifyLocationWithNominatim(trimmedName);
        if (!isValid) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "location.invalidName");
        }

        // 3. Crea e salva la nuova Location
        Location newLocation = new Location();
        newLocation.setName(trimmedName);
        newLocation.setImageUrl(fetchUnsplashImageUrl(trimmedName));
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
            return "https://images.unsplash.com/photo-1599839575945-a9e5af0c3fa5?auto=format&fit=crop&w=800&q=80";
        if (lower.contains("roma"))
            return "https://images.unsplash.com/photo-1552832230-c0197dd311b5?auto=format&fit=crop&w=800&q=80";
        if (lower.contains("parigi"))
            return "https://images.unsplash.com/photo-1502602898657-3e91760cbb34?auto=format&fit=crop&w=800&q=80";
        if (lower.contains("londra"))
            return "https://images.unsplash.com/photo-1513635269975-59663e0ca1ad?auto=format&fit=crop&w=800&q=80";
        if (lower.contains("new york"))
            return "https://images.unsplash.com/photo-1496442226666-8d4d0e62e6e9?auto=format&fit=crop&w=800&q=80";
        if (lower.contains("tokyo"))
            return "https://images.unsplash.com/photo-1540959733332-eab4deceeaf7?auto=format&fit=crop&w=800&q=80";
        if (lower.contains("barcellona"))
            return "https://images.unsplash.com/photo-1583422409516-2895a77efedd?auto=format&fit=crop&w=800&q=80";
        if (lower.contains("venezia"))
            return "https://images.unsplash.com/photo-1527631746610-bca00a040d60?auto=format&fit=crop&w=800&q=80";
        if (lower.contains("firenze"))
            return "https://images.unsplash.com/photo-1528114039593-4366cc08227d?auto=format&fit=crop&w=800&q=80";
        if (lower.contains("sydney"))
            return "https://images.unsplash.com/photo-1506973035872-a4ec16b8e8d9?auto=format&fit=crop&w=800&q=80";
        if (lower.contains("rio de janeiro"))
            return "https://images.unsplash.com/photo-1483728642387-6c3bdd6c93e5?auto=format&fit=crop&w=800&q=80";
        if (lower.contains("cairo"))
            return "https://images.unsplash.com/photo-1572252017417-20815197f809?auto=format&fit=crop&w=800&q=80";
        if (lower.contains("atene"))
            return "https://images.unsplash.com/photo-1506477331477-33d5d8b3dc85?auto=format&fit=crop&w=800&q=80";
        if (lower.contains("amsterdam"))
            return "https://images.unsplash.com/photo-1513694203232-719a280e022f?auto=format&fit=crop&w=800&q=80";
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
            return "https://images.unsplash.com/photo-154134307207b-2bc11763eeac?auto=format&fit=crop&w=800&q=80";
        if (lower.contains("san francisco"))
            return "https://images.unsplash.com/photo-1506012787146-f92b2d7d6d96?auto=format&fit=crop&w=800&q=80";
        return null;
    }
}
