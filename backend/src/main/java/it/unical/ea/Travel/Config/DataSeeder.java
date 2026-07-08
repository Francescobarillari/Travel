package it.unical.ea.Travel.Config;

import it.unical.ea.Travel.Entities.activity.Activity;
import it.unical.ea.Travel.Entities.activity.ActivityTemplate;
import it.unical.ea.Travel.Entities.itinerary.Itinerary;
import it.unical.ea.Travel.Entities.location.Location;
import it.unical.ea.Travel.Entities.user.User;
import it.unical.ea.Travel.Repositories.activity.ActivityRepository;
import it.unical.ea.Travel.Repositories.activity.ActivityTemplateRepository;
import it.unical.ea.Travel.Repositories.itinerary.ItineraryRepository;
import it.unical.ea.Travel.Repositories.location.LocationRepository;
import it.unical.ea.Travel.Repositories.user.UserRepository;
import it.unical.ea.Travel.Services.keycloak.KeycloakAdminService;
import it.unical.ea.Travel.Services.keycloak.KeycloakUserAlreadyExistsException;
import it.unical.ea.dtos.authDto.SignupRequest;
import it.unical.ea.Travel.Entities.review.Review;
import it.unical.ea.Travel.Repositories.review.ReviewRepository;
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
    private final ActivityTemplateRepository activityTemplateRepository;
    private final ActivityRepository activityRepository;
    private final ItineraryRepository itineraryRepository;
    private final LocationRepository locationRepository;
    private final KeycloakAdminService keycloakAdminService;
    private final ReviewRepository reviewRepository;

    public DataSeeder(UserRepository userRepository, ActivityTemplateRepository activityTemplateRepository, ActivityRepository activityRepository,
                      ItineraryRepository itineraryRepository, LocationRepository locationRepository,
                      KeycloakAdminService keycloakAdminService, ReviewRepository reviewRepository) {
        this.userRepository = userRepository;
        this.activityTemplateRepository = activityTemplateRepository;
        this.activityRepository = activityRepository;
        this.itineraryRepository = itineraryRepository;
        this.locationRepository = locationRepository;
        this.keycloakAdminService = keycloakAdminService;
        this.reviewRepository = reviewRepository;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        seedTestUser();
        if (activityRepository.count() == 0) {
            seedData();
        }
        if (reviewRepository.count() == 0) {
            seedReviews();
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
                keycloakAdminService.verifyEmail(keycloakId);

                User user = new User();
                user.setEmail(TEST_EMAIL);
                user.setPasswordHash("seeded");
                user.setUserType(UserType.VIAGGIATORE);
                user.setFirstName("Test");
                user.setLastName("User");
                user.setRoles("ROLE_VIAGGIATORE");
                user.setKeycloakId(keycloakId);
                userRepository.save(user);
                log.info("✅ Utente test creato e verificato: {} / {}", TEST_EMAIL, TEST_PASSWORD);
            } catch (KeycloakUserAlreadyExistsException e) {
                log.info("ℹ️ Utente test già presente su Keycloak: {}", TEST_EMAIL);
                try {
                    userRepository.getUserByEmail(TEST_EMAIL).ifPresent(u -> {
                        keycloakAdminService.verifyEmail(u.getKeycloakId());
                    });
                } catch (Exception ex) {
                    log.warn("Impossibile verificare utente esistente: {}", ex.getMessage());
                }
            } catch (Exception e) {
                log.warn("⚠️ Impossibile creare utente test (Keycloak non raggiungibile?): {}", e.getMessage());
            }
        } else {
            try {
                userRepository.getUserByEmail(TEST_EMAIL).ifPresent(u -> {
                    keycloakAdminService.verifyEmail(u.getKeycloakId());
                });
            } catch (Exception e) {
                log.warn("Impossibile verificare utente esistente: {}", e.getMessage());
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

        // Create the super short account
        User shortUser = new User();
        shortUser.setEmail("a@a.com");
        shortUser.setPasswordHash("hashed_password");
        shortUser.setUserType(UserType.VIAGGIATORE);
        shortUser.setFirstName("A");
        shortUser.setLastName("A");
        shortUser.setRoles("ROLE_VIAGGIATORE");
        shortUser.setKeycloakId(UUID.randomUUID().toString());
        userRepository.save(shortUser);

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

        // --- Templates and Activities ---
        ActivityTemplate tpl1 = new ActivityTemplate();
        tpl1.setName("Visita al Colosseo");
        tpl1.setDescription("Tour guidato al Colosseo e Fori Imperiali.");
        tpl1.setLocation("Roma");
        tpl1.setLocationEntity(loc1);
        tpl1.setOrganizer(organizer);
        tpl1.setImages(List.of("https://images.unsplash.com/photo-1552832230-c0197dd311b5?ixlib=rb-1.2.1&auto=format&fit=crop&w=800&q=80"));
        tpl1.setTags(new HashSet<>(Arrays.asList(TravelTag.CULTURA, TravelTag.STORIA)));
        tpl1 = activityTemplateRepository.save(tpl1);

        Activity act1 = new Activity();
        act1.setTemplate(tpl1);
        act1.setStartTime(LocalDateTime.now().plusDays(10).plusHours(9));
        act1.setEndTime(LocalDateTime.now().plusDays(10).plusHours(12));
        act1.setParticipants(20);
        act1.setPrice(new BigDecimal("35.00"));

        ActivityTemplate tpl2 = new ActivityTemplate();
        tpl2.setName("Cena a Trastevere");
        tpl2.setDescription("Cena tipica romana in una trattoria a Trastevere.");
        tpl2.setLocation("Roma");
        tpl2.setLocationEntity(loc1);
        tpl2.setOrganizer(organizer);
        tpl2.setTags(new HashSet<>(Arrays.asList(TravelTag.CULTURA, TravelTag.CIBO)));
        tpl2 = activityTemplateRepository.save(tpl2);

        Activity act2 = new Activity();
        act2.setTemplate(tpl2);
        act2.setStartTime(LocalDateTime.now().plusDays(10).plusHours(20));
        act2.setEndTime(LocalDateTime.now().plusDays(10).plusHours(23));
        act2.setParticipants(15);
        act2.setPrice(new BigDecimal("45.00"));

        ActivityTemplate tpl3 = new ActivityTemplate();
        tpl3.setName("Trekking Tre Cime");
        tpl3.setDescription("Escursione panoramica alle Tre Cime di Lavaredo.");
        tpl3.setLocation("Dolomiti");
        tpl3.setLocationEntity(loc2);
        tpl3.setOrganizer(organizer);
        tpl3.setImages(List.of("https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?ixlib=rb-1.2.1&auto=format&fit=crop&w=800&q=80"));
        tpl3.setTags(new HashSet<>(Arrays.asList(TravelTag.AVVENTURA, TravelTag.MONTAGNA, TravelTag.NATURA, TravelTag.TREKKING)));
        tpl3 = activityTemplateRepository.save(tpl3);

        Activity act3 = new Activity();
        act3.setTemplate(tpl3);
        act3.setStartTime(LocalDateTime.now().plusDays(21).plusHours(8));
        act3.setEndTime(LocalDateTime.now().plusDays(21).plusHours(16));
        act3.setParticipants(10);
        act3.setPrice(new BigDecimal("25.00"));

        ActivityTemplate tpl4 = new ActivityTemplate();
        tpl4.setName("Spa in Montagna");
        tpl4.setDescription("Giornata di relax alle terme alpine.");
        tpl4.setLocation("Dolomiti");
        tpl4.setLocationEntity(loc2);
        tpl4.setOrganizer(organizer);
        tpl4.setTags(new HashSet<>(Arrays.asList(TravelTag.RELAX, TravelTag.MONTAGNA)));
        tpl4 = activityTemplateRepository.save(tpl4);

        Activity act4 = new Activity();
        act4.setTemplate(tpl4);
        act4.setStartTime(LocalDateTime.now().plusDays(22).plusHours(10));
        act4.setEndTime(LocalDateTime.now().plusDays(22).plusHours(18));
        act4.setParticipants(10);
        act4.setPrice(new BigDecimal("60.00"));

        ActivityTemplate tplSafari = new ActivityTemplate();
        tplSafari.setName("Game Drive");
        tplSafari.setDescription("Safari in Jeep per vedere i Big Five.");
        tplSafari.setLocation("Masai Mara");
        tplSafari.setLocationEntity(loc3);
        tplSafari.setOrganizer(organizer);
        tplSafari.setTags(new HashSet<>(Arrays.asList(TravelTag.AVVENTURA, TravelTag.SAFARI, TravelTag.NATURA, TravelTag.ANIMALI)));
        tplSafari = activityTemplateRepository.save(tplSafari);

        Activity actSafari = new Activity();
        actSafari.setTemplate(tplSafari);
        actSafari.setStartTime(LocalDateTime.now().plusDays(41).plusHours(6));
        actSafari.setEndTime(LocalDateTime.now().plusDays(41).plusHours(12));
        actSafari.setParticipants(6);
        actSafari.setPrice(new BigDecimal("150.00"));

        ActivityTemplate tpl5 = new ActivityTemplate();
        tpl5.setName("Corso di Cucina Italiana");
        tpl5.setDescription("Impara a fare pasta e pizza con uno chef locale.");
        tpl5.setLocation("Firenze");
        tpl5.setLocationEntity(null);
        tpl5.setOrganizer(organizer);
        tpl5.setImages(List.of("https://images.unsplash.com/photo-1556910103-1c02745aae4d?ixlib=rb-1.2.1&auto=format&fit=crop&w=800&q=80"));
        tpl5.setTags(new HashSet<>(Arrays.asList(TravelTag.CIBO, TravelTag.CULTURA)));
        tpl5 = activityTemplateRepository.save(tpl5);

        Activity act5 = new Activity();
        act5.setTemplate(tpl5);
        act5.setStartTime(LocalDateTime.now().plusDays(5).plusHours(16));
        act5.setEndTime(LocalDateTime.now().plusDays(5).plusHours(20));
        act5.setParticipants(8);
        act5.setPrice(new BigDecimal("80.00"));

        ActivityTemplate tpl6 = new ActivityTemplate();
        tpl6.setName("Giro in Gondola");
        tpl6.setDescription("Romantico giro nei canali di Venezia.");
        tpl6.setLocation("Venezia");
        tpl6.setLocationEntity(null);
        tpl6.setOrganizer(organizer);
        tpl6.setTags(new HashSet<>(Arrays.asList(TravelTag.ROMANTICISMO, TravelTag.CULTURA)));
        tpl6 = activityTemplateRepository.save(tpl6);

        Activity act6 = new Activity();
        act6.setTemplate(tpl6);
        act6.setStartTime(LocalDateTime.now().plusDays(2).plusHours(18));
        act6.setEndTime(LocalDateTime.now().plusDays(2).plusHours(19));
        act6.setParticipants(2);
        act6.setPrice(new BigDecimal("90.00"));

        ActivityTemplate tpl7 = new ActivityTemplate();
        tpl7.setName("Free Walking Tour");
        tpl7.setDescription("Tour a piedi gratuito per il centro storico (mancia consigliata).");
        tpl7.setLocation("Milano");
        tpl7.setLocationEntity(null);
        tpl7.setOrganizer(organizer);
        tpl7.setTags(new HashSet<>(Arrays.asList(TravelTag.CULTURA, TravelTag.CITTA)));
        tpl7 = activityTemplateRepository.save(tpl7);

        Activity act7 = new Activity();
        act7.setTemplate(tpl7);
        act7.setStartTime(LocalDateTime.now().plusDays(1).plusHours(10));
        act7.setEndTime(LocalDateTime.now().plusDays(1).plusHours(12));
        act7.setParticipants(30);
        act7.setPrice(BigDecimal.ZERO);

        // Save all activities
        activityRepository.saveAll(Arrays.asList(act1, act2, act3, act4, actSafari, act5, act6, act7));

        // Link activities to itineraries
        itinerary1.setActivities(Arrays.asList(act1, act2));
        itinerary2.setActivities(Arrays.asList(act3, act4));
        itinerary3.setActivities(Arrays.asList(actSafari));

        // Save all itineraries
        itineraryRepository.saveAll(Arrays.asList(itinerary1, itinerary2, itinerary3));
    }

    private void seedReviews() {
        // Find users
        List<User> travelers = userRepository.findByUserType(UserType.VIAGGIATORE);
        if (travelers.isEmpty()) return;
        User reviewer = travelers.get(0);
        
        List<ActivityTemplate> templates = activityTemplateRepository.findAll();
        if (!templates.isEmpty()) {
            ActivityTemplate tpl1 = templates.get(0);
            Review rev1 = new Review();
            rev1.setAuthor(reviewer);
            rev1.setActivityTemplate(tpl1);
            rev1.setRating(5.0);
            rev1.setComment("Attività fantastica, guida super preparata!");
            reviewRepository.save(rev1);

            if (templates.size() > 1) {
                ActivityTemplate tpl2 = templates.get(1);
                Review rev2 = new Review();
                rev2.setAuthor(reviewer);
                rev2.setActivityTemplate(tpl2);
                rev2.setRating(4.0);
                rev2.setComment("Molto bello, ma faceva un po' freddo.");
                reviewRepository.save(rev2);
            }
        }

        List<Itinerary> itineraries = itineraryRepository.findAll();
        if (!itineraries.isEmpty()) {
            Itinerary iti1 = itineraries.get(0);
            Review revIti = new Review();
            revIti.setAuthor(reviewer);
            revIti.setItinerary(iti1);
            revIti.setRating(5.0);
            revIti.setComment("L'itinerario nel complesso è stato indimenticabile. Consigliatissimo!");
            reviewRepository.save(revIti);
        }
    }
}
