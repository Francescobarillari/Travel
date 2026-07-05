package it.unical.ea.Travel.Config;

import it.unical.ea.Travel.Entities.activity.Activity;
import it.unical.ea.Travel.Entities.itinerary.Itinerary;
import it.unical.ea.Travel.Entities.localita.Localita;
import it.unical.ea.Travel.Entities.user.User;
import it.unical.ea.Travel.Repositories.activity.ActivityRepository;
import it.unical.ea.Travel.Repositories.itinerary.ItineraryRepository;
import it.unical.ea.Travel.Repositories.localita.LocalitaRepository;
import it.unical.ea.Travel.Repositories.user.UserRepository;
import it.unical.ea.enums.UserType;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Component
@Profile("!test")
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ActivityRepository activityRepository;
    private final ItineraryRepository itineraryRepository;
    private final LocalitaRepository localitaRepository;

    public DataSeeder(UserRepository userRepository, ActivityRepository activityRepository, ItineraryRepository itineraryRepository, LocalitaRepository localitaRepository) {
        this.userRepository = userRepository;
        this.activityRepository = activityRepository;
        this.itineraryRepository = itineraryRepository;
        this.localitaRepository = localitaRepository;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (localitaRepository.count() == 0 && activityRepository.count() == 0) {
            seedData();
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

        // --- Località 1: Roma ---
        Localita loc1 = new Localita();
        loc1.setName("Roma, Italia");
        loc1.setDescription("La capitale italiana, famosa per la sua storia, cultura e il Colosseo.");
        localitaRepository.save(loc1);

        Activity act1 = new Activity();
        act1.setName("Visita al Colosseo");
        act1.setDescription("Tour guidato al Colosseo e Fori Imperiali.");
        act1.setLocation("Roma");
        act1.setStartTime(LocalDateTime.now().plusDays(10).plusHours(9));
        act1.setEndTime(LocalDateTime.now().plusDays(10).plusHours(12));
        act1.setParticipants(20);
        act1.setPrice(new BigDecimal("35.00"));
        act1.setLocalita(loc1);
        act1.setOrganizer(organizer);
        act1.setImages(List.of("https://images.unsplash.com/photo-1552832230-c0197dd311b5?ixlib=rb-1.2.1&auto=format&fit=crop&w=800&q=80"));

        Activity act2 = new Activity();
        act2.setName("Cena a Trastevere");
        act2.setDescription("Cena tipica romana in una trattoria a Trastevere.");
        act2.setLocation("Roma");
        act2.setStartTime(LocalDateTime.now().plusDays(10).plusHours(20));
        act2.setEndTime(LocalDateTime.now().plusDays(10).plusHours(23));
        act2.setParticipants(15);
        act2.setPrice(new BigDecimal("45.00"));
        act2.setLocalita(loc1);
        act2.setOrganizer(organizer);

        // --- Località 2: Alpi ---
        Localita loc2 = new Localita();
        loc2.setName("Trentino-Alto Adige");
        loc2.setDescription("Trekking, natura e relax in montagna. Per ricaricare le batterie lontano dalla città.");
        localitaRepository.save(loc2);

        Activity act3 = new Activity();
        act3.setName("Trekking Tre Cime");
        act3.setDescription("Escursione panoramica alle Tre Cime di Lavaredo.");
        act3.setLocation("Dolomiti");
        act3.setStartTime(LocalDateTime.now().plusDays(21).plusHours(8));
        act3.setEndTime(LocalDateTime.now().plusDays(21).plusHours(16));
        act3.setParticipants(10);
        act3.setPrice(new BigDecimal("25.00"));
        act3.setLocalita(loc2);
        act3.setOrganizer(organizer);
        act3.setImages(List.of("https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?ixlib=rb-1.2.1&auto=format&fit=crop&w=800&q=80"));

        Activity act4 = new Activity();
        act4.setName("Spa in Montagna");
        act4.setDescription("Giornata di relax alle terme alpine.");
        act4.setLocation("Dolomiti");
        act4.setStartTime(LocalDateTime.now().plusDays(22).plusHours(10));
        act4.setEndTime(LocalDateTime.now().plusDays(22).plusHours(18));
        act4.setParticipants(10);
        act4.setPrice(new BigDecimal("60.00"));
        act4.setLocalita(loc2);
        act4.setOrganizer(organizer);

        // --- Località 3: Kenya ---
        Localita loc3 = new Localita();
        loc3.setName("Nairobi, Kenya");
        loc3.setDescription("Un'avventura indimenticabile nella savana africana.");
        localitaRepository.save(loc3);

        Activity actSafari = new Activity();
        actSafari.setName("Game Drive");
        actSafari.setDescription("Safari in Jeep per vedere i Big Five.");
        actSafari.setLocation("Masai Mara");
        actSafari.setStartTime(LocalDateTime.now().plusDays(41).plusHours(6));
        actSafari.setEndTime(LocalDateTime.now().plusDays(41).plusHours(12));
        actSafari.setParticipants(6);
        actSafari.setPrice(new BigDecimal("150.00"));
        actSafari.setLocalita(loc3);
        actSafari.setOrganizer(organizer);

        activityRepository.saveAll(Arrays.asList(act1, act2, act3, act4, actSafari));
    }
}
