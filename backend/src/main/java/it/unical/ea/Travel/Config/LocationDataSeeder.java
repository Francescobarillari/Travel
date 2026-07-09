package it.unical.ea.Travel.Config;

import it.unical.ea.Travel.Entities.location.Location;
import it.unical.ea.Travel.Repositories.location.LocationRepository;
import it.unical.ea.Travel.Services.location.LocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

@Component
@Profile("!test")
@Order(1)
@RequiredArgsConstructor
public class LocationDataSeeder implements CommandLineRunner {

    private final LocationRepository locationRepository;
    private final LocationService locationService;

    @Value("${app.upload.dir:./uploads}")
    private String uploadDir;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        seedLocations();
        healExistingLocations();
    }

    private void healExistingLocations() {
        locationRepository.findAll().forEach(loc -> {
            if (loc.getImageUrl() != null && loc.getImageUrl().startsWith("/activity/images/")) {
                return; // Non sovrascrivere immagini locali caricate custom
            }
            String curated = locationService.getCuratedImageUrl(loc.getName());
            if (curated != null) {
                if (!curated.equals(loc.getImageUrl())) {
                    loc.setImageUrl(curated);
                    locationRepository.save(loc);
                }
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
            "Cairo, Egitto", "Atene, Grecia", "Amsterdam, Paesi Basi", "Dubai, Emirati Arabi Uniti", "Istanbul, Turchia",
            "Nairobi, Kenya", "Trentino-Alto Adige, Italia", "Città del Capo, Sudafrica", "Praga, Repubblica Ceca", "San Francisco, USA",
            "Berlino, Germania", "Madrid, Spagna", "Lisbona, Portogallo", "Vienna, Austria", "Stoccolma, Svezia",
            "Kyoto, Giappone", "Bangkok, Thailandia", "Buenos Aires, Argentina", "Toronto, Canada", "Chicago, USA"
        );
        for (String city : cities) {
            Location loc = locationService.getOrCreateLocation(city);
            if (loc != null) {
                copyLocalImageIfExists(city, loc);
            }
        }
    }

    private void copyLocalImageIfExists(String cityName, Location location) {
        String cleanName = cityName.split(",")[0].trim();
        List<String> extensions = Arrays.asList(".jpg", ".jpeg", ".png");
        
        List<String> parentDirs = Arrays.asList(
            "/Users/francescobarillari/Desktop/Travel/img",
            "../img",
            "./img",
            "img"
        );
        
        for (String parentDir : parentDirs) {
            Path dirPath = Paths.get(parentDir);
            if (Files.exists(dirPath) && Files.isDirectory(dirPath)) {
                for (String ext : extensions) {
                    Path sourceFile = dirPath.resolve(cleanName + ext);
                    if (Files.exists(sourceFile) && Files.isRegularFile(sourceFile)) {
                        try {
                            Path destDir = Paths.get(uploadDir).resolve("activities").toAbsolutePath().normalize();
                            Files.createDirectories(destDir);
                            
                            String destFileName = "location_" + cleanName.toLowerCase().replace(" ", "_") + ext;
                            Path destFile = destDir.resolve(destFileName);
                            
                            Files.copy(sourceFile, destFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                            
                            location.setImageUrl("/activity/images/" + destFileName);
                            locationRepository.save(location);
                            return;
                        } catch (Exception e) {
                            System.err.println("Errore durante la copia dell'immagine per " + cleanName + ": " + e.getMessage());
                        }
                    }
                }
            }
        }
    }
}
