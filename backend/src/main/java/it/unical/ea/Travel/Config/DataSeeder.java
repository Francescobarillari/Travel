package it.unical.ea.Travel.Config;

import it.unical.ea.Travel.Entities.activity.Activity;
import it.unical.ea.Travel.Entities.itinerary.Itinerary;
import it.unical.ea.Travel.Entities.location.Location;
import it.unical.ea.Travel.Entities.user.User;
import it.unical.ea.Travel.Repositories.activity.ActivityRepository;
import it.unical.ea.Travel.Repositories.itinerary.ItineraryRepository;
import it.unical.ea.Travel.Repositories.location.LocationRepository;
import it.unical.ea.Travel.Repositories.user.UserRepository;
import it.unical.ea.Travel.Services.keycloak.KeycloakAdminService;
import it.unical.ea.Travel.Services.keycloak.KeycloakUserAlreadyExistsException;
import it.unical.ea.dtos.authDto.SignupRequest;
import it.unical.ea.enums.UserType;
import it.unical.ea.enums.TravelTag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.UUID;

@Slf4j
@Component
@Profile("!test")
@Order(2)
public class DataSeeder implements CommandLineRunner {

    private static final String TEST_EMAIL = "a@a.it";
    private static final String TEST_PASSWORD = "aaa";

    private final UserRepository userRepository;
    private final ActivityRepository activityRepository;
    private final ItineraryRepository itineraryRepository;
    private final LocationRepository locationRepository;
    private final KeycloakAdminService keycloakAdminService;

    public DataSeeder(UserRepository userRepository, ActivityRepository activityRepository,
                      ItineraryRepository itineraryRepository, LocationRepository locationRepository,
                      KeycloakAdminService keycloakAdminService) {
        this.userRepository = userRepository;
        this.activityRepository = activityRepository;
        this.itineraryRepository = itineraryRepository;
        this.locationRepository = locationRepository;
        this.keycloakAdminService = keycloakAdminService;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        seedTestUser();
        if (activityRepository.count() == 0) {
            seedData();
        }
    }

    private void seedTestUser() {
        boolean existsInDb = userRepository.getUserByEmail(TEST_EMAIL).isPresent();
        if (!existsInDb) {
            try {
                SignupRequest req = new SignupRequest();
                req.setEmail(TEST_EMAIL);
                req.setPassword(TEST_PASSWORD);
                req.setUserType(UserType.VIAGGIATORE);
                req.setFirstName("Test");
                req.setLastName("User");
                String keycloakId = keycloakAdminService.createUser(req);

                User user = new User();
                user.setEmail(TEST_EMAIL);
                user.setPasswordHash("seeded");
                user.setUserType(UserType.VIAGGIATORE);
                user.setFirstName("Test");
                user.setLastName("User");
                user.setRoles("ROLE_VIAGGIATORE");
                user.setKeycloakId(keycloakId);
                userRepository.save(user);
                log.info("✅ Utente test creato: {} / {}", TEST_EMAIL, TEST_PASSWORD);
            } catch (KeycloakUserAlreadyExistsException e) {
                log.info("ℹ️ Utente test già presente su Keycloak: {}", TEST_EMAIL);
            } catch (Exception e) {
                log.warn("⚠️ Impossibile creare utente test (Keycloak non raggiungibile?): {}", e.getMessage());
            }
        }
    }

