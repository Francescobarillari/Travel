package it.unical.ea.Travel.Config;

import it.unical.ea.Travel.Entities.activity.Activity;
import it.unical.ea.Travel.Entities.itinerary.Itinerary;
import it.unical.ea.Travel.Entities.trip.Trip;
import it.unical.ea.Travel.Entities.user.User;
import it.unical.ea.Travel.Repositories.activity.ActivityRepository;
import it.unical.ea.Travel.Repositories.itinerary.ItineraryRepository;
import it.unical.ea.Travel.Repositories.trip.TripRepository;
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
    private final TripRepository tripRepository;

    public DataSeeder(UserRepository userRepository, ActivityRepository activityRepository, ItineraryRepository itineraryRepository, TripRepository tripRepository) {
        this.userRepository = userRepository;
        this.activityRepository = activityRepository;
        this.itineraryRepository = itineraryRepository;
        this.tripRepository = tripRepository;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (tripRepository.count() == 0 && activityRepository.count() == 0) {
            seedData();
        }
    }

    private void seedData() {
        // Create an organizer user
        User organizer = new User();
        organizer.setEmail("organizer_" + UUID.randomUUID().toString().substring(0,8) + "@example.com");
        organizer.setPasswordHash("hashed_password");
        organizer.setUserType(UserType.SOCIETA);
        organizer.setCompanyName("Travel Adventures SRL");
        organizer.setVatNumber("IT12345678901");
        organizer.setRoles("ROLE_ORGANIZER");
        organizer.setKeycloakId(UUID.randomUUID().toString());
        organizer = userRepository.save(organizer);

        // --- Trip 1: Weekend a Roma ---
        Trip trip1 = new Trip();
        trip1.setTitle("Weekend a Roma");
        trip1.setLocation("Roma, Italia");
        trip1.setDescription("Un fantastico weekend tra storia e cultura nella capitale italiana. Include visita al Colosseo e cena tipica.");
        trip1.setDuration(3);
        trip1.setOrganizer(organizer);
        trip1.setImageUrl("https://images.unsplash.com/photo-1552832230-c0197dd311b5?ixlib=rb-1.2.1&auto=format&fit=crop&w=800&q=80");

        Itinerary itinerary1 = new Itinerary();
        itinerary1.setTitle("Itinerario Romano");
        itinerary1.setDescription("Colosseo, Vaticano e Trastevere");
        itinerary1.setStartDateTime(LocalDateTime.now().plusDays(10));
        itinerary1.setEndDateTime(LocalDateTime.now().plusDays(13));
        itinerary1.setCreator(organizer);
        itinerary1.setImagePath("https://images.unsplash.com/photo-1552832230-c0197dd311b5?ixlib=rb-1.2.1&auto=format&fit=crop&w=800&q=80");

        Activity act1 = new Activity();
        act1.setName("Visita al Colosseo");
        act1.setDescription("Tour guidato al Colosseo e Fori Imperiali.");
        act1.setLocation("Roma");
        act1.setStartTime(LocalDateTime.now().plusDays(10).plusHours(9));
        act1.setEndTime(LocalDateTime.now().plusDays(10).plusHours(12));
        act1.setParticipants(20);
        act1.setPrice(new BigDecimal("35.00"));
        act1.setTrip(trip1);
        act1.setOrganizer("Travel Adventures SRL");
        act1.setImages(List.of("https://images.unsplash.com/photo-1552832230-c0197dd311b5?ixlib=rb-1.2.1&auto=format&fit=crop&w=800&q=80"));

        Activity act2 = new Activity();
        act2.setName("Cena a Trastevere");
        act2.setDescription("Cena tipica romana in una trattoria a Trastevere.");
        act2.setLocation("Roma");
        act2.setStartTime(LocalDateTime.now().plusDays(10).plusHours(20));
        act2.setEndTime(LocalDateTime.now().plusDays(10).plusHours(23));
        act2.setParticipants(15);
        act2.setPrice(new BigDecimal("45.00"));
        act2.setTrip(trip1);
        act2.setOrganizer("Travel Adventures SRL");

        itinerary1.setActivities(Arrays.asList(act1, act2));
        trip1.setStandardItinerary(itinerary1);
        trip1.setActivities(Arrays.asList(act1, act2));

        tripRepository.save(trip1);

        // --- Trip 2: Esplorazione delle Alpi ---
        Trip trip2 = new Trip();
        trip2.setTitle("Esplorazione delle Alpi");
        trip2.setLocation("Trentino-Alto Adige");
        trip2.setDescription("Trekking, natura e relax in montagna. Per ricaricare le batterie lontano dalla città.");
        trip2.setDuration(5);
        trip2.setOrganizer(organizer);
        trip2.setImageUrl("https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?ixlib=rb-1.2.1&auto=format&fit=crop&w=800&q=80");

        Itinerary itinerary2 = new Itinerary();
        itinerary2.setTitle("Itinerario Alpino");
        itinerary2.setDescription("Escursioni giornaliere sulle Dolomiti");
        itinerary2.setStartDateTime(LocalDateTime.now().plusDays(20));
        itinerary2.setEndDateTime(LocalDateTime.now().plusDays(25));
        itinerary2.setCreator(organizer);
        itinerary2.setImagePath("https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?ixlib=rb-1.2.1&auto=format&fit=crop&w=800&q=80");

        Activity act3 = new Activity();
        act3.setName("Trekking Tre Cime");
        act3.setDescription("Escursione panoramica alle Tre Cime di Lavaredo.");
        act3.setLocation("Dolomiti");
        act3.setStartTime(LocalDateTime.now().plusDays(21).plusHours(8));
        act3.setEndTime(LocalDateTime.now().plusDays(21).plusHours(16));
        act3.setParticipants(10);
        act3.setPrice(new BigDecimal("25.00"));
        act3.setTrip(trip2);
        act3.setOrganizer("Travel Adventures SRL");
        act3.setImages(List.of("https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?ixlib=rb-1.2.1&auto=format&fit=crop&w=800&q=80"));

        Activity act4 = new Activity();
        act4.setName("Spa in Montagna");
        act4.setDescription("Giornata di relax alle terme alpine.");
        act4.setLocation("Dolomiti");
        act4.setStartTime(LocalDateTime.now().plusDays(22).plusHours(10));
        act4.setEndTime(LocalDateTime.now().plusDays(22).plusHours(18));
        act4.setParticipants(10);
        act4.setPrice(new BigDecimal("60.00"));
        act4.setTrip(trip2);
        act4.setOrganizer("Travel Adventures SRL");

        itinerary2.setActivities(Arrays.asList(act3, act4));
        trip2.setStandardItinerary(itinerary2);
        trip2.setActivities(Arrays.asList(act3, act4));

        tripRepository.save(trip2);

        // --- Trip 3: Safari in Kenya (High price) ---
        Trip trip3 = new Trip();
        trip3.setTitle("Safari in Kenya");
        trip3.setLocation("Nairobi, Kenya");
        trip3.setDescription("Un'avventura indimenticabile nella savana africana.");
        trip3.setDuration(7);
        trip3.setOrganizer(organizer);
        trip3.setImageUrl("https://images.unsplash.com/photo-1516426122078-c23e76319801?ixlib=rb-1.2.1&auto=format&fit=crop&w=800&q=80");

        Itinerary itinerary3 = new Itinerary();
        itinerary3.setTitle("Avventura Safari");
        itinerary3.setDescription("Safari nel Masai Mara");
        itinerary3.setStartDateTime(LocalDateTime.now().plusDays(40));
        itinerary3.setEndDateTime(LocalDateTime.now().plusDays(47));
        itinerary3.setCreator(organizer);

        Activity actSafari = new Activity();
        actSafari.setName("Game Drive");
        actSafari.setDescription("Safari in Jeep per vedere i Big Five.");
        actSafari.setLocation("Masai Mara");
        actSafari.setStartTime(LocalDateTime.now().plusDays(41).plusHours(6));
        actSafari.setEndTime(LocalDateTime.now().plusDays(41).plusHours(12));
        actSafari.setParticipants(6);
        actSafari.setPrice(new BigDecimal("150.00")); // Prezzo alto per testare i filtri (>100)
        actSafari.setTrip(trip3);
        actSafari.setOrganizer("Travel Adventures SRL");

        itinerary3.setActivities(Arrays.asList(actSafari));
        trip3.setStandardItinerary(itinerary3);
        trip3.setActivities(Arrays.asList(actSafari));

        tripRepository.save(trip3);


        // --- Additional independent activities ---
        Activity act5 = new Activity();
        act5.setName("Corso di Cucina Italiana");
        act5.setDescription("Impara a fare pasta e pizza con uno chef locale.");
        act5.setLocation("Firenze");
        act5.setStartTime(LocalDateTime.now().plusDays(5).plusHours(16));
        act5.setEndTime(LocalDateTime.now().plusDays(5).plusHours(20));
        act5.setParticipants(8);
        act5.setPrice(new BigDecimal("80.00"));
        act5.setOrganizer("Chef Mario");
        act5.setImages(List.of("https://images.unsplash.com/photo-1556910103-1c02745aae4d?ixlib=rb-1.2.1&auto=format&fit=crop&w=800&q=80"));

        Activity act6 = new Activity();
        act6.setName("Giro in Gondola");
        act6.setDescription("Romantico giro nei canali di Venezia.");
        act6.setLocation("Venezia");
        act6.setStartTime(LocalDateTime.now().plusDays(2).plusHours(18));
        act6.setEndTime(LocalDateTime.now().plusDays(2).plusHours(19));
        act6.setParticipants(2);
        act6.setPrice(new BigDecimal("90.00"));
        act6.setOrganizer("Gondolieri Veneziani");
        
        Activity act7 = new Activity();
        act7.setName("Free Walking Tour");
        act7.setDescription("Tour a piedi gratuito per il centro storico (mancia consigliata).");
        act7.setLocation("Milano");
        act7.setStartTime(LocalDateTime.now().plusDays(1).plusHours(10));
        act7.setEndTime(LocalDateTime.now().plusDays(1).plusHours(12));
        act7.setParticipants(30);
        act7.setPrice(BigDecimal.ZERO);
        act7.setOrganizer("Milan Tours");

        activityRepository.saveAll(Arrays.asList(act5, act6, act7));
    }
}
