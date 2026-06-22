package it.unical.ea.Travel.Controllers.itinerary;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import it.unical.ea.Travel.DTOs.itinerary.CreateItineraryRequest;
import it.unical.ea.Travel.DTOs.itinerary.ItineraryDto;
import it.unical.ea.Travel.Entities.itinerary.Itinerary;
import it.unical.ea.Travel.Mappers.itinerary.ItineraryMapper;
import it.unical.ea.Travel.Services.itinerary.ItineraryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/itinerary")
public class ItineraryController {

    private final ItineraryService itineraryService;

    @PostMapping
    public ItineraryDto saveItinerary(@Valid @RequestBody CreateItineraryRequest request) {
        Itinerary itinerary = new Itinerary();
        itinerary.setTitle(request.getTitle());
        itinerary.setDescription(request.getDescription());
        itinerary.setStartDateTime(request.getStartDateTime());
        itinerary.setEndDateTime(request.getEndDateTime());

        Itinerary savedItinerary = itineraryService.createItinerary(
                itinerary,
                request.getCreatorId(),
                request.getActivityIds()
        );

        return ItineraryMapper.toDTO(savedItinerary);
    }

    @GetMapping("/{stringId}")
    public ItineraryDto getItinerary(@PathVariable String stringId) {
        Itinerary itinerary = itineraryService.getItinerary(stringId);
        return ItineraryMapper.toDTO(itinerary);
    }

    @GetMapping
    public List<ItineraryDto> getItineraries() {
        return itineraryService.getAllItineraries().stream()
                .map(ItineraryMapper::toDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/creator/{creatorId}")
    public List<ItineraryDto> getItinerariesByCreator(@PathVariable String creatorId) {
        return itineraryService.getItinerariesByCreator(creatorId).stream()
                .map(ItineraryMapper::toDTO)
                .collect(Collectors.toList());
    }

    @DeleteMapping("/{stringId}")
    public void deleteItinerary(@PathVariable String stringId) {
        itineraryService.deleteItinerary(stringId);
    }
}
