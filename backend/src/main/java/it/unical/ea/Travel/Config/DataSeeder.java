package it.unical.ea.Travel.Config;

import it.unical.ea.Travel.Entities.activity.Activity;
import it.unical.ea.Travel.Entities.activity.ActivityTemplate;
import it.unical.ea.Travel.Entities.itinerary.Itinerary;
import it.unical.ea.Travel.Entities.location.Location;
import it.unical.ea.Travel.Entities.user.User;
import it.unical.ea.Travel.Entities.review.Review;
import it.unical.ea.Travel.Repositories.activity.ActivityRepository;
import it.unical.ea.Travel.Repositories.activity.ActivityTemplateRepository;
import it.unical.ea.Travel.Repositories.activity.ActivityBookingRepository;
import it.unical.ea.Travel.Repositories.itinerary.ItineraryRepository;
import it.unical.ea.Travel.Repositories.itinerary.ItineraryBookingRepository;
import it.unical.ea.Travel.Repositories.location.LocationRepository;
import it.unical.ea.Travel.Repositories.user.UserRepository;
import it.unical.ea.Travel.Repositories.review.ReviewRepository;
import it.unical.ea.Travel.Repositories.favorite.FavoriteListRepository;
import it.unical.ea.Travel.Repositories.notification.NotificationRepository;
import it.unical.ea.Travel.Services.keycloak.KeycloakAdminService;
import it.unical.ea.Travel.Services.keycloak.KeycloakUserAlreadyExistsException;
import it.unical.ea.Travel.Services.location.LocationService;
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
import java.util.*;

@Slf4j
@Component
@Profile("!test")
@Order(1)
public class DataSeeder implements CommandLineRunner {

    private static final String TEST_EMAIL = "a@a.it";
    private static final String TEST_PASSWORD = "aaa";

    private final UserRepository userRepository;
    private final ActivityTemplateRepository activityTemplateRepository;
    private final ActivityRepository activityRepository;
    private final ItineraryRepository itineraryRepository;
    private final LocationRepository locationRepository;
    private final LocationService locationService;
    private final KeycloakAdminService keycloakAdminService;
    private final ReviewRepository reviewRepository;
    private final FavoriteListRepository favoriteListRepository;
    private final NotificationRepository notificationRepository;
    private final ItineraryBookingRepository itineraryBookingRepository;
    private final ActivityBookingRepository activityBookingRepository;

    public DataSeeder(UserRepository userRepository, ActivityTemplateRepository activityTemplateRepository, ActivityRepository activityRepository,
                      ItineraryRepository itineraryRepository, LocationRepository locationRepository, LocationService locationService,
                      KeycloakAdminService keycloakAdminService, ReviewRepository reviewRepository,
                      FavoriteListRepository favoriteListRepository, NotificationRepository notificationRepository,
                      ItineraryBookingRepository itineraryBookingRepository, ActivityBookingRepository activityBookingRepository) {
        this.userRepository = userRepository;
        this.activityTemplateRepository = activityTemplateRepository;
        this.activityRepository = activityRepository;
        this.itineraryRepository = itineraryRepository;
        this.locationRepository = locationRepository;
        this.locationService = locationService;
        this.keycloakAdminService = keycloakAdminService;
        this.reviewRepository = reviewRepository;
        this.favoriteListRepository = favoriteListRepository;
        this.notificationRepository = notificationRepository;
        this.itineraryBookingRepository = itineraryBookingRepository;
        this.activityBookingRepository = activityBookingRepository;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        seedTestUser();
        
        log.info(" Pulizia database per la generazione del dataset demo...");
        cleanupDatabase();

        log.info("📍 Seeding e allineamento delle località...");
        seedAndHealLocations();
        
        log.info("Seeding del nuovo ricco dataset demo...");
        seedData();
        
        log.info(" Generazione recensioni (almeno 4-5 per attività e itinerario)...");
        seedReviews();
        
        log.info(" Database popolato con successo!");
    }

