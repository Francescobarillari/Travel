package it.unical.ea.Travel.Config;

import it.unical.ea.Travel.Repositories.location.LocationRepository;
import it.unical.ea.Travel.Services.location.LocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Component
@Profile("!test")
@Order(1)
@RequiredArgsConstructor
public class LocationDataSeeder implements CommandLineRunner {

    private final LocationRepository locationRepository;
    private final LocationService locationService;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (locationRepository.count() == 0) {
            seedLocations();
        } else {
            healExistingLocations();
        }
    }

    private void healExistingLocations() {
        locationRepository.findAll().forEach(loc -> {
            String curated = locationService.getCuratedImageUrl(loc.getName());
            if (curated != null) {
                loc.setImageUrl(curated);
                locationRepository.save(loc);
            } else if (loc.getImageUrl() == null || loc.getImageUrl().contains("wikimedia.org") || loc.getImageUrl().contains("photo-1488646953014-85cb44e25828") || loc.getImageUrl().contains("loremflickr.com")) {
                loc.setImageUrl(null);
                locationRepository.save(loc);
            }
        });
    }

    private void seedLocations() {
        List<String> cities = Arrays.asList(
            "Roma, Italia", "Parigi, Francia", "Londra, Regno Unito", "New York, USA", "Tokyo, Giappone",
            "Barcellona, Spagna", "Venezia, Italia", "Firenze, Italia", "Sydney, Australia", "Rio de Janeiro, Brasile",
            "Cairo, Egitto", "Atene, Grecia", "Amsterdam, Paesi Bassi", "Dubai, Emirati Arabi Uniti", "Istanbul, Turchia",
            "Nairobi, Kenya", "Trentino-Alto Adige", "Città del Capo, Sudafrica", "Praga, Repubblica Ceca", "San Francisco, USA"
        );
        for (String city : cities) {
            locationService.getOrCreateLocation(city);
        }
    }
}
