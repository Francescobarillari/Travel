package it.unical.ea.Travel.Services.trip;

import it.unical.ea.Travel.Entities.trip.Trip;
import it.unical.ea.Travel.Mappers.trip.TripMapper;
import it.unical.ea.Travel.Repositories.trip.TripRepository;
import it.unical.ea.dtos.trip.TripDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TripService {

    private final TripRepository tripRepository;
    private final TripMapper tripMapper;

    @Transactional(readOnly = true)
    public Page<TripDto> searchTrips(String keyword, Double minPrice, Double maxPrice, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        String safeKeyword = (keyword == null) ? "" : keyword.trim();
        Page<Trip> trips = tripRepository.searchByKeyword(safeKeyword, minPrice, maxPrice, pageable);
        return trips.map(tripMapper::toDto);
    }

    @Transactional(readOnly = true)
    public java.util.Optional<TripDto> getTripById(java.util.UUID id) {
        return tripRepository.findById(id).map(tripMapper::toDto);
    }
}
