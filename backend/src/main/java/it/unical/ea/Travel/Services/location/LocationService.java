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

    @Value("${unsplash.access-key:}")
    private String unsplashAccessKey;

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
            return existing.get();
        }

        // 2. Verifica tramite l'API OpenStreetMap Nominatim
        boolean isValid = verifyLocationWithNominatim(trimmedName);
        if (!isValid) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "location.invalidName");
        }

        // 3. Crea e salva la nuova Location popolando i dati da Unsplash / Wikipedia
        Location newLocation = new Location();
        newLocation.setName(trimmedName);
        
        // Cerca prima l'immagine su Unsplash
        String imageUrl = fetchUnsplashImageUrl(trimmedName);
        if (imageUrl != null && !imageUrl.isBlank()) {
            newLocation.setImageUrl(imageUrl);
        }
        
        // Recupera la descrizione e l'immagine di fallback da Wikipedia
        WikipediaResponse wikiData = fetchWikipediaData(trimmedName);
        if (wikiData != null) {
            if (wikiData.getExtract() != null && !wikiData.getExtract().isBlank()) {
                String desc = wikiData.getExtract();
                if (desc.length() > 990) {
                    desc = desc.substring(0, 987) + "...";
                }
                newLocation.setDescription(desc);
            }
            // Se Unsplash non ha restituito immagini, usa Wikipedia come fallback
            if (newLocation.getImageUrl() == null || newLocation.getImageUrl().isBlank()) {
                if (wikiData.getOriginalimage() != null && wikiData.getOriginalimage().getSource() != null) {
                    newLocation.setImageUrl(wikiData.getOriginalimage().getSource());
                } else if (wikiData.getThumbnail() != null && wikiData.getThumbnail().getSource() != null) {
                    newLocation.setImageUrl(wikiData.getThumbnail().getSource());
                }
            }
        }

        if (newLocation.getDescription() == null || newLocation.getDescription().isBlank()) {
            newLocation.setDescription("Località verificata tramite OpenStreetMap.");
        }
        if (newLocation.getImageUrl() == null || newLocation.getImageUrl().isBlank()) {
            newLocation.setImageUrl("https://images.unsplash.com/photo-1488646953014-85cb44e25828?ixlib=rb-1.2.1&auto=format&fit=crop&w=800&q=80");
        }
        
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
                    List.class
            );
            List<?> body = response.getBody();
            return body != null && !body.isEmpty();
        } catch (Exception e) {
            System.err.println("Nominatim verification failed: " + e.getMessage());
            return true; // Fallback in caso di KO di rete
        }
    }

    private WikipediaResponse fetchWikipediaData(String cityName) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            String cleanName = cityName.split(",")[0].trim();
            String encodedName = java.net.URLEncoder.encode(cleanName, java.nio.charset.StandardCharsets.UTF_8.toString());
            String url = "https://it.wikipedia.org/api/rest_v1/page/summary/" + encodedName;
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "TravelApp/1.0 (contact: admin@travelapp.com)");
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<WikipediaResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    WikipediaResponse.class
            );
            return response.getBody();
        } catch (Exception e) {
            System.err.println("Wikipedia data fetch failed for " + cityName + ": " + e.getMessage());
            return null;
        }
    }

    private String fetchUnsplashImageUrl(String cityName) {
        if (unsplashAccessKey == null || unsplashAccessKey.trim().isEmpty()) {
            return null;
        }
        try {
            RestTemplate restTemplate = new RestTemplate();
            String cleanName = cityName.split(",")[0].trim();
            String url = UriComponentsBuilder.fromUriString("https://api.unsplash.com/search/photos")
                    .queryParam("query", cleanName)
                    .queryParam("per_page", 1)
                    .build()
                    .toUriString();
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Client-ID " + unsplashAccessKey.trim());
            headers.set("User-Agent", "TravelApp/1.0 (contact: admin@travelapp.com)");
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<UnsplashResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    UnsplashResponse.class
            );
            UnsplashResponse body = response.getBody();
            if (body != null && body.getResults() != null && !body.getResults().isEmpty()) {
                UnsplashResponse.UnsplashPhoto photo = body.getResults().get(0);
                if (photo.getUrls() != null) {
                    if (photo.getUrls().getRegular() != null) {
                        return photo.getUrls().getRegular();
                    } else if (photo.getUrls().getSmall() != null) {
                        return photo.getUrls().getSmall();
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Unsplash image fetch failed for " + cityName + ": " + e.getMessage());
        }
        return null;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class WikipediaResponse {
        private String extract;
        private ImageInfo thumbnail;
        private ImageInfo originalimage;

        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class ImageInfo {
            private String source;
        }
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class UnsplashResponse {
        private List<UnsplashPhoto> results;

        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class UnsplashPhoto {
            private Urls urls;

            @Data
            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class Urls {
                private String raw;
                private String full;
                private String regular;
                private String small;
                private String thumb;
            }
        }
    }
}
