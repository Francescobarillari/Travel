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
        } else {
            healExistingActivityTemplates();
        }
        if (reviewRepository.count() == 0) {
            seedReviews();
        }
    }

    private void healExistingActivityTemplates() {
        activityTemplateRepository.findAll().forEach(tpl -> {
            String name = tpl.getName().toLowerCase();
            if (name.contains("colosseo")) {
                tpl.setImages(new java.util.ArrayList<>(List.of("https://images.unsplash.com/photo-1552832230-c0197dd311b5?ixlib=rb-1.2.1&auto=format&fit=crop&w=800&q=80")));
            } else if (name.contains("cena a trastevere") || name.contains("trastevere")) {
                tpl.setImages(new java.util.ArrayList<>(List.of("https://images.unsplash.com/photo-1515003197210-e0cd71810b5f?auto=format&fit=crop&w=800&q=80")));
            } else if (name.contains("trekking")) {
                tpl.setImages(new java.util.ArrayList<>(List.of("https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?ixlib=rb-1.2.1&auto=format&fit=crop&w=800&q=80")));
            } else if (name.contains("spa") || name.contains("terme")) {
                tpl.setImages(new java.util.ArrayList<>(List.of("https://images.unsplash.com/photo-1540555700478-4be289fbecef?auto=format&fit=crop&w=800&q=80")));
            } else if (name.contains("cucina")) {
                tpl.setImages(new java.util.ArrayList<>(List.of("https://images.unsplash.com/photo-1556910103-1c02745aae4d?ixlib=rb-1.2.1&auto=format&fit=crop&w=800&q=80")));
            } else if (name.contains("gondola")) {
                tpl.setImages(new java.util.ArrayList<>(List.of("https://images.unsplash.com/photo-1527631746610-bca00a040d60?auto=format&fit=crop&w=800&q=80")));
            } else if (name.contains("walking tour") || name.contains("milano") || name.contains("free walking")) {
                tpl.setImages(new java.util.ArrayList<>(List.of("https://images.unsplash.com/photo-1520175480921-4edfa2983e0f?auto=format&fit=crop&w=800&q=80")));
            }
            activityTemplateRepository.save(tpl);
        });
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
        Location loc2 = locationRepository.findByNameIgnoreCase("Trentino-Alto Adige, Italia")
                .orElseGet(() -> {
                    Location loc = new Location();
                    loc.setName("Trentino-Alto Adige, Italia");
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
        tpl1.setLocation("Roma, Italia");
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
        tpl2.setLocation("Roma, Italia");
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
        tpl3.setLocation("Trentino-Alto Adige, Italia");
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
        tpl4.setLocation("Trentino-Alto Adige, Italia");
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
        tplSafari.setLocation("Nairobi, Kenya");
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
        tpl5.setLocation("Firenze, Italia");
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
        tpl6.setLocation("Venezia, Italia");
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
        tpl7.setLocation("Milano, Italia");
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

        // --- Località Kyoto ---
        Location locKyoto = locationRepository.findByNameIgnoreCase("Kyoto, Giappone")
                .orElseGet(() -> {
                    Location loc = new Location();
                    loc.setName("Kyoto, Giappone");
                    loc.setDescription("L'antica capitale del Giappone, celebre per i templi buddisti, i giardini e le geisha.");
                    return locationRepository.save(loc);
                });

        // --- Località Berlino ---
        Location locBerlino = locationRepository.findByNameIgnoreCase("Berlino, Germania")
                .orElseGet(() -> {
                    Location loc = new Location();
                    loc.setName("Berlino, Germania");
                    loc.setDescription("La capitale tedesca, intrisa di storia del XX secolo e famosa per l'arte e la vita notturna.");
                    return locationRepository.save(loc);
                });

        // --- Località Madrid ---
        Location locMadrid = locationRepository.findByNameIgnoreCase("Madrid, Spagna")
                .orElseGet(() -> {
                    Location loc = new Location();
                    loc.setName("Madrid, Spagna");
                    loc.setDescription("Capitale della Spagna, ricca di eleganti viali, palazzi storici e musei d'arte di fama mondiale.");
                    return locationRepository.save(loc);
                });

        // --- Località Chicago ---
        Location locChicago = locationRepository.findByNameIgnoreCase("Chicago, USA")
                .orElseGet(() -> {
                    Location loc = new Location();
                    loc.setName("Chicago, USA");
                    loc.setDescription("La Windy City, nota per la sua architettura avveniristica e le sculture d'arte pubblica.");
                    return locationRepository.save(loc);
                });

        // Kyoto - Preferito
        Itinerary itineraryKyoto = new Itinerary();
        itineraryKyoto.setId(UUID.fromString("11111111-2222-3333-4444-555555555555"));
        itineraryKyoto.setTitle("Fascino di Kyoto");
        itineraryKyoto.setDescription("Esperienza autentica tra templi, tè e foreste di bamboo.");
        itineraryKyoto.setStartDateTime(LocalDateTime.now().plusDays(15));
        itineraryKyoto.setEndDateTime(LocalDateTime.now().plusDays(20));
        itineraryKyoto.setCreator(organizer);

        // Berlino - Non preferito
        Itinerary itineraryBerlino = new Itinerary();
        itineraryBerlino.setId(UUID.fromString("66666666-7777-8888-9999-000000000000"));
        itineraryBerlino.setTitle("Berlino e la sua Storia");
        itineraryBerlino.setDescription("Muro di Berlino, Currywurst e cultura underground.");
        itineraryBerlino.setStartDateTime(LocalDateTime.now().plusDays(5));
        itineraryBerlino.setEndDateTime(LocalDateTime.now().plusDays(8));
        itineraryBerlino.setCreator(organizer);

        // Madrid - Preferito
        Itinerary itineraryMadrid = new Itinerary();
        itineraryMadrid.setId(UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee"));
        itineraryMadrid.setTitle("Weekend Real a Madrid");
        itineraryMadrid.setDescription("Arte, storia e sapori tradizionali spagnoli.");
        itineraryMadrid.setStartDateTime(LocalDateTime.now().plusDays(12));
        itineraryMadrid.setEndDateTime(LocalDateTime.now().plusDays(15));
        itineraryMadrid.setCreator(organizer);

        // Chicago - Non preferito
        Itinerary itineraryChicago = new Itinerary();
        itineraryChicago.setId(UUID.fromString("ffffffff-0000-1111-2222-333333333333"));
        itineraryChicago.setTitle("Grattacieli di Chicago");
        itineraryChicago.setDescription("Architettura moderna e crociera fluviale.");
        itineraryChicago.setStartDateTime(LocalDateTime.now().plusDays(25));
        itineraryChicago.setEndDateTime(LocalDateTime.now().plusDays(28));
        itineraryChicago.setCreator(organizer);

        // Kyoto Activities
        ActivityTemplate tplKyotoTea = new ActivityTemplate();
        tplKyotoTea.setName("Cerimonia del Tè");
        tplKyotoTea.setDescription("Un'esperienza di meditazione e degustazione del Matcha tradizionale.");
        tplKyotoTea.setLocation("Kyoto, Giappone");
        tplKyotoTea.setLocationEntity(locKyoto);
        tplKyotoTea.setOrganizer(organizer);
        tplKyotoTea.setTags(new HashSet<>(Arrays.asList(TravelTag.CULTURA, TravelTag.CIBO, TravelTag.RELAX)));
        tplKyotoTea = activityTemplateRepository.save(tplKyotoTea);

        Activity actKyotoTea = new Activity();
        actKyotoTea.setId(UUID.fromString("a1a1a1a1-b2b2-c3c3-d4d4-e5e5e5e5e5e5"));
        actKyotoTea.setTemplate(tplKyotoTea);
        actKyotoTea.setStartTime(LocalDateTime.now().plusDays(16).plusHours(15));
        actKyotoTea.setEndTime(LocalDateTime.now().plusDays(16).plusHours(17));
        actKyotoTea.setParticipants(8);
        actKyotoTea.setPrice(new BigDecimal("45.00"));

        ActivityTemplate tplKyotoBamboo = new ActivityTemplate();
        tplKyotoBamboo.setName("Foresta di Bamboo");
        tplKyotoBamboo.setDescription("Passeggiata guidata tra i suggestivi sentieri di Arashiyama.");
        tplKyotoBamboo.setLocation("Kyoto, Giappone");
        tplKyotoBamboo.setLocationEntity(locKyoto);
        tplKyotoBamboo.setOrganizer(organizer);
        tplKyotoBamboo.setTags(new HashSet<>(Arrays.asList(TravelTag.NATURA, TravelTag.AVVENTURA, TravelTag.FOTOGRAFIA)));
        tplKyotoBamboo = activityTemplateRepository.save(tplKyotoBamboo);

        Activity actKyotoBamboo = new Activity();
        actKyotoBamboo.setTemplate(tplKyotoBamboo);
        actKyotoBamboo.setStartTime(LocalDateTime.now().plusDays(17).plusHours(9));
        actKyotoBamboo.setEndTime(LocalDateTime.now().plusDays(17).plusHours(12));
        actKyotoBamboo.setParticipants(15);
        actKyotoBamboo.setPrice(new BigDecimal("15.00"));

        // Berlino Activities
        ActivityTemplate tplMuroBerlino = new ActivityTemplate();
        tplMuroBerlino.setName("Tour del Muro di Berlino");
        tplMuroBerlino.setDescription("Passeggiata storica lungo la East Side Gallery con guida locale.");
        tplMuroBerlino.setLocation("Berlino, Germania");
        tplMuroBerlino.setLocationEntity(locBerlino);
        tplMuroBerlino.setOrganizer(organizer);
        tplMuroBerlino.setTags(new HashSet<>(Arrays.asList(TravelTag.STORIA, TravelTag.CULTURA)));
        tplMuroBerlino = activityTemplateRepository.save(tplMuroBerlino);

        Activity actMuroBerlino = new Activity();
        actMuroBerlino.setTemplate(tplMuroBerlino);
        actMuroBerlino.setStartTime(LocalDateTime.now().plusDays(6).plusHours(10));
        actMuroBerlino.setEndTime(LocalDateTime.now().plusDays(6).plusHours(13));
        actMuroBerlino.setParticipants(20);
        actMuroBerlino.setPrice(new BigDecimal("10.00"));

        // Madrid Activities
        ActivityTemplate tplPradoMadrid = new ActivityTemplate();
        tplPradoMadrid.setName("Visita al Museo del Prado");
        tplPradoMadrid.setDescription("Capolavori di Velázquez, Goya ed El Greco con storico dell'arte.");
        tplPradoMadrid.setLocation("Madrid, Spagna");
        tplPradoMadrid.setLocationEntity(locMadrid);
        tplPradoMadrid.setOrganizer(organizer);
        tplPradoMadrid.setTags(new HashSet<>(Arrays.asList(TravelTag.CULTURA, TravelTag.STORIA, TravelTag.ARTE)));
        tplPradoMadrid = activityTemplateRepository.save(tplPradoMadrid);

        Activity actPradoMadrid = new Activity();
        actPradoMadrid.setId(UUID.fromString("f1f1f1f1-e2e2-d3d3-c4c4-b5b5b5b5b5b5"));
        actPradoMadrid.setTemplate(tplPradoMadrid);
        actPradoMadrid.setStartTime(LocalDateTime.now().plusDays(13).plusHours(14));
        actPradoMadrid.setEndTime(LocalDateTime.now().plusDays(13).plusHours(17));
        actPradoMadrid.setParticipants(12);
        actPradoMadrid.setPrice(new BigDecimal("30.00"));

        // Chicago Activities
        ActivityTemplate tplCrocieraChicago = new ActivityTemplate();
        tplCrocieraChicago.setName("Crociera sull'Architettura");
        tplCrocieraChicago.setDescription("Crociera in barca per ammirare i grattacieli iconici di Chicago.");
        tplCrocieraChicago.setLocation("Chicago, USA");
        tplCrocieraChicago.setLocationEntity(locChicago);
        tplCrocieraChicago.setOrganizer(organizer);
        tplCrocieraChicago.setTags(new HashSet<>(Arrays.asList(TravelTag.CITTA, TravelTag.NATURA, TravelTag.AVVENTURA, TravelTag.ARCHITETTURA)));
        tplCrocieraChicago = activityTemplateRepository.save(tplCrocieraChicago);

        Activity actCrocieraChicago = new Activity();
        actCrocieraChicago.setTemplate(tplCrocieraChicago);
        actCrocieraChicago.setStartTime(LocalDateTime.now().plusDays(26).plusHours(11));
        actCrocieraChicago.setEndTime(LocalDateTime.now().plusDays(26).plusHours(13));
        actCrocieraChicago.setParticipants(25);
        actCrocieraChicago.setPrice(new BigDecimal("50.00"));

        // Save all activities
        activityRepository.saveAll(Arrays.asList(
            act1, act2, act3, act4, actSafari, act5, act6, act7,
            actKyotoTea, actKyotoBamboo, actMuroBerlino, actPradoMadrid, actCrocieraChicago
        ));

        // Link activities to itineraries
        itinerary1.setActivities(Arrays.asList(act1, act2));
        itinerary2.setActivities(Arrays.asList(act3, act4));
        itinerary3.setActivities(Arrays.asList(actSafari));
        itineraryKyoto.setActivities(Arrays.asList(actKyotoTea, actKyotoBamboo));
        itineraryBerlino.setActivities(Arrays.asList(actMuroBerlino));
        itineraryMadrid.setActivities(Arrays.asList(actPradoMadrid));
        itineraryChicago.setActivities(Arrays.asList(actCrocieraChicago));

        // Save all itineraries
        itineraryRepository.saveAll(Arrays.asList(
            itinerary1, itinerary2, itinerary3,
            itineraryKyoto, itineraryBerlino, itineraryMadrid, itineraryChicago
        ));
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
