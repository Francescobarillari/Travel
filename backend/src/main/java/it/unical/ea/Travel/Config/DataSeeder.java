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
import it.unical.ea.Travel.Repositories.itinerary.ItineraryJoinRequestRepository;
import it.unical.ea.Travel.Repositories.location.LocationRepository;
import it.unical.ea.Travel.Repositories.user.UserRepository;
import it.unical.ea.Travel.Repositories.review.ReviewRepository;
import it.unical.ea.Travel.Repositories.favorite.FavoriteListRepository;
import it.unical.ea.Travel.Repositories.notification.NotificationRepository;
import it.unical.ea.Travel.Services.keycloak.KeycloakAdminService;
import it.unical.ea.Travel.Services.keycloak.KeycloakUserAlreadyExistsException;
import it.unical.ea.Travel.Services.location.LocationService;
import it.unical.ea.Travel.Entities.itinerary.ItineraryJoinRequest;
import it.unical.ea.dtos.authDto.SignupRequest;
import it.unical.ea.enums.JoinRequestStatus;
import it.unical.ea.enums.UserType;
import it.unical.ea.enums.TravelTag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
    private final ItineraryJoinRequestRepository itineraryJoinRequestRepository;

    public DataSeeder(UserRepository userRepository, ActivityTemplateRepository activityTemplateRepository, ActivityRepository activityRepository,
                      ItineraryRepository itineraryRepository, LocationRepository locationRepository, LocationService locationService,
                      KeycloakAdminService keycloakAdminService, ReviewRepository reviewRepository,
                      FavoriteListRepository favoriteListRepository, NotificationRepository notificationRepository,
                      ItineraryBookingRepository itineraryBookingRepository, ActivityBookingRepository activityBookingRepository,
                      ItineraryJoinRequestRepository itineraryJoinRequestRepository) {
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
        this.itineraryJoinRequestRepository = itineraryJoinRequestRepository;
    }

    @Override
    public void run(String... args) {
        if (itineraryRepository.count() >= 50 && activityRepository.count() >= 1000) {
            log.info("ℹ️ Dataset demo completo già presente nel database ({} attività e {} itinerari trovati), skip pulizia e rigenerazione.", activityRepository.count(), itineraryRepository.count());
            return;
        }

        seedTestUser();
        
        log.info("🧹 Pulizia database per la generazione del nuovo dataset demo esteso fino al 31 Ottobre...");
        cleanupDatabase();

        log.info("📍 Seeding e allineamento delle località...");
        seedAndHealLocations();
        
        log.info("🚀 Seeding del ricco dataset demo (5 attività al giorno per città fino al 31 Ottobre + Itinerari di Viaggiatori e Società)...");
        seedData();
        
        log.info("⭐ Generazione recensioni (4-5 per attività e itinerario)...");
        seedReviews();
        
        log.info("🎉 Database popolato con successo!");
    }

    private void cleanupDatabase() {
        notificationRepository.deleteAll();
        favoriteListRepository.deleteAll();
        reviewRepository.deleteAll();
        itineraryJoinRequestRepository.deleteAll();
        itineraryBookingRepository.deleteAll();
        activityBookingRepository.deleteAll();
        itineraryRepository.deleteAll();
        activityRepository.deleteAll();
        activityTemplateRepository.deleteAll();
        
        Set<String> preservedEmails = Set.of(
            "a@a.com",
            "a@a.it",
            "admin-user@example.com",
            "basic-user@example.com"
        );
        userRepository.findAll().forEach(u -> {
            if (u.getEmail() != null && !preservedEmails.contains(u.getEmail().toLowerCase().trim())) {
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
        // Data Base: 30 Agosto dell'anno corrente
        int baseYear = LocalDate.now().getYear();
        LocalDate baseDate = LocalDate.of(baseYear, 8, 30);

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
                        newLoc.setDescription("Splendida località ricca di attrazioni, esperienze e cultura.");
                        return locationRepository.save(newLoc);
                    });
            locationMap.put(city, loc);
        }

        // --- 4. Seed Activity Templates (3 template tematici per ciascuna delle 16 città = 48 template) ---
        List<ActivityTemplate> templates = new ArrayList<>();
        Map<String, List<ActivityTemplate>> cityTemplatesMap = new LinkedHashMap<>();

        // Helper to register templates per city
        java.util.function.Consumer<ActivityTemplate> addTpl = tpl -> {
            templates.add(tpl);
            cityTemplatesMap.computeIfAbsent(tpl.getLocation(), k -> new ArrayList<>()).add(tpl);
        };

        // 1. Roma, Italia (Org 1: Roma ArcheoTours)
        addTpl.accept(createTemplate("Visita Guidata al Colosseo e Fori Romani", "Esplora la grandezza dell'Impero Romano e dell'Anfiteatro Flavio con una guida archeologica.", "Roma, Italia", locationMap.get("Roma, Italia"), organizers.get(1), List.of("colosseo.jpg"), Set.of(TravelTag.CULTURA, TravelTag.STORIA, TravelTag.STORIA_ANTICA)));
        addTpl.accept(createTemplate("Cena Tradizionale nel Cuore di Trastevere", "Assapora carbonara, amatriciana e carciofi alla giudia in una caratteristica osteria trasteverina.", "Roma, Italia", locationMap.get("Roma, Italia"), organizers.get(1), List.of(), Set.of(TravelTag.CIBO, TravelTag.CITTA, TravelTag.ENOGASTRONOMIA)));
        addTpl.accept(createTemplate("Tour Notturno delle Piazze e Fontane Barocche", "Passeggiata suggestiva tra Fontana di Trevi, Piazza Navona e il Pantheon illuminato.", "Roma, Italia", locationMap.get("Roma, Italia"), organizers.get(1), List.of(), Set.of(TravelTag.ROMANTICISMO, TravelTag.CITTA, TravelTag.ARCHITETTURA)));

        // 2. Milano, Italia (Org 2: Milano Style & Food)
        addTpl.accept(createTemplate("Duomo di Milano & Terrazze Panoramiche", "Ammira le guglie gotiche e il panorama della città meneghina fino alle Alpi.", "Milano, Italia", locationMap.get("Milano, Italia"), organizers.get(2), List.of("duomo.jpg"), Set.of(TravelTag.CULTURA, TravelTag.CITTA, TravelTag.ARCHITETTURA)));
        addTpl.accept(createTemplate("Aperitivo Gourmet sui Navigli Storici", "Gusta cocktail d'autore e finger food tipico milanese lungo il Naviglio Grande.", "Milano, Italia", locationMap.get("Milano, Italia"), organizers.get(2), List.of(), Set.of(TravelTag.CIBO, TravelTag.RELAX, TravelTag.CITTA)));
        addTpl.accept(createTemplate("Pinacoteca di Brera & Quadrilatero della Moda", "Un viaggio tra capolavori rinascimentali e l'eleganza senza tempo di via Montenapoleone.", "Milano, Italia", locationMap.get("Milano, Italia"), organizers.get(2), List.of(), Set.of(TravelTag.ARTE, TravelTag.CULTURA, TravelTag.SHOPPING)));

        // 3. Venezia, Italia (Org 3: Venezia Gondola Experience)
        addTpl.accept(createTemplate("Giro Romantico in Gondola nel Canal Grande", "Lasciati cullare dall'acqua tra ponti storici, palazzi patrizi e canali nascosti.", "Venezia, Italia", locationMap.get("Venezia, Italia"), organizers.get(3), List.of("gondola.jpg"), Set.of(TravelTag.ROMANTICISMO, TravelTag.CULTURA, TravelTag.MARE)));
        addTpl.accept(createTemplate("Laboratorio Artigianale di Maschere Veneziane", "Crea e decora la tua maschera originale del Carnevale insieme a maestri cartapestai.", "Venezia, Italia", locationMap.get("Venezia, Italia"), organizers.get(3), List.of(), Set.of(TravelTag.CULTURA, TravelTag.RELAX, TravelTag.ARTE)));
        addTpl.accept(createTemplate("Escursione in Barca a Murano e Burano", "Scopri l'arte del vetro soffiato e le celebri case variopinte dei pescatori di Burano.", "Venezia, Italia", locationMap.get("Venezia, Italia"), organizers.get(3), List.of(), Set.of(TravelTag.MARE, TravelTag.NATURA, TravelTag.CULTURA)));

        // 4. Firenze, Italia (Org 4: Toscana Bella Tours)
        addTpl.accept(createTemplate("Galleria degli Uffizi Tour Guidato Salta-Fila", "Ammira la Venere del Botticelli, Leonardo e Caravaggio con una guida storica dell'arte.", "Firenze, Italia", locationMap.get("Firenze, Italia"), organizers.get(4), List.of("uffizi.jpg"), Set.of(TravelTag.CULTURA, TravelTag.STORIA, TravelTag.ARTE)));
        addTpl.accept(createTemplate("Lezione di Cucina Toscana e Degustazione Chianti", "Impara i segreti della pasta fresca, cantucci e pici abbinati a calici di ottimo Chianti Classico.", "Firenze, Italia", locationMap.get("Firenze, Italia"), organizers.get(4), List.of(), Set.of(TravelTag.CIBO, TravelTag.CULTURA, TravelTag.ENOGASTRONOMIA)));
        addTpl.accept(createTemplate("Tramonto Panoramico al Piazzale Michelangelo", "Passeggiata guidata dal Ponte Vecchio fino al belvedere più suggestivo della città.", "Firenze, Italia", locationMap.get("Firenze, Italia"), organizers.get(4), List.of(), Set.of(TravelTag.ROMANTICISMO, TravelTag.RELAX, TravelTag.FOTOGRAFIA)));

        // 5. Napoli, Italia (Org 5: Campania Vesuvio Guides)
        addTpl.accept(createTemplate("Scavi Archeologici di Pompei con Archeologo", "Un viaggio straordinario nel 79 d.C. alla scoperta di strade, domus e affreschi millenari.", "Napoli, Italia", locationMap.get("Napoli, Italia"), organizers.get(5), List.of("pompei.jpg"), Set.of(TravelTag.CULTURA, TravelTag.STORIA, TravelTag.STORIA_ANTICA)));
        addTpl.accept(createTemplate("Masterclass Pizza Napoletana Verace", "Metti le mani in pasta e inforna la tua Margherita con un autentico mastro pizzaiolo partenopeo.", "Napoli, Italia", locationMap.get("Napoli, Italia"), organizers.get(5), List.of(), Set.of(TravelTag.CIBO, TravelTag.RELAX, TravelTag.DIVERTIMENTO)));
        addTpl.accept(createTemplate("Napoli Sotterranea e Spaccanapoli Experience", "Cunicoli greco-romani nel sottosuolo ed escursione folkloristica tra i presepi di San Gregorio Armeno.", "Napoli, Italia", locationMap.get("Napoli, Italia"), organizers.get(5), List.of(), Set.of(TravelTag.STORIA, TravelTag.CULTURA, TravelTag.AVVENTURA)));

        // 6. Tropea, Italia (Org 0: Calabria Tour Operator)
        addTpl.accept(createTemplate("Giro in Barca a Tropea e Capo Vaticano", "Esplora acque turchesi, cale segrete e fondali mozzafiato lungo la splendida Costa degli Dei.", "Tropea, Italia", locationMap.get("Tropea, Italia"), organizers.get(0), List.of(), Set.of(TravelTag.MARE, TravelTag.NATURA, TravelTag.ROMANTICISMO)));
        addTpl.accept(createTemplate("Trekking Urbano e Degustazione Cipolla Rossa IGP", "Passeggiata tra i vicoli affacciati sulla rupe e assaggi di 'Nduja e specialità calabresi.", "Tropea, Italia", locationMap.get("Tropea, Italia"), organizers.get(0), List.of(), Set.of(TravelTag.CIBO, TravelTag.CULTURA, TravelTag.STORIA)));
        addTpl.accept(createTemplate("Tramonto in Kayak e Snorkeling a Tropea", "Pagaia nelle acque limpide al tramonto ammirando il santuario di Santa Maria dell'Isola.", "Tropea, Italia", locationMap.get("Tropea, Italia"), organizers.get(0), List.of(), Set.of(TravelTag.MARE, TravelTag.AVVENTURA, TravelTag.SPORT)));

        // 7. Reggio Calabria, Italia (Org 0: Calabria Tour Operator)
        addTpl.accept(createTemplate("Bronzi di Riace & Museo Archeologico Nazionale", "Incontra da vicino i leggendari capolavori bronzei del V secolo a.C. e la storia della Magna Grecia.", "Reggio Calabria, Italia", locationMap.get("Reggio Calabria, Italia"), organizers.get(0), List.of(), Set.of(TravelTag.CULTURA, TravelTag.STORIA, TravelTag.MUSEI)));
        addTpl.accept(createTemplate("Passeggiata sul Lungomare Falcomatà & Gelato Cesare", "Vivi il 'più bel chilometro d'Italia' con una sosta golosa al chiosco più premiato d'Italia.", "Reggio Calabria, Italia", locationMap.get("Reggio Calabria, Italia"), organizers.get(0), List.of(), Set.of(TravelTag.RELAX, TravelTag.CIBO, TravelTag.ROMANTICISMO)));
        addTpl.accept(createTemplate("Tour delle Coltivazioni di Bergamotto", "Scopri l'oro verde di Reggio Calabria tra profumati agrumeti e degustazioni di liquori tipici.", "Reggio Calabria, Italia", locationMap.get("Reggio Calabria, Italia"), organizers.get(0), List.of(), Set.of(TravelTag.NATURA, TravelTag.CIBO, TravelTag.ENOGASTRONOMIA)));

        // 8. Cosenza, Italia (Org 0: Calabria Tour Operator)
        addTpl.accept(createTemplate("Trekking nel Parco Nazionale della Sila e Laghi", "Escursione tra pini loricati giganti, laghi montani e l'aria certificata più pura d'Europa.", "Cosenza, Italia", locationMap.get("Cosenza, Italia"), organizers.get(0), List.of(), Set.of(TravelTag.NATURA, TravelTag.TREKKING, TravelTag.MONTAGNA, TravelTag.AVVENTURA)));
        addTpl.accept(createTemplate("Castello Normanno-Svevo e Centro Storico di Cosenza", "Visita la maestosa fortezza sul colle Pancrazio e il suggestivo borgo antico dei Bruzi.", "Cosenza, Italia", locationMap.get("Cosenza, Italia"), organizers.get(0), List.of(), Set.of(TravelTag.STORIA, TravelTag.CULTURA, TravelTag.ARCHITETTURA)));
        addTpl.accept(createTemplate("Degustazione Sapori Silani: Caciocavallo e Funghi", "Esperienza gastronomica in agriturismo con prodotti tipici d'eccellenza dell'altopiano.", "Cosenza, Italia", locationMap.get("Cosenza, Italia"), organizers.get(0), List.of(), Set.of(TravelTag.CIBO, TravelTag.ENOGASTRONOMIA, TravelTag.MONTAGNA)));

        // 9. Scilla, Italia (Org 0: Calabria Tour Operator)
        addTpl.accept(createTemplate("Cena Romantica a Chianalea sul Mare", "Gusta il celebre panino al pesce spada e specialità marinare sulle tipiche pedane a pelo d'acqua.", "Scilla, Italia", locationMap.get("Scilla, Italia"), organizers.get(0), List.of(), Set.of(TravelTag.CIBO, TravelTag.ROMANTICISMO, TravelTag.MARE)));
        addTpl.accept(createTemplate("Snorkeling nella Spiaggia delle Sirene", "Immergiti tra i fondali ricchi di biodiversità sotto l'imponente rocca del Castello Ruffo.", "Scilla, Italia", locationMap.get("Scilla, Italia"), organizers.get(0), List.of(), Set.of(TravelTag.MARE, TravelTag.NATURA, TravelTag.AVVENTURA)));
        addTpl.accept(createTemplate("Visita al Castello Ruffo e Belvedere sullo Stretto", "Vista panoramica spettacolare sulla Sicilia, l'Etna e le acque mitologiche dello Stretto.", "Scilla, Italia", locationMap.get("Scilla, Italia"), organizers.get(0), List.of(), Set.of(TravelTag.STORIA, TravelTag.CULTURA, TravelTag.FOTOGRAFIA)));

        // 10. Torino, Italia (Org 6: Torino Reale Travel)
        addTpl.accept(createTemplate("Museo Egizio & Torino Reale Tour", "Visita il secondo museo di antichità egizie al mondo e le splendide piazze sabaude.", "Torino, Italia", locationMap.get("Torino, Italia"), organizers.get(6), List.of(), Set.of(TravelTag.CULTURA, TravelTag.STORIA, TravelTag.MUSEI)));
        addTpl.accept(createTemplate("Tour del Cioccolato, Bicerin e Caffè Storici", "Assapora il gianduiotto originale e la tradizionale bevanda calda nei locali d'epoca.", "Torino, Italia", locationMap.get("Torino, Italia"), organizers.get(6), List.of(), Set.of(TravelTag.CIBO, TravelTag.RELAX, TravelTag.CULTURA)));
        addTpl.accept(createTemplate("Mole Antonelliana e Museo Nazionale del Cinema", "Sali con l'ascensore panoramico per una visuale a 360 gradi e immergiti nel mondo del cinema.", "Torino, Italia", locationMap.get("Torino, Italia"), organizers.get(6), List.of(), Set.of(TravelTag.ARTE, TravelTag.CULTURA, TravelTag.ARCHITETTURA)));

        // 11. Bologna, Italia (Org 9: Puglia Sole & Salento)
        addTpl.accept(createTemplate("Food Tour Bologna la Grassa: Salumi e Parmigiano", "Tour gastronomico nel Quadrilatero con degustazione di mortadella, tortellini e calici locali.", "Bologna, Italia", locationMap.get("Bologna, Italia"), organizers.get(9), List.of(), Set.of(TravelTag.CIBO, TravelTag.CITTA, TravelTag.ENOGASTRONOMIA)));
        addTpl.accept(createTemplate("Salita sulla Torre degli Asinelli e Piazza Maggiore", "Panoramica mozzafiato dall'alto delle celebri Due Torri e racconto dei segreti medievali.", "Bologna, Italia", locationMap.get("Bologna, Italia"), organizers.get(9), List.of(), Set.of(TravelTag.STORIA, TravelTag.CITTA, TravelTag.AVVENTURA)));
        addTpl.accept(createTemplate("Masterclass Pasta Fresca e Ragù alla Bolognese", "Impara a tirare la sfoglia a mano al mattarello per preparare tagliatelle e tortelloni perfetti.", "Bologna, Italia", locationMap.get("Bologna, Italia"), organizers.get(9), List.of(), Set.of(TravelTag.CIBO, TravelTag.DIVERTIMENTO, TravelTag.CULTURA)));

        // 12. Palermo, Italia (Org 7: Sicilia Bedda Vacanze)
        addTpl.accept(createTemplate("Street Food Palermitano tra Ballarò e il Capo", "Gusta arancine croccanti, panelle, sfincione e cannoli freschi immerso nel folklore siciliano.", "Palermo, Italia", locationMap.get("Palermo, Italia"), organizers.get(7), List.of(), Set.of(TravelTag.CIBO, TravelTag.AVVENTURA, TravelTag.CULTURA)));
        addTpl.accept(createTemplate("Palazzo dei Normanni e Cappella Palatina", "I capolavori arabo-normanni patrimonio UNESCO con mosaici dorati bizantini senza eguali.", "Palermo, Italia", locationMap.get("Palermo, Italia"), organizers.get(7), List.of(), Set.of(TravelTag.STORIA, TravelTag.CULTURA, TravelTag.ARTE)));
        addTpl.accept(createTemplate("Escursione in Barca al Golfo di Mondello", "Navigazione rilassante con bagni in acque cristalline e aperitivo siciliano a bordo.", "Palermo, Italia", locationMap.get("Palermo, Italia"), organizers.get(7), List.of(), Set.of(TravelTag.MARE, TravelTag.NATURA, TravelTag.RELAX)));

        // 13. Verona, Italia (Org 3: Venezia Gondola Experience)
        addTpl.accept(createTemplate("Arena di Verona e Casa di Giulietta Tour", "Ripercorri il mito shakespeariano tra vicoli medievali, l'Anfiteatro Romano e il celebre balcone.", "Verona, Italia", locationMap.get("Verona, Italia"), organizers.get(3), List.of(), Set.of(TravelTag.ROMANTICISMO, TravelTag.STORIA, TravelTag.CULTURA)));
        addTpl.accept(createTemplate("Degustazione Vini della Valpolicella e Amarone", "Visita a una prestigiosa cantina storica veronese con assaggi di Amarone e Ripasso.", "Verona, Italia", locationMap.get("Verona, Italia"), organizers.get(3), List.of(), Set.of(TravelTag.CIBO, TravelTag.ENOGASTRONOMIA, TravelTag.RELAX)));
        addTpl.accept(createTemplate("Passeggiata al Tramonto a Castel San Pietro", "Salita con la funicolare per ammirare le anse dell'Adige e i ponti storici di Verona.", "Verona, Italia", locationMap.get("Verona, Italia"), organizers.get(3), List.of(), Set.of(TravelTag.ROMANTICISMO, TravelTag.CITTA, TravelTag.FOTOGRAFIA)));

        // 14. Lecce, Italia (Org 9: Puglia Sole & Salento)
        addTpl.accept(createTemplate("Tour del Barocco Leccese e Santa Croce", "Ammira i ricami di pietra leccese, l'anfiteatro romano e la maestosa Piazza del Duomo.", "Lecce, Italia", locationMap.get("Lecce, Italia"), organizers.get(9), List.of(), Set.of(TravelTag.CULTURA, TravelTag.STORIA, TravelTag.ARCHITETTURA)));
        addTpl.accept(createTemplate("Degustazione Salentina: Pasticciotto e Rustico", "Itinerario dei sapori tipici con caffè leccese al latte di mandorla e prelibatezze artigianali.", "Lecce, Italia", locationMap.get("Lecce, Italia"), organizers.get(9), List.of(), Set.of(TravelTag.CIBO, TravelTag.RELAX, TravelTag.CITTA)));
        addTpl.accept(createTemplate("Laboratorio di Cartapesta Leccese", "Scopri l'antica arte scultorea salentina guidato da un maestro cartapestaio locale.", "Lecce, Italia", locationMap.get("Lecce, Italia"), organizers.get(9), List.of(), Set.of(TravelTag.ARTE, TravelTag.CULTURA, TravelTag.RELAX)));

        // 15. Cortina d'Ampezzo, Italia (Org 8: Dolomiti Adventure Alps)
        addTpl.accept(createTemplate("Trekking Tre Cime di Lavaredo e Rifugi Alpini", "Escursione spettacolare al cospetto delle pareti verticali più iconiche delle Dolomiti UNESCO.", "Cortina d'Ampezzo, Italia", locationMap.get("Cortina d'Ampezzo, Italia"), organizers.get(8), List.of(), Set.of(TravelTag.AVVENTURA, TravelTag.MONTAGNA, TravelTag.NATURA, TravelTag.TREKKING)));
        addTpl.accept(createTemplate("Escursione alle Acque Turchesi del Lago di Sorapis", "Un cammino incantevole attraverso foreste alpine fino al celebre specchio d'acqua glaciale.", "Cortina d'Ampezzo, Italia", locationMap.get("Cortina d'Ampezzo, Italia"), organizers.get(8), List.of(), Set.of(TravelTag.NATURA, TravelTag.TREKKING, TravelTag.AVVENTURA)));
        addTpl.accept(createTemplate("Cena Gourmet in Baita Montana con Vista Dolomiti", "Sapori autentici ampezzani: canederli, casunziei e dolci tipici con vista panoramica sulle vette.", "Cortina d'Ampezzo, Italia", locationMap.get("Cortina d'Ampezzo, Italia"), organizers.get(8), List.of(), Set.of(TravelTag.CIBO, TravelTag.MONTAGNA, TravelTag.RELAX)));

        // 16. Costiera Amalfitana, Italia (Org 5: Campania Vesuvio Guides)
        addTpl.accept(createTemplate("Giro in Barca da Positano a Capri e Faraglioni", "Navigazione panoramica tra baie nascoste, grotte marine e soste per nuotare in acque cobalto.", "Costiera Amalfitana, Italia", locationMap.get("Costiera Amalfitana, Italia"), organizers.get(5), List.of(), Set.of(TravelTag.MARE, TravelTag.ROMANTICISMO, TravelTag.AVVENTURA)));
        addTpl.accept(createTemplate("Trekking sul Sentiero degli Dei da Bomerano", "Uno dei percorsi panoramici più celebri al mondo sospeso tra cielo azzurro e mare.", "Costiera Amalfitana, Italia", locationMap.get("Costiera Amalfitana, Italia"), organizers.get(5), List.of(), Set.of(TravelTag.TREKKING, TravelTag.NATURA, TravelTag.AVVENTURA)));
        addTpl.accept(createTemplate("Tour di Amalfi, Ravello e Villa Rufolo", "Esplora il Duomo marinaro di Amalfi e i giardini incantati a picco sulla scogliera.", "Costiera Amalfitana, Italia", locationMap.get("Costiera Amalfitana, Italia"), organizers.get(5), List.of(), Set.of(TravelTag.CULTURA, TravelTag.ROMANTICISMO, TravelTag.STORIA)));

        log.info("✅ Seeding di {} template di attività completato per {} città.", templates.size(), cities.length);

        // --- 5. Seed Activity Sessions (5 attività al giorno per ciascuna delle 16 città fino al 31 Ottobre = 5.040 eventi) ---
        List<Activity> allActivities = new ArrayList<>();
        Map<String, List<Activity>> cityActivitiesMap = new LinkedHashMap<>();

        int totalDays = (int) java.time.temporal.ChronoUnit.DAYS.between(baseDate, LocalDate.of(baseYear, 10, 31)) + 1;
        log.info("📅 Generazione eventi per {} giorni (dal 30 Agosto al 31 Ottobre)...", totalDays);

        for (String city : cities) {
            List<ActivityTemplate> cityTpls = cityTemplatesMap.get(city);
            if (cityTpls == null || cityTpls.isEmpty()) continue;

            List<Activity> cityActs = new ArrayList<>();
            for (int day = 0; day < totalDays; day++) {
                LocalDate currentDate = baseDate.plusDays(day);

                // Slot 1: Mattina (09:00 - 11:30) - Tour Culturale / Walking
                ActivityTemplate tpl0 = cityTpls.get(0 % cityTpls.size());
                Activity act0 = new Activity();
                act0.setTemplate(tpl0);
                act0.setStartTime(currentDate.atTime(9, 0));
                act0.setEndTime(currentDate.atTime(11, 30));
                act0.setParticipants(20);
                act0.setPrice(BigDecimal.valueOf(30 + (day % 4) * 5));
                cityActs.add(act0);

                // Slot 2: Pranzo / Gusto (12:00 - 14:00) - Food Experience
                ActivityTemplate tpl1 = cityTpls.get(1 % cityTpls.size());
                Activity act1 = new Activity();
                act1.setTemplate(tpl1);
                act1.setStartTime(currentDate.atTime(12, 0));
                act1.setEndTime(currentDate.atTime(14, 0));
                act1.setParticipants(15);
                act1.setPrice(BigDecimal.valueOf(25 + (day % 3) * 5));
                cityActs.add(act1);

                // Slot 3: Pomeriggio (15:00 - 17:30) - Musei / Esperienze
                ActivityTemplate tpl2 = cityTpls.get(2 % cityTpls.size());
                Activity act2 = new Activity();
                act2.setTemplate(tpl2);
                act2.setStartTime(currentDate.atTime(15, 0));
                act2.setEndTime(currentDate.atTime(17, 30));
                act2.setParticipants(25);
                act2.setPrice(BigDecimal.valueOf(35 + (day % 5) * 5));
                cityActs.add(act2);

                // Slot 4: Tramonto Panoramico (18:00 - 19:45) - Aperitivo / Belvedere
                ActivityTemplate tpl3 = cityTpls.get(day % cityTpls.size());
                Activity act3 = new Activity();
                act3.setTemplate(tpl3);
                act3.setStartTime(currentDate.atTime(18, 0));
                act3.setEndTime(currentDate.atTime(19, 45));
                act3.setParticipants(18);
                act3.setPrice(BigDecimal.valueOf(20 + (day % 4) * 5));
                cityActs.add(act3);

                // Slot 5: Cena / Tour Notturno (20:30 - 23:00) - Notturno & Cena tipica
                ActivityTemplate tpl4 = cityTpls.get((day + 1) % cityTpls.size());
                Activity act4 = new Activity();
                act4.setTemplate(tpl4);
                act4.setStartTime(currentDate.atTime(20, 30));
                act4.setEndTime(currentDate.atTime(23, 0));
                act4.setParticipants(14);
                act4.setPrice(BigDecimal.valueOf(45 + (day % 5) * 5));
                cityActs.add(act4);
            }

            List<Activity> savedCityActs = activityRepository.saveAll(cityActs);
            allActivities.addAll(savedCityActs);
            cityActivitiesMap.put(city, savedCityActs);
        }

        log.info("✅ Seeding di {} sessioni di attività completato (5 attività al giorno per città fino al 31 Ottobre).", allActivities.size());

        // --- 6. Seed Itineraries (Itinerari di Organizzatori + Itinerari Fittizi di Utenti Viaggiatori) ---
        List<Itinerary> allItineraries = new ArrayList<>();

        // A. 16 Itinerari completi ufficiali degli organizzatori
        for (int c = 0; c < cities.length; c++) {
            String city = cities[c];
            List<Activity> cityActs = cityActivitiesMap.get(city);
            User creator = organizers.get(c % organizers.size());
            String cityNameOnly = city.split(",")[0].trim();

            if (cityActs != null && cityActs.size() >= 15) {
                // Seleziona 3 sessioni in 3 giorni consecutivi (es. Giorno 0 mattina, Giorno 1 pomeriggio, Giorno 2 sera)
                Activity a1 = cityActs.get(0);  // Day 0, Slot 0
                Activity a2 = cityActs.get(7);  // Day 1, Slot 2
                Activity a3 = cityActs.get(14); // Day 2, Slot 4
                List<Activity> selectedActs = List.of(a1, a2, a3);
                
                LocalDateTime startIti = a1.getStartTime().minusHours(1);
                LocalDateTime endIti = a3.getEndTime().plusHours(1);

                Itinerary iti = new Itinerary();
                iti.setTitle("Esperienza Completa a " + cityNameOnly);
                iti.setDescription("Tre giorni indimenticabili alla scoperta di " + cityNameOnly + " tra arte, cultura, gastronomia e paesaggi unici.");
                iti.setStartDateTime(startIti);
                iti.setEndDateTime(endIti);
                iti.setCreator(creator);
                iti.setActivities(selectedActs);
                iti.setVisibility("PUBLIC");
                iti.setImagePath(getFallbackItineraryUrl(iti.getTitle()));
                allItineraries.add(itineraryRepository.save(iti));
            }
        }

        // B. 25 Itinerari fittizi creati da UTENTI VIAGGIATORI (sia PUBLIC che SHARED con partecipanti)
        String[] travelerItineraryTitles = {
            "I miei 3 giorni a Roma tra cucina e storia antica",
            "Weekend Romantico a Firenze con tramonto a Piazzale Michelangelo",
            "Avventura e Trekking tra i Laghi della Sila con amici",
            "Napoli sotterranea, Spaccanapoli e pizza verace",
            "Venezia insolita: laguna, Murano e botteghe storiche",
            "Torino magica tra Museo Egizio, cioccolato e caffè d'epoca",
            "Costiera Amalfitana on the road da Positano a Ravello",
            "Weekend Gourmet a Bologna la Grassa tra pasta e torri",
            "Salento d'incanto tra Barocco leccese e sapori tipici",
            "Dolomiti d'autunno: trekking mozzafiato a Cortina",
            "Scilla e Tropea: mare cristallino e borghi marinari",
            "Milano Design & Food tra Duomo e Navigli",
            "Palermo autentica: mercati storici e mare a Mondello",
            "Verona d'amore: arte, Arena e vini della Valpolicella",
            "Reggio Calabria tra Bronzi di Riace e lungomare da favola",
            "Fuga di fine estate a Tropea e Capo Vaticano",
            "Weekend culturale a Roma tra piazze barocche e osterie",
            "Colori e profumi di Firenze e del Chianti",
            "Escursione e relax tra i pini della Sila cosentina",
            "Tour dei sapori partenopei con degustazioni imperdibili",
            "Gondola e segreti veneziani per un weekend speciale",
            "Torino Reale: arte, Mole Antonelliana e gianduiotti",
            "Alla scoperta delle meraviglie di Amalfi e Capri",
            "Bologna da gustare: tour dei tortellini e torre degli Asinelli",
            "Salento autunnale tra storia, cartapesta e mare"
        };

        for (int i = 0; i < travelerItineraryTitles.length; i++) {
            User travelerCreator = travelers.get(i % travelers.size());
            String city = cities[i % cities.length];
            List<Activity> cityActs = cityActivitiesMap.get(city);

            if (cityActs != null && cityActs.size() >= 30) {
                int dayOffset = (i * 2) % (totalDays - 5);
                // Prendi 2 o 3 attività nei giorni consecutivi senza sovrapposizioni
                Activity act1 = cityActs.get(dayOffset * 5 + 0); // Mattina Giorno N
                Activity act2 = cityActs.get((dayOffset + 1) * 5 + 2); // Pomeriggio Giorno N+1
                Activity act3 = cityActs.get((dayOffset + 1) * 5 + 4); // Sera Giorno N+1
                List<Activity> itiActs = (i % 3 == 0) ? List.of(act1, act2, act3) : List.of(act1, act2);

                LocalDateTime itiStart = act1.getStartTime().minusHours(2);
                LocalDateTime itiEnd = itiActs.get(itiActs.size() - 1).getEndTime().plusHours(2);

                boolean isShared = (i % 2 == 1); // Alterna tra PUBBLICO e CONDIVISO
                String shareCode = isShared ? String.format("TRV%03d", 100 + i) : null;

                Itinerary iti = new Itinerary();
                iti.setTitle(travelerItineraryTitles[i]);
                iti.setDescription("Itinerario creato e pianificato da " + travelerCreator.getFirstName() + " " + travelerCreator.getLastName() + " per vivere un'esperienza indimenticabile a " + city.split(",")[0] + ".");
                iti.setStartDateTime(itiStart);
                iti.setEndDateTime(itiEnd);
                iti.setCreator(travelerCreator);
                iti.setActivities(itiActs);
                iti.setVisibility(isShared ? "SHARED" : "PUBLIC");
                iti.setShareCode(shareCode);
                iti.setImagePath(getFallbackItineraryUrl(city));
                Itinerary savedIti = itineraryRepository.save(iti);
                allItineraries.add(savedIti);

                // Se l'itinerario è CONDIVISO, aggiungi 2 compagni di viaggio accettati
                if (isShared) {
                    for (int p = 1; p <= 2; p++) {
                        User companion = travelers.get((i + p) % travelers.size());
                        ItineraryJoinRequest joinReq = new ItineraryJoinRequest();
                        joinReq.setItinerary(savedIti);
                        joinReq.setUser(companion);
                        joinReq.setStatus(JoinRequestStatus.ACCEPTED);
                        joinReq.setCreatedAt(LocalDateTime.now().minusDays(3 - p));
                        itineraryJoinRequestRepository.save(joinReq);
                    }
                }
            }
        }

        // C. 15 Itinerari Tematici aggiuntivi degli Organizzatori
        String[] thematicTitles = {
            "Tour Enogastronomico d'Autunno a", "Gran Tour Culturale e Museale di",
            "Weekend Romantico ed Esclusivo a", "Outdoor, Trekking e Natura a", "Esperienza Fotografica e Panorami di"
        };
        for (int i = 0; i < 15; i++) {
            User creator = organizers.get(i % organizers.size());
            String city = cities[(i + 3) % cities.length];
            String cityNameOnly = city.split(",")[0].trim();
            List<Activity> cityActs = cityActivitiesMap.get(city);

            if (cityActs != null && cityActs.size() >= 25) {
                int dayOffset = (i * 3 + 1) % (totalDays - 4);
                Activity act1 = cityActs.get(dayOffset * 5 + 1); // Pranzo Giorno N
                Activity act2 = cityActs.get((dayOffset + 1) * 5 + 3); // Tramonto Giorno N+1
                List<Activity> itiActs = List.of(act1, act2);

                LocalDateTime itiStart = act1.getStartTime().minusHours(2);
                LocalDateTime itiEnd = act2.getEndTime().plusHours(3);

                Itinerary iti = new Itinerary();
                iti.setTitle(thematicTitles[i % thematicTitles.length] + " " + cityNameOnly);
                iti.setDescription("Proposta esclusiva curata da " + creator.getCompanyName() + " per scoprire il meglio di " + city + ".");
                iti.setStartDateTime(itiStart);
                iti.setEndDateTime(itiEnd);
                iti.setCreator(creator);
                iti.setActivities(itiActs);
                iti.setVisibility("PUBLIC");
                iti.setImagePath(getFallbackItineraryUrl(city));
                allItineraries.add(itineraryRepository.save(iti));
            }
        }

        log.info("✅ Seeding di {} itinerari completato (Itinerari di Viaggiatori e Organizzatori con compagni di viaggio).", allItineraries.size());
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

    private String getFallbackImageUrl(String activityName) {
        String lower = activityName.toLowerCase();
        if (lower.contains("tropea") || lower.contains("costa degli dei"))
            return "https://images.unsplash.com/photo-1590001155093-a3c66ab0c3ff?q=80&w=800&auto=format&fit=crop";
        if (lower.contains("bronzi") || lower.contains("reggio"))
            return "https://images.unsplash.com/photo-1690289793717-f92cc222752c?auto=format&fit=crop&w=800&q=80";
        if (lower.contains("sila") || lower.contains("cosenza"))
            return "https://images.unsplash.com/photo-1441974231531-c6227db76b6e?auto=format&fit=crop&w=800&q=80";
        if (lower.contains("scilla") || lower.contains("chianalea"))
            return "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?auto=format&fit=crop&w=800&q=80";
        if (lower.contains("colosseo") || lower.contains("roma"))
            return "https://images.unsplash.com/photo-1552832230-c0197dd311b5?auto=format&fit=crop&w=800&q=80";
        if (lower.contains("trastevere") || lower.contains("fontane"))
            return "https://images.unsplash.com/photo-1515003197210-e0cd71810b5f?auto=format&fit=crop&w=800&q=80";
        if (lower.contains("duomo") || lower.contains("milano") || lower.contains("brera"))
            return "https://images.unsplash.com/photo-1520175480921-4edfa2983e0f?auto=format&fit=crop&w=800&q=80";
        if (lower.contains("navigli") || lower.contains("aperitivo"))
            return "https://images.unsplash.com/photo-1574085733277-851d9d856a3a?q=80&w=800&auto=format&fit=crop";
        if (lower.contains("gondola") || lower.contains("venezia") || lower.contains("burano") || lower.contains("murano"))
            return "https://images.unsplash.com/photo-1527631746610-bca00a040d60?auto=format&fit=crop&w=800&q=80";
        if (lower.contains("maschere"))
            return "https://images.unsplash.com/photo-1517524008697-84bbe3c3fd98?auto=format&fit=crop&w=800&q=80";
        if (lower.contains("uffizi") || lower.contains("firenze") || lower.contains("michelangelo"))
            return "https://images.unsplash.com/photo-1478147427282-58a87a120781?auto=format&fit=crop&w=800&q=80";
        if (lower.contains("cucina") || lower.contains("chianti") || lower.contains("pasta"))
            return "https://images.unsplash.com/photo-1556910103-1c02745aae4d?auto=format&fit=crop&w=800&q=80";
        if (lower.contains("pompei"))
            return "https://images.unsplash.com/photo-1595183350284-ff7741d40131?q=80&w=800&auto=format&fit=crop";
        if (lower.contains("pizza") || lower.contains("napoli") || lower.contains("sotterranea"))
            return "https://images.unsplash.com/photo-1513104890138-7c749659a591?q=80&w=800&auto=format&fit=crop";
        if (lower.contains("egizio") || lower.contains("torino") || lower.contains("mole"))
            return "https://images.unsplash.com/photo-1539650116574-8efeb43e2750?q=80&w=800&auto=format&fit=crop";
        if (lower.contains("bologna") || lower.contains("mortadella") || lower.contains("asinelli"))
            return "https://images.unsplash.com/photo-1563245372-f21724e3856d?q=80&w=800&auto=format&fit=crop";
        if (lower.contains("palermo") || lower.contains("mondello") || lower.contains("ballarò"))
            return "https://images.unsplash.com/photo-1541532713592-79a0317b6b77?q=80&w=800&auto=format&fit=crop";
        if (lower.contains("verona") || lower.contains("giulietta") || lower.contains("arena") || lower.contains("valpolicella"))
            return "https://images.unsplash.com/photo-1584824486509-112e4181ff6b?auto=format&fit=crop&w=800&q=80";
        if (lower.contains("lecce") || lower.contains("salento") || lower.contains("barocco"))
            return "https://images.unsplash.com/photo-1568084680786-a84f91d1153c?auto=format&fit=crop&w=800&q=80";
        if (lower.contains("cortina") || lower.contains("dolomiti") || lower.contains("lavaredo") || lower.contains("sorapis"))
            return "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?auto=format&fit=crop&w=800&q=80";
        if (lower.contains("costiera") || lower.contains("amalfi") || lower.contains("positano") || lower.contains("capri") || lower.contains("dei"))
            return "https://images.unsplash.com/photo-1533105079780-92b9be482077?auto=format&fit=crop&w=800&q=80";
        return "https://images.unsplash.com/photo-1488646953014-85cb44e25828?auto=format&fit=crop&w=800&q=80";
    }

    private String getFallbackItineraryUrl(String itineraryTitle) {
        String lower = itineraryTitle.toLowerCase();
        if (lower.contains("calabria") || lower.contains("tropea") || lower.contains("scilla") || lower.contains("sila") || lower.contains("reggio") || lower.contains("cosenza"))
            return "https://images.unsplash.com/photo-1590001155093-a3c66ab0c3ff?q=80&w=800&auto=format&fit=crop";
        if (lower.contains("roma"))
            return "https://images.unsplash.com/photo-1552832230-c0197dd311b5?auto=format&fit=crop&w=800&q=80";
        if (lower.contains("milano"))
            return "https://images.unsplash.com/photo-1520175480921-4edfa2983e0f?auto=format&fit=crop&w=800&q=80";
        if (lower.contains("venezia"))
            return "https://images.unsplash.com/photo-1527631746610-bca00a040d60?auto=format&fit=crop&w=800&q=80";
        if (lower.contains("firenze"))
            return "https://images.unsplash.com/photo-1478147427282-58a87a120781?auto=format&fit=crop&w=800&q=80";
        if (lower.contains("napoli"))
            return "https://images.unsplash.com/photo-1599682715474-361182378581?q=80&w=800&auto=format&fit=crop";
        if (lower.contains("torino"))
            return "https://images.unsplash.com/photo-1539650116574-8efeb43e2750?auto=format&fit=crop&w=800&q=80";
        if (lower.contains("bologna"))
            return "https://images.unsplash.com/photo-1563245372-f21724e3856d?auto=format&fit=crop&w=800&q=80";
        if (lower.contains("palermo"))
            return "https://images.unsplash.com/photo-1541532713592-79a0317b6b77?auto=format&fit=crop&w=800&q=80";
        if (lower.contains("verona"))
            return "https://images.unsplash.com/photo-1584824486509-112e4181ff6b?auto=format&fit=crop&w=800&q=80";
        if (lower.contains("lecce"))
            return "https://images.unsplash.com/photo-1568084680786-a84f91d1153c?auto=format&fit=crop&w=800&q=80";
        if (lower.contains("cortina"))
            return "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?auto=format&fit=crop&w=800&q=80";
        if (lower.contains("costiera") || lower.contains("amalfi") || lower.contains("positano"))
            return "https://images.unsplash.com/photo-1533105079780-92b9be482077?auto=format&fit=crop&w=800&q=80";
        return "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?auto=format&fit=crop&w=800&q=80";
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

        log.info("✅ Generazione recensioni completata con successo: {} recensioni inserite.", reviewCount);
    }
}