    private void seedData() {
        // Create an organizer user (SOCIETA)
        User organizer = new User();
        organizer.setEmail("organizer_" + UUID.randomUUID().toString().substring(0,8) + "@example.com");
        organizer.setPasswordHash("hashed_password");
        organizer.setUserType(UserType.SOCIETA);
        organizer.setCompanyName("Travel Adventures SRL");
        organizer.setVatNumber("IT12345678901");
        organizer.setRoles("ROLE_ORGANIZER");
        organizer.setKeycloakId(UUID.randomUUID().toString());
        organizer = userRepository.save(organizer);

        // Create a traveler user with some preferences
        User traveler = new User();
        traveler.setEmail("traveler@example.com");
        traveler.setPasswordHash("hashed_password");
        traveler.setUserType(UserType.VIAGGIATORE);
        traveler.setFirstName("Marco");
        traveler.setLastName("Rossi");
        traveler.setRoles("ROLE_VIAGGIATORE");
        traveler.setKeycloakId(UUID.randomUUID().toString());
        traveler.setPreferences(new HashSet<>(Arrays.asList(TravelTag.AVVENTURA, TravelTag.NATURA)));
        userRepository.save(traveler);

        // --- Località 1: Roma ---
        Location loc1 = locationRepository.findByNameIgnoreCase("Roma, Italia")
                .orElseGet(() -> {
                    Location loc = new Location();
                    loc.setName("Roma, Italia");
                    loc.setDescription("La capitale italiana, famosa per la sua storia, cultura e il Colosseo.");
                    loc.setImageUrl("https://img.icons8.com/color/500/colosseum.png");
                    return locationRepository.save(loc);
                });

        // --- Località 2: Alpi ---
        Location loc2 = locationRepository.findByNameIgnoreCase("Trentino-Alto Adige")
                .orElseGet(() -> {
                    Location loc = new Location();
                    loc.setName("Trentino-Alto Adige");
                    loc.setDescription("Trekking, natura e relax in montagna. Per ricaricare le batterie lontano dalla città.");
                    loc.setImageUrl("https://img.icons8.com/color/500/mountain.png");
                    return locationRepository.save(loc);
                });

        // --- Località 3: Kenya ---
        Location loc3 = locationRepository.findByNameIgnoreCase("Nairobi, Kenya")
                .orElseGet(() -> {
                    Location loc = new Location();
                    loc.setName("Nairobi, Kenya");
                    loc.setDescription("Un'avventura indimenticabile nella savana africana.");
                    loc.setImageUrl("https://img.icons8.com/color/500/giraffe.png");
                    return locationRepository.save(loc);
                });

        // --- Itineraries ---
        Itinerary itinerary1 = new Itinerary();
        itinerary1.setTitle("Itinerario Romano");
        itinerary1.setDescription("Colosseo, Vaticano e Trastevere");
        itinerary1.setStartDateTime(LocalDateTime.now().plusDays(10));
        itinerary1.setEndDateTime(LocalDateTime.now().plusDays(13));
        itinerary1.setCreator(organizer);
        itinerary1.setImagePath("https://images.unsplash.com/photo-1552832230-c0197dd311b5?ixlib=rb-1.2.1&auto=format&fit=crop&w=800&q=80");

        Itinerary itinerary2 = new Itinerary();
        itinerary2.setTitle("Itinerario Alpino");
        itinerary2.setDescription("Escursioni giornaliere sulle Dolomiti");
        itinerary2.setStartDateTime(LocalDateTime.now().plusDays(20));
        itinerary2.setEndDateTime(LocalDateTime.now().plusDays(25));
        itinerary2.setCreator(organizer);
        itinerary2.setImagePath("https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?ixlib=rb-1.2.1&auto=format&fit=crop&w=800&q=80");

        Itinerary itinerary3 = new Itinerary();
        itinerary3.setTitle("Avventura Safari");
        itinerary3.setDescription("Safari nel Masai Mara");
        itinerary3.setStartDateTime(LocalDateTime.now().plusDays(40));
        itinerary3.setEndDateTime(LocalDateTime.now().plusDays(47));
        itinerary3.setCreator(organizer);

        // --- Activities ---
        Activity act1 = new Activity();
        act1.setName("Visita al Colosseo");
        act1.setDescription("Tour guidato al Colosseo e Fori Imperiali.");
        act1.setLocation("Roma");
        act1.setStartTime(LocalDateTime.now().plusDays(10).plusHours(9));
        act1.setEndTime(LocalDateTime.now().plusDays(10).plusHours(12));
        act1.setParticipants(20);
        act1.setPrice(new BigDecimal("35.00"));
        act1.setLocationEntity(loc1);
        act1.setOrganizer(organizer);
        act1.setImages(List.of("https://images.unsplash.com/photo-1552832230-c0197dd311b5?ixlib=rb-1.2.1&auto=format&fit=crop&w=800&q=80"));
        act1.setTags(new HashSet<>(Arrays.asList(TravelTag.CULTURA, TravelTag.STORIA)));

        Activity act2 = new Activity();
        act2.setName("Cena a Trastevere");
        act2.setDescription("Cena tipica romana in una trattoria a Trastevere.");
        act2.setLocation("Roma");
        act2.setStartTime(LocalDateTime.now().plusDays(10).plusHours(20));
        act2.setEndTime(LocalDateTime.now().plusDays(10).plusHours(23));
        act2.setParticipants(15);
        act2.setPrice(new BigDecimal("45.00"));
        act2.setLocationEntity(loc1);
        act2.setOrganizer(organizer);
        act2.setTags(new HashSet<>(Arrays.asList(TravelTag.CULTURA, TravelTag.CIBO)));

        Activity act3 = new Activity();
        act3.setName("Trekking Tre Cime");
        act3.setDescription("Escursione panoramica alle Tre Cime di Lavaredo.");
        act3.setLocation("Dolomiti");
        act3.setStartTime(LocalDateTime.now().plusDays(21).plusHours(8));
        act3.setEndTime(LocalDateTime.now().plusDays(21).plusHours(16));
        act3.setParticipants(10);
        act3.setPrice(new BigDecimal("25.00"));
        act3.setLocationEntity(loc2);
        act3.setOrganizer(organizer);
        act3.setImages(List.of("https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?ixlib=rb-1.2.1&auto=format&fit=crop&w=800&q=80"));
        act3.setTags(new HashSet<>(Arrays.asList(TravelTag.AVVENTURA, TravelTag.MONTAGNA, TravelTag.NATURA, TravelTag.TREKKING)));

        Activity act4 = new Activity();
        act4.setName("Spa in Montagna");
        act4.setDescription("Giornata di relax alle terme alpine.");
        act4.setLocation("Dolomiti");
        act4.setStartTime(LocalDateTime.now().plusDays(22).plusHours(10));
        act4.setEndTime(LocalDateTime.now().plusDays(22).plusHours(18));
        act4.setParticipants(10);
        act4.setPrice(new BigDecimal("60.00"));
        act4.setLocationEntity(loc2);
        act4.setOrganizer(organizer);
        act4.setTags(new HashSet<>(Arrays.asList(TravelTag.RELAX, TravelTag.MONTAGNA)));

        Activity actSafari = new Activity();
        actSafari.setName("Game Drive");
        actSafari.setDescription("Safari in Jeep per vedere i Big Five.");
        actSafari.setLocation("Masai Mara");
        actSafari.setStartTime(LocalDateTime.now().plusDays(41).plusHours(6));
        actSafari.setEndTime(LocalDateTime.now().plusDays(41).plusHours(12));
        actSafari.setParticipants(6);
        actSafari.setPrice(new BigDecimal("150.00"));
        actSafari.setLocationEntity(loc3);
        actSafari.setOrganizer(organizer);
        actSafari.setTags(new HashSet<>(Arrays.asList(TravelTag.AVVENTURA, TravelTag.SAFARI, TravelTag.NATURA, TravelTag.ANIMALI)));

        // --- Additional independent activities ---
        Activity act5 = new Activity();
        act5.setName("Corso di Cucina Italiana");
        act5.setDescription("Impara a fare pasta e pizza con uno chef locale.");
        act5.setLocation("Firenze");
        act5.setStartTime(LocalDateTime.now().plusDays(5).plusHours(16));
        act5.setEndTime(LocalDateTime.now().plusDays(5).plusHours(20));
        act5.setParticipants(8);
        act5.setPrice(new BigDecimal("80.00"));
        act5.setLocationEntity(null);
        act5.setOrganizer(organizer);
        act5.setImages(List.of("https://images.unsplash.com/photo-1556910103-1c02745aae4d?ixlib=rb-1.2.1&auto=format&fit=crop&w=800&q=80"));
        act5.setTags(new HashSet<>(Arrays.asList(TravelTag.CIBO, TravelTag.CULTURA)));

        Activity act6 = new Activity();
        act6.setName("Giro in Gondola");
        act6.setDescription("Romantico giro nei canali di Venezia.");
        act6.setLocation("Venezia");
        act6.setStartTime(LocalDateTime.now().plusDays(2).plusHours(18));
        act6.setEndTime(LocalDateTime.now().plusDays(2).plusHours(19));
        act6.setParticipants(2);
        act6.setPrice(new BigDecimal("90.00"));
        act6.setLocationEntity(null);
        act6.setOrganizer(organizer);
        act6.setTags(new HashSet<>(Arrays.asList(TravelTag.ROMANTICISMO, TravelTag.CULTURA)));

        Activity act7 = new Activity();
        act7.setName("Free Walking Tour");
        act7.setDescription("Tour a piedi gratuito per il centro storico (mancia consigliata).");
        act7.setLocation("Milano");
        act7.setStartTime(LocalDateTime.now().plusDays(1).plusHours(10));
        act7.setEndTime(LocalDateTime.now().plusDays(1).plusHours(12));
        act7.setParticipants(30);
        act7.setPrice(BigDecimal.ZERO);
        act7.setLocationEntity(null);
        act7.setOrganizer(organizer);
        act7.setTags(new HashSet<>(Arrays.asList(TravelTag.CULTURA, TravelTag.CITTA)));

        // Save all activities
        activityRepository.saveAll(Arrays.asList(act1, act2, act3, act4, actSafari, act5, act6, act7));

        // Link activities to itineraries
        itinerary1.setActivities(Arrays.asList(act1, act2));
        itinerary2.setActivities(Arrays.asList(act3, act4));
        itinerary3.setActivities(Arrays.asList(actSafari));

        // Save all itineraries
        itineraryRepository.saveAll(Arrays.asList(itinerary1, itinerary2, itinerary3));
    }
}