    private void cleanupDatabase() {
        notificationRepository.deleteAll();
        favoriteListRepository.deleteAll();
        reviewRepository.deleteAll();
        itineraryBookingRepository.deleteAll();
        activityBookingRepository.deleteAll();
        itineraryRepository.deleteAll();
        activityRepository.deleteAll();
        activityTemplateRepository.deleteAll();
        
        // Delete all users except the test user
        userRepository.findAll().forEach(u -> {
            if (!u.getEmail().equalsIgnoreCase(TEST_EMAIL)) {
                userRepository.delete(u);
            }
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
        }
    }

    private void seedAndHealLocations() {
        List<String> cities = Arrays.asList(
            "Roma, Italia", "Parigi, Francia", "Londra, Regno Unito", "New York, USA", "Tokyo, Giappone",
            "Barcellona, Spagna", "Venezia, Italia", "Firenze, Italia", "Sydney, Australia", "Rio de Janeiro, Brasile",
            "Cairo, Egitto", "Atene, Grecia", "Amsterdam, Paesi Basi", "Dubai, Emirati Arabi Uniti", "Istanbul, Turchia",
            "Nairobi, Kenya", "Trentino-Alto Adige, Italia", "Città del Capo, Sudafrica", "Praga, Repubblica Ceca", "San Francisco, USA",
            "Tropea, Italia", "Reggio Calabria, Italia", "Cosenza, Italia", "Scilla, Italia", "Milano, Italia", "Napoli, Italia",
            "Torino, Italia", "Bologna, Italia", "Palermo, Italia", "Verona, Italia", "Lecce, Italia",
            "Cortina d'Ampezzo, Italia", "Costiera Amalfitana, Italia"
        );
        for (String city : cities) {
            locationService.getOrCreateLocation(city);
        }

        locationRepository.findAll().forEach(loc -> {
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

    private String copyLocalImage(String filename, String subDir) {
        java.io.File sourceFile = new java.io.File("pics/" + filename);
        if (!sourceFile.exists()) {
            sourceFile = new java.io.File("../pics/" + filename);
        }
        
        if (!sourceFile.exists()) {
            return null;
        }

        try {
            java.nio.file.Path targetDir = java.nio.file.Paths.get("uploads/" + subDir).toAbsolutePath().normalize();
            java.nio.file.Files.createDirectories(targetDir);
            
            java.nio.file.Path targetPath = targetDir.resolve(filename).normalize();
            java.nio.file.Files.copy(sourceFile.toPath(), targetPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            
            log.info("📸 File locale copiato: {} -> {}", filename, targetPath);
            return subDir + "/" + filename;
        } catch (Exception e) {
            log.warn("⚠️ Errore durante la copia dell'immagine {}: {}", filename, e.getMessage());
            return null;
        }
    }

    private void seedData() {
        // --- 1. Seed 10 Organizers (SOCIETA) ---
        List<User> organizers = new ArrayList<>();
        String[] companyNames = {
            "Calabria Tour Operator", "Roma ArcheoTours SRL", "Milano Style & Food", 
            "Venezia Gondola Experience", "Toscana Bella Tours", "Campania Vesuvio Guides", 
            "Torino Reale Travel", "Dolomiti Adventure Alps", "Sicilia Bedda Vacanze", 
            "Puglia Sole & Salento"
        };
        for (int i = 1; i <= 10; i++) {
            User org = new User();
            org.setEmail("organizer" + i + "@test.com");
            org.setPasswordHash("password_seeded");
            org.setUserType(UserType.SOCIETA);
            org.setCompanyName(companyNames[i - 1]);
            org.setVatNumber("IT" + String.format("%011d", i));
            org.setRoles("ROLE_ORGANIZER");
            org.setKeycloakId(UUID.randomUUID().toString());
            org.setApproved(true);
            org.setEmailVerified(true);
            organizers.add(userRepository.save(org));
        }

        // --- 2. Seed 10 Travelers (VIAGGIATORE) ---
        List<User> travelers = new ArrayList<>();
        String[] firstNames = {"Alessandro", "Giulia", "Federico", "Chiara", "Matteo", "Sofia", "Andrea", "Sara", "Davide", "Elena"};
        String[] lastNames = {"Rossi", "Bianchi", "Verdi", "Neri", "Russo", "Ferrari", "Esposito", "Romano", "Ricci", "Marino"};
        TravelTag[][] prefTags = {
            {TravelTag.AVVENTURA, TravelTag.NATURA},
            {TravelTag.CULTURA, TravelTag.CIBO},
            {TravelTag.ROMANTICISMO, TravelTag.CITTA},
            {TravelTag.RELAX, TravelTag.MONTAGNA},
            {TravelTag.SAFARI, TravelTag.AVVENTURA},
            {TravelTag.CITTA, TravelTag.CULTURA},
            {TravelTag.CIBO, TravelTag.RELAX},
            {TravelTag.NATURA, TravelTag.MONTAGNA},
            {TravelTag.AVVENTURA, TravelTag.CIBO},
            {TravelTag.ROMANTICISMO, TravelTag.RELAX}
        };

        for (int i = 1; i <= 10; i++) {
            User trav = new User();
            trav.setEmail("viaggiatore" + i + "@test.com");
            trav.setPasswordHash("password_seeded");
            trav.setUserType(UserType.VIAGGIATORE);
            trav.setFirstName(firstNames[i - 1]);
            trav.setLastName(lastNames[i - 1]);
            trav.setRoles("ROLE_VIAGGIATORE");
            trav.setKeycloakId(UUID.randomUUID().toString());
            trav.setPreferences(new HashSet<>(Arrays.asList(prefTags[i - 1])));
            trav.setEmailVerified(true);
            travelers.add(userRepository.save(trav));
        }

        // Ensure test user is also present in travelers list for reviews
        userRepository.getUserByEmail(TEST_EMAIL).ifPresent(travelers::add);

        // --- 3. Seed/Load Locations ---
        String[] cities = {
            "Roma, Italia", "Milano, Italia", "Venezia, Italia", "Firenze, Italia", "Napoli, Italia",
            "Tropea, Italia", "Reggio Calabria, Italia", "Cosenza, Italia", "Scilla, Italia",
            "Torino, Italia", "Bologna, Italia", "Palermo, Italia", "Verona, Italia", "Lecce, Italia",
            "Cortina d'Ampezzo, Italia", "Costiera Amalfitana, Italia"
        };
        Map<String, Location> locationMap = new HashMap<>();
        for (String city : cities) {
            Location loc = locationRepository.findByNameIgnoreCase(city)
                    .orElseGet(() -> {
                        Location newLoc = new Location();
                        newLoc.setName(city);
                        newLoc.setDescription("Splendida località italiana ricca di attrazioni e cultura.");
                        return locationRepository.save(newLoc);
                    });
            locationMap.put(city, loc);
        }

        // --- 4. Seed Activity Templates (22 Templates) ---
        List<ActivityTemplate> templates = new ArrayList<>();
        
        // Calabria templates (Focus - 2 templates per location to avoid identical activities in itineraries)
        templates.add(createTemplate("Giro in Barca a Tropea e Capo Vaticano", "Esplora le acque cristalline e le grotte marine della Costa degli Dei.", "Tropea, Italia", locationMap.get("Tropea, Italia"), organizers.get(0), List.of(), Set.of(TravelTag.MARE, TravelTag.NATURA, TravelTag.ROMANTICISMO)));
        templates.add(createTemplate("Trekking Urbano a Tropea", "Passeggiata guidata nel centro storico tra vicoli e degustazione di cipolla rossa.", "Tropea, Italia", locationMap.get("Tropea, Italia"), organizers.get(0), Set.of(TravelTag.CIBO, TravelTag.CULTURA)));

        templates.add(createTemplate("Bronzi di Riace & Museo Nazionale", "Visita guidata al Museo Archeologico Nazionale di Reggio Calabria per ammirare i celebri Bronzi.", "Reggio Calabria, Italia", locationMap.get("Reggio Calabria, Italia"), organizers.get(0), Set.of(TravelTag.CULTURA, TravelTag.STORIA)));
        templates.add(createTemplate("Passeggiata sul Lungomare Falcomatà", "Passeggia sul 'più bel chilometro d'Italia' con degustazione del famoso gelato Cesare.", "Reggio Calabria, Italia", locationMap.get("Reggio Calabria, Italia"), organizers.get(0), Set.of(TravelTag.RELAX, TravelTag.CIBO)));

        templates.add(createTemplate("Trekking nel Parco Nazionale della Sila", "Escursione tra i boschi incontaminati e i laghi della Sila, respirando l'aria più pura d'Europa.", "Cosenza, Italia", locationMap.get("Cosenza, Italia"), organizers.get(0), Set.of(TravelTag.NATURA, TravelTag.TREKKING, TravelTag.MONTAGNA, TravelTag.AVVENTURA)));
        templates.add(createTemplate("Castello Normanno-Svevo Tour", "Visita al celebre castello sul colle Pancrazio per una vista panoramica di Cosenza.", "Cosenza, Italia", locationMap.get("Cosenza, Italia"), organizers.get(0), Set.of(TravelTag.STORIA, TravelTag.CULTURA)));

        templates.add(createTemplate("Cena Tipica a Scilla nel borgo di Chianalea", "Degusta il celebre panino col pesce spada e altre prelibatezze in riva al mare.", "Scilla, Italia", locationMap.get("Scilla, Italia"), organizers.get(0), Set.of(TravelTag.CIBO, TravelTag.ROMANTICISMO)));
        templates.add(createTemplate("Snorkeling nella Spiaggia delle Sirene", "Esplora i meravigliosi fondali sotto il castello di Ruffo di Scilla.", "Scilla, Italia", locationMap.get("Scilla, Italia"), organizers.get(0), Set.of(TravelTag.MARE, TravelTag.NATURA, TravelTag.AVVENTURA)));

        // Rome templates
        templates.add(createTemplate("Visita Guidata al Colosseo e Fori", "Esplora la storia dell'Impero Romano con una guida esperta.", "Roma, Italia", locationMap.get("Roma, Italia"), organizers.get(1), List.of("colosseo.jpg"), Set.of(TravelTag.CULTURA, TravelTag.STORIA)));
        templates.add(createTemplate("Cena Tradizionale a Trastevere", "Assapora la vera cucina romana in una trattoria tipica a Trastevere.", "Roma, Italia", locationMap.get("Roma, Italia"), organizers.get(1), List.of(), Set.of(TravelTag.CIBO, TravelTag.CITTA)));
        
        // Milan templates
        templates.add(createTemplate("Duomo di Milano & Terrazze", "Sali sulle terrazze del Duomo di Milano per godere di un panorama mozzafiato.", "Milano, Italia", locationMap.get("Milano, Italia"), organizers.get(2), List.of("duomo.jpg"), Set.of(TravelTag.CULTURA, TravelTag.CITTA)));
        templates.add(createTemplate("Aperitivo sui Navigli", "Gusta un tradicional aperitivo milanese sulle sponde dei Navigli storici.", "Milano, Italia", locationMap.get("Milano, Italia"), organizers.get(2), List.of(), Set.of(TravelTag.CIBO, TravelTag.RELAX)));
        
        // Venice templates
        templates.add(createTemplate("Giro Romantico in Gondola", "Gondola condivisa o privata lungo il Canal Grande e i canali più nascosti.", "Venezia, Italia", locationMap.get("Venezia, Italia"), organizers.get(3), List.of("gondola.jpg"), Set.of(TravelTag.ROMANTICISMO, TravelTag.CULTURA)));
        templates.add(createTemplate("Laboratorio di Maschere Veneziane", "Crea e decora la tua maschera del Carnevale di Venezia con un maestro artigiano.", "Venezia, Italia", locationMap.get("Venezia, Italia"), organizers.get(3), List.of(), Set.of(TravelTag.CULTURA, TravelTag.RELAX)));
        
        // Florence templates
        templates.add(createTemplate("Galleria degli Uffizi Tour", "Ammira i capolavori del Rinascimento con una visita guidata salta-fila.", "Firenze, Italia", locationMap.get("Firenze, Italia"), organizers.get(4), List.of("uffizi.jpg"), Set.of(TravelTag.CULTURA, TravelTag.STORIA)));
        templates.add(createTemplate("Lezione di Cucina Toscana", "Impara a preparare la pasta fatta in casa ed i cantucci toscani.", "Firenze, Italia", locationMap.get("Firenze, Italia"), organizers.get(4), List.of(), Set.of(TravelTag.CIBO, TravelTag.CULTURA)));
        
        // Naples templates
        templates.add(createTemplate("Scavi Archeologici di Pompei Tour", "Un viaggio indietro nel tempo per visitare i resti dell'antica città romana.", "Napoli, Italia", locationMap.get("Napoli, Italia"), organizers.get(5), List.of("pompei.jpg"), Set.of(TravelTag.CULTURA, TravelTag.STORIA, TravelTag.AVVENTURA)));
        templates.add(createTemplate("Masterclass Pizza Napoletana", "Tutti i segreti dell'impasto della vera pizza napoletana con un maestro pizzaiolo.", "Napoli, Italia", locationMap.get("Napoli, Italia"), organizers.get(5), List.of(), Set.of(TravelTag.CIBO, TravelTag.RELAX)));

        // Torino template
        templates.add(createTemplate("Museo Egizio & Torino Reale", "Visita il secondo museo egizio più importante al mondo nel cuore di Torino.", "Torino, Italia", locationMap.get("Torino, Italia"), organizers.get(6), List.of(), Set.of(TravelTag.CULTURA, TravelTag.STORIA)));
        
        // Bologna template
        templates.add(createTemplate("Food Tour Bologna la Grassa", "Degustazione guidata di mortadella, parmigiano, aceto balsamico e tortellini.", "Bologna, Italia", locationMap.get("Bologna, Italia"), organizers.get(9), List.of(), Set.of(TravelTag.CIBO, TravelTag.CITTA)));
        
        // Palermo template
        templates.add(createTemplate("Street Food Palermitano", "Tour dei mercati storici di Palermo per gustare arancine, panelle e sfincione.", "Palermo, Italia", locationMap.get("Palermo, Italia"), organizers.get(7), List.of(), Set.of(TravelTag.CIBO, TravelTag.AVVENTURA)));
        
        // Cortina template
        templates.add(createTemplate("Trekking Tre Cime di Lavaredo", "Escursione indimenticabile sulle vette dolomitiche più famose d'Italia.", "Cortina d'Ampezzo, Italia", locationMap.get("Cortina d'Ampezzo, Italia"), organizers.get(8), List.of(), Set.of(TravelTag.AVVENTURA, TravelTag.MONTAGNA, TravelTag.NATURA)));

        // --- 5. Seed Activity Sessions (at least 50 Sessions in total -> 66 Sessions here) ---
        List<Activity> allActivities = new ArrayList<>();
        BigDecimal[] prices = {
            new BigDecimal("35.00"), new BigDecimal("25.00"), new BigDecimal("40.00"), new BigDecimal("45.00"),
            new BigDecimal("35.00"), new BigDecimal("45.00"), new BigDecimal("25.00"), new BigDecimal("60.00"),
            new BigDecimal("90.00"), new BigDecimal("40.00"), new BigDecimal("55.00"), new BigDecimal("75.00"),
            new BigDecimal("50.00"), new BigDecimal("30.00"), new BigDecimal("20.00"), new BigDecimal("80.00"),
            new BigDecimal("15.00"), new BigDecimal("110.00")
        };
        Integer[] capacities = {15, 20, 25, 12, 20, 15, 10, 25, 6, 12, 30, 8, 40, 18, 50, 14, 35, 15};

        for (int t = 0; t < templates.size(); t++) {
            ActivityTemplate tpl = templates.get(t);
            BigDecimal price = prices[t % prices.length];
            int cap = capacities[t % capacities.length];

            // 3 sessions per template -> 66 total activities
            for (int s = 1; s <= 3; s++) {
                Activity act = new Activity();
                act.setTemplate(tpl);
                act.setStartTime(LocalDateTime.now().plusDays(s * 5).plusHours(9 + (s % 2) * 4));
                act.setEndTime(LocalDateTime.now().plusDays(s * 5).plusHours(12 + (s % 2) * 5));
                act.setParticipants(cap);
                act.setPrice(price);
                allActivities.add(activityRepository.save(act));
            }
        }
        log.info("✅ Seeding di {} sessioni di attività completato.", allActivities.size());

        // --- 6. Seed Itineraries (9 Premium + 43 Dynamic = 52 total) ---
        List<Itinerary> allItineraries = new ArrayList<>();
        
        // 9 Detailed Manual Itineraries (linking 2 different templates in each city)
        allItineraries.add(createItinerary("Weekend da Sogno a Tropea", "Relax in barca e trekking urbano sulla Costa degli Dei.", organizers.get(0), List.of(allActivities.get(0), allActivities.get(3)), null));
        allItineraries.add(createItinerary("Fascino Antico a Reggio", "Dalla bellezza dei Bronzi di Riace al Lungomare con delizioso gelato.", organizers.get(0), List.of(allActivities.get(6), allActivities.get(9)), null));
        allItineraries.add(createItinerary("Sila Wild Adventure", "Esplora i laghi silani e visita il Castello storico.", organizers.get(0), List.of(allActivities.get(12), allActivities.get(15)), null));
        allItineraries.add(createItinerary("Incanto a Scilla e Chianalea", "Cena tipica in riva al mare e snorkeling nella Spiaggia delle Sirene.", organizers.get(0), List.of(allActivities.get(18), allActivities.get(21)), null));
        allItineraries.add(createItinerary("Gran Tour di Roma", "Il meglio della Città Eterna: Colosseo e cena a Trastevere.", organizers.get(1), List.of(allActivities.get(24), allActivities.get(27)), null));
        allItineraries.add(createItinerary("Milano Moderna e Gusto", "Salita sulle terrazze del Duomo e aperitivo sui Navigli.", organizers.get(2), List.of(allActivities.get(30), allActivities.get(33)), null));
        allItineraries.add(createItinerary("Fascino Veneziano", "Romanticismo in gondola e laboratorio di maschere veneziane.", organizers.get(3), List.of(allActivities.get(36), allActivities.get(39)), null));
        allItineraries.add(createItinerary("Arte e Cucina a Firenze", "I capolavori degli Uffizi e lezione di cucina toscana.", organizers.get(4), List.of(allActivities.get(42), allActivities.get(45)), null));
        allItineraries.add(createItinerary("Sole e Sapori di Napoli", "Dalla pizza napoletana al tour archeologico di Pompei.", organizers.get(5), List.of(allActivities.get(48), allActivities.get(51)), null));

        // 43 Dynamically generated itineraries to reach 52 total
        String[] titles = {
            "Weekend a", "Alla scoperta di", "Guida Storica per", "Le prelibatezze di", "Fuga Romantica a",
            "Esperienza di Gruppo a", "Outdoor e Avventura a", "Emozioni nel cuore di", "Itinerario Culturale in"
        };
        for (int i = 10; i <= 52; i++) {
            User creator = organizers.get(i % organizers.size());
            String locationName = cities[i % cities.length];
            
            // Strictly fetch activities that belong to this exact location
            List<Activity> matchedActs = allActivities.stream()
                    .filter(a -> a.getTemplate().getLocation().equals(locationName))
                    .toList();

            // Group activities by template to ensure we pick DIFFERENT activities
            Map<ActivityTemplate, List<Activity>> actsByTemplate = new LinkedHashMap<>();
            for (Activity act : matchedActs) {
                actsByTemplate.computeIfAbsent(act.getTemplate(), k -> new ArrayList<>()).add(act);
            }

            List<Activity> itineraryActivities = new ArrayList<>();
            for (List<Activity> sessions : actsByTemplate.values()) {
                if (!sessions.isEmpty()) {
                    itineraryActivities.add(sessions.get(0)); // Add first session of this template
                }
            }

            if (!itineraryActivities.isEmpty()) {
                Itinerary iti = new Itinerary();
                iti.setTitle(titles[i % titles.length] + " " + locationName.split(",")[0].trim() + " #" + (i - 9));
                iti.setDescription("Un fantastico itinerario curato da " + creator.getCompanyName() + " per esplorare " + locationName);
                iti.setStartDateTime(LocalDateTime.now().plusDays(i + 5));
                iti.setEndDateTime(LocalDateTime.now().plusDays(i + 8));
                iti.setCreator(creator);
                iti.setActivities(itineraryActivities);
                iti.setVisibility("PUBLIC");
                allItineraries.add(itineraryRepository.save(iti));
            }
        }
        log.info("✅ Seeding di {} itinerari completato.", allItineraries.size());
    }

    private ActivityTemplate createTemplate(String name, String desc, String locName, Location locEntity, User organizer, Set<TravelTag> tags) {
        return createTemplate(name, desc, locName, locEntity, organizer, List.of(), tags);
    }

    private ActivityTemplate createTemplate(String name, String desc, String locName, Location locEntity, User organizer, List<String> localPics, Set<TravelTag> tags) {
        ActivityTemplate tpl = new ActivityTemplate();
        tpl.setName(name);
        tpl.setDescription(desc);
        tpl.setLocation(locName);
        tpl.setLocationEntity(locEntity);
        tpl.setOrganizer(organizer);
        tpl.setTags(tags);

        List<String> images = new ArrayList<>();
        for (String pic : localPics) {
            String path = copyLocalImage(pic, "activities");
            if (path != null) {
                images.add(path);
            }
        }
        if (images.isEmpty() && !name.toLowerCase().contains("maschere")) {
            images.add(getFallbackImageUrl(name));
        }
        tpl.setImages(images);

        return activityTemplateRepository.save(tpl);
    }

    private Itinerary createItinerary(String title, String desc, User creator, List<Activity> activities, String localPic) {
        Itinerary itinerary = new Itinerary();
        itinerary.setTitle(title);
        itinerary.setDescription(desc);
        itinerary.setStartDateTime(LocalDateTime.now().plusDays(10));
        itinerary.setEndDateTime(LocalDateTime.now().plusDays(14));
        itinerary.setCreator(creator);
        itinerary.setActivities(activities);
        itinerary.setVisibility("PUBLIC");

        if (localPic != null) {
            String path = copyLocalImage(localPic, "itineraries");
            if (path != null) {
                itinerary.setImagePath(path);
            } else {
                itinerary.setImagePath(getFallbackItineraryUrl(title));
            }
        } else {
            itinerary.setImagePath(getFallbackItineraryUrl(title));
        }

        return itineraryRepository.save(itinerary);
    }


    // NOTA SULLE IMMAGINI DI RIPAGINAMENTO / FALLBACK (UNSPLASH):
    // Sappiamo bene che caricare le immagini staticamente tramite URL esterni di Unsplash
    // non è una soluzione ideale per una produzione professionale. Questo approccio viene
    // utilizzato esclusivamente in questa fase di seeding per la demo dell'applicazione.
    // Nella realtà, quando gli utenti caricano immagini vere per attività ed itinerari,
    // queste vengono salvate a livello del file system del backend (dentro il volume Docker
    // mappato "/app/uploads") e associate nel database tramite un UUID univoco specifico
    // per ciascun file, garantendo performance ottimali, persistenza e isolamento.
    private String getFallbackImageUrl(String activityName) {
        String lower = activityName.toLowerCase();
        if (lower.contains("tropea") || lower.contains("costa degli dei"))
            return "https://images.unsplash.com/photo-1590001155093-a3c66ab0c3ff?q=80&w=800&auto=format&fit=crop";
        if (lower.contains("bronzi") || lower.contains("reggio"))
            return "https://images.unsplash.com/photo-1549887534-1541e9326642?q=80&w=800&auto=format&fit=crop";
        if (lower.contains("sila") || lower.contains("trekking"))
            return "https://images.unsplash.com/photo-1441974231531-c6227db76b6e?q=80&w=800&auto=format&fit=crop";
        if (lower.contains("scilla") || lower.contains("chianalea"))
            return "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?q=80&w=800&auto=format&fit=crop";
        if (lower.contains("colosseo"))
            return "https://images.unsplash.com/photo-1552832230-c0197dd311b5?auto=format&fit=crop&w=800&q=80";
        if (lower.contains("trastevere"))
            return "https://images.unsplash.com/photo-1515003197210-e0cd71810b5f?auto=format&fit=crop&w=800&q=80";
        if (lower.contains("duomo"))
            return "https://images.unsplash.com/photo-1520175480921-4edfa2983e0f?auto=format&fit=crop&w=800&q=80";
        if (lower.contains("aperitivo") || lower.contains("navigli"))
            return "https://images.unsplash.com/photo-1574085733277-851d9d856a3a?q=80&w=800&auto=format&fit=crop";
        if (lower.contains("gondola"))
            return "https://images.unsplash.com/photo-1527631746610-bca00a040d60?auto=format&fit=crop&w=800&q=80";
        if (lower.contains("maschere"))
            return "https://images.unsplash.com/photo-1517524008697-84bbe3c3fd98?q=80&w=800&auto=format&fit=crop";
        if (lower.contains("uffizi"))
            return "https://images.unsplash.com/photo-1478147427282-58a87a120781?auto=format&fit=crop&w=800&q=80";
        if (lower.contains("cucina") || lower.contains("pasta"))
            return "https://images.unsplash.com/photo-1556910103-1c02745aae4d?ixlib=rb-1.2.1&auto=format&fit=crop&w=800&q=80";
        if (lower.contains("pompei"))
            return "https://images.unsplash.com/photo-1595183350284-ff7741d40131?q=80&w=800&auto=format&fit=crop";
        if (lower.contains("pizza"))
            return "https://images.unsplash.com/photo-1513104890138-7c749659a591?q=80&w=800&auto=format&fit=crop";
        if (lower.contains("egizio"))
            return "https://images.unsplash.com/photo-1539650116574-8efeb43e2750?q=80&w=800&auto=format&fit=crop";
        if (lower.contains("gastronomico") || lower.contains("bologna"))
            return "https://images.unsplash.com/photo-1563245372-f21724e3856d?q=80&w=800&auto=format&fit=crop";
        if (lower.contains("street food") || lower.contains("palermo"))
            return "https://images.unsplash.com/photo-1541532713592-79a0317b6b77?q=80&w=800&auto=format&fit=crop";
        if (lower.contains("dolomiti"))
            return "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?ixlib=rb-1.2.1&auto=format&fit=crop&w=800&q=80";
        if (lower.contains("barca") || lower.contains("positano"))
            return "https://images.unsplash.com/photo-1533105079780-92b9be482077?q=80&w=800&auto=format&fit=crop";
        return "https://images.unsplash.com/photo-1488646953014-85cb44e25828?auto=format&fit=crop&w=800&q=80";
    }

    private String getFallbackItineraryUrl(String itineraryTitle) {
        String lower = itineraryTitle.toLowerCase();
        if (lower.contains("calabria") || lower.contains("tropea") || lower.contains("scilla") || lower.contains("sila") || lower.contains("reggio"))
            return "https://images.unsplash.com/photo-1590001155093-a3c66ab0c3ff?q=80&w=800&auto=format&fit=crop";
        if (lower.contains("roma"))
            return "https://images.unsplash.com/photo-1552832230-c0197dd311b5?ixlib=rb-1.2.1&auto=format&fit=crop&w=800&q=80";
        if (lower.contains("milano"))
            return "https://images.unsplash.com/photo-1520175480921-4edfa2983e0f?ixlib=rb-1.2.1&auto=format&fit=crop&w=800&q=80";
        if (lower.contains("venezia") || lower.contains("fascino"))
            return "https://images.unsplash.com/photo-1527631746610-bca00a040d60?ixlib=rb-1.2.1&auto=format&fit=crop&w=800&q=80";
        if (lower.contains("firenze") || lower.contains("arte"))
            return "https://images.unsplash.com/photo-1478147427282-58a87a120781?ixlib=rb-1.2.1&auto=format&fit=crop&w=800&q=80";
        if (lower.contains("napoli") || lower.contains("sud"))
            return "https://images.unsplash.com/photo-1599682715474-361182378581?q=80&w=800&auto=format&fit=crop";
        return "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?ixlib=rb-1.2.1&auto=format&fit=crop&w=800&q=80";
    }

    private void seedReviews() {
        List<User> travelers = userRepository.findByUserType(UserType.VIAGGIATORE);
        if (travelers.isEmpty()) return;

        List<ActivityTemplate> templates = activityTemplateRepository.findAll();
        List<Itinerary> itineraries = itineraryRepository.findAll();

        String[] comments = {
            "Esperienza stupenda, la consiglio a tutti!",
            "Tutto perfetto, organizzazione impeccabile.",
            "Davvero interessante e ben fatto. Consigliato!",
            "Un'esperienza unica che rifarei sicuramente.",
            "Molto carino e piacevole, adatto a tutte le età.",
            "Una delle migliori attività provate di recente.",
            "Ottimo rapporto qualità prezzo, molto soddisfatto.",
            "Guida professionale e super simpatica. 5 stelle!",
            "Atmosfera magica e posti mozzafiato.",
            "Organizzato benissimo, cura in ogni minimo dettaglio."
        };

        double[] ratings = {5.0, 4.5, 4.0, 5.0, 4.5};

        int reviewCount = 0;
        
        // Seed 4-5 reviews per ActivityTemplate
        for (ActivityTemplate tpl : templates) {
            int numReviews = 4 + (tpl.hashCode() % 2 == 0 ? 1 : 0);
            Set<Integer> selectedTravelers = new HashSet<>();
            for (int r = 0; r < numReviews; r++) {
                int travelerIndex = Math.abs((tpl.hashCode() + r * 7) % travelers.size());
                for (int attempts = 0; attempts < travelers.size(); attempts++) {
                    if (selectedTravelers.add(travelerIndex)) {
                        break;
                    }
                    travelerIndex = (travelerIndex + 1) % travelers.size();
                }
                
                User author = travelers.get(travelerIndex);
                double rating = ratings[(tpl.hashCode() + r) % ratings.length];
                String comment = comments[(tpl.hashCode() + r * 3) % comments.length];

                Review review = new Review();
                review.setAuthor(author);
                review.setActivityTemplate(tpl);
                review.setRating(rating);
                review.setComment(comment);
                reviewRepository.save(review);
                reviewCount++;
            }
        }

        // Seed 4-5 reviews per Itinerary
        for (Itinerary iti : itineraries) {
            int numReviews = 4 + (iti.hashCode() % 2 == 0 ? 1 : 0);
            Set<Integer> selectedTravelers = new HashSet<>();
            for (int r = 0; r < numReviews; r++) {
                int travelerIndex = Math.abs((iti.hashCode() + r * 13) % travelers.size());
                for (int attempts = 0; attempts < travelers.size(); attempts++) {
                    if (selectedTravelers.add(travelerIndex)) {
                        break;
                    }
                    travelerIndex = (travelerIndex + 1) % travelers.size();
                }
                
                User author = travelers.get(travelerIndex);
                double rating = ratings[(iti.hashCode() + r) % ratings.length];
                String comment = comments[(iti.hashCode() + r * 5) % comments.length];

                Review review = new Review();
                review.setAuthor(author);
                review.setItinerary(iti);
                review.setRating(rating);
                review.setComment(comment);
                reviewRepository.save(review);
                reviewCount++;
            }
        }

        log.info("✅ Generazione completata con successo: {} recensioni inserite.", reviewCount);
    }
}
